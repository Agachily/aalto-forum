package fi.aalto.forum.quartz;

import fi.aalto.forum.service.DiscussPostService;
import fi.aalto.forum.service.ElasticsearchService;
import fi.aalto.forum.service.GiveLikeService;
import fi.aalto.forum.entity.DiscussPost;
import fi.aalto.forum.util.ForumConstant;
import fi.aalto.forum.util.RedisUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This is a timing task to refresh the score of post
 */
public class PostScoreRefreshJob implements Job, ForumConstant {

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);
    private final RedisTemplate redisTemplate;
    private final DiscussPostService discussPostService;
    private final GiveLikeService giveLikeService;
    private final ElasticsearchService elasticsearchService;
    /* The date when the service begin, using for calculating post score */
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("Fail to initialize the start time", e);
        }
    }

    public PostScoreRefreshJob(RedisTemplate redisTemplate, DiscussPostService discussPostService, GiveLikeService giveLikeService, ElasticsearchService elasticsearchService) {
        this.redisTemplate = redisTemplate;
        this.discussPostService = discussPostService;
        this.giveLikeService = giveLikeService;
        this.elasticsearchService = elasticsearchService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String redisKey = RedisUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if (operations.size() == 0) {
            logger.info("[Task Canceled] No post requires to be refreshed");
            return;
        }

        logger.info("[Task Start] Refreshing post score, has: " + operations.size());
        while (operations.size() > 0) {
            this.refresh((Integer) operations.pop());
        }
        logger.info("[Task End] Finish the refreshing of post score");
    }

    private void refresh(int postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);

        if (post == null) {
            logger.error("The post with id: " + postId + " doesn't exist");
            return;
        }

        // 是否精华
        boolean wonderful = post.getStatus() == 1;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 点赞数量
        long likeCount = giveLikeService.findLikeNumberOfEntity(ENTITY_TYPE_POST, postId);

        // 计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        // 分数 = 帖子权重 + 距离天数
        double score = Math.log10(Math.max(w, 1))
                + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
        // 更新帖子分数
        discussPostService.updateScore(postId, score);
        // 同步搜索数据到Elasticsearch
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }

}
