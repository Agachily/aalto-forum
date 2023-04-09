package fi.aalto.forum.controller;

import fi.aalto.forum.entity.*;
import fi.aalto.forum.event.EventProducer;
import fi.aalto.forum.service.GiveLikeService;
import fi.aalto.forum.service.CommentService;
import fi.aalto.forum.service.DiscussPostService;
import fi.aalto.forum.service.UserService;
import fi.aalto.forum.util.HostHolder;
import fi.aalto.forum.util.ForumConstant;
import fi.aalto.forum.util.ForumUtil;
import fi.aalto.forum.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements ForumConstant {
    private final DiscussPostService discussPostService;

    private final HostHolder hostHolder;

    private final UserService userService;

    private final CommentService commentService;

    private final GiveLikeService giveLikeService;

    private final EventProducer eventProducer;

    private final RedisTemplate redisTemplate;

    public DiscussPostController(
            DiscussPostService discussPostService,
            HostHolder hostHolder, UserService userService,
            CommentService commentService, GiveLikeService giveLikeService,
            EventProducer eventProducer, RedisTemplate redisTemplate) {
        this.discussPostService = discussPostService;
        this.hostHolder = hostHolder;
        this.userService = userService;
        this.commentService = commentService;
        this.giveLikeService = giveLikeService;
        this.eventProducer = eventProducer;
        this.redisTemplate = redisTemplate;
    }

    // To return the JSON string, we should use the @RequestBody annotation and the String as the return type.
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return ForumUtil.getJSONString(403, "Login required");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        /* Put the newly added post to ES */
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

        /* Denoting the score of the post needed to be refreshed */
        String redisKey = RedisUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, post.getId());

        // The error will be processed in the future
        return ForumUtil.getJSONString(200, "The post has been released");
    }

    @RequestMapping(path = "/detail/{id}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("id") int id, Model model, Page page) {
        /* Get the post content */
        DiscussPost post = discussPostService.findDiscussPostById(id);
        model.addAttribute("post", post);

        /* Get the user of the post */
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        // 该帖子点赞数量
        long likeNumberOfPost = giveLikeService.findLikeNumberOfEntity(ENTITY_TYPE_POST, id);
        model.addAttribute("likeNumberOfPost", likeNumberOfPost);
        // 如果用户没登陆，则直接显示赞，不显示已赞
        long likeStatusOfPost = hostHolder.getUser() == null ? 0 :
                giveLikeService.checkLikeStatusOfEntity(hostHolder.getUser().getId(), ENTITY_TYPE_POST, id);
        model.addAttribute("likeStatusOfPost", likeStatusOfPost);

        // 评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + id);
        page.setRows(post.getCommentCount());

        // 目前一共有两种评论，给帖子的评论和给评论的评论
        // 先获取给帖子的评论
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        // 使用List和Map来组织评论信息，Map中存储评论，用户信息，该评论的回复
        List<Map<String, Object>> commentListToShow = new ArrayList<>();
        if (commentList != null) {
            for (Comment c : commentList) {
                Map<String, Object> commentToShow = new HashMap<>();
                // 评论对象
                commentToShow.put("comment", c);
                // 该评论的作者
                commentToShow.put("user", userService.findUserById(c.getUserId()));
                // 评论的点赞数量
                long likeNumberOfComment = giveLikeService.findLikeNumberOfEntity(ENTITY_TYPE_COMMENT, c.getId());
                commentToShow.put("likeNumberOfComment", likeNumberOfComment);
                long likeStatusOfComment = hostHolder.getUser() == null ? 0 :
                        giveLikeService.checkLikeStatusOfEntity(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, c.getId());
                commentToShow.put("likeStatusOfComment", likeStatusOfComment);

                // 获取与该评论的相关的回复
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, c.getId(), 0, Integer.MAX_VALUE);

                ArrayList<Object> replyListToShow = new ArrayList<>();

                if (replyList != null) {
                    for (Comment r : replyList) {
                        Map<String, Object> replyToShow = new HashMap<>();
                        // 回复
                        replyToShow.put("reply", r);
                        // 作者
                        replyToShow.put("user", userService.findUserById(r.getUserId()));
                        // 回复目标
                        User target = r.getTargetId() == 0 ? null : userService.findUserById(r.getTargetId());
                        replyToShow.put("target", target);
                        // 回复的点赞
                        likeNumberOfComment = giveLikeService.findLikeNumberOfEntity(ENTITY_TYPE_COMMENT, r.getId());
                        replyToShow.put("likeNumberOfComment", likeNumberOfComment);
                        likeStatusOfComment = hostHolder.getUser() == null ? 0 :
                                giveLikeService.checkLikeStatusOfEntity(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, r.getId());
                        replyToShow.put("likeStatusOfComment", likeStatusOfComment);

                        replyListToShow.add(replyToShow);
                    }
                }
                commentToShow.put("replies", replyListToShow);

                // 该评论回复的数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, c.getId());
                commentToShow.put("replyCount", replyCount);

                commentListToShow.add(commentToShow);
            }
        }

        // 该帖子下的所有回复都被封装到了commentListToShow对象中，并将该对象放到model中。
        model.addAttribute("comments", commentListToShow);

        return "/site/discuss-detail";
    }

    /**
     * 置顶帖子
     *
     * @param id
     * @return
     */
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id) {
        discussPostService.updateType(id, 1);
        /* 帖子状态发生了变化，需要将其同步到Elasticsearch中 */
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return ForumUtil.getJSONString(200, "The type of post is updated");
    }

    /**
     * 加精
     *
     * @param id
     * @return
     */
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id) {
        discussPostService.updateStatus(id, 1);
        /* 帖子状态发生了变化，需要将其同步到Elasticsearch中 */
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        // 重新计算帖子分数
        String redisKey = RedisUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);

        return ForumUtil.getJSONString(200, "The status of post is updated");
    }

    /**
     * 删除，即将帖子设置为删除状态，并不是从数据库中删除
     *
     * @param id
     * @return
     */
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.updateStatus(id, 2);
        /* 帖子状态发生了变化，需要将其同步到Elasticsearch中 */
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return ForumUtil.getJSONString(200, "The post is deleted");
    }
}
