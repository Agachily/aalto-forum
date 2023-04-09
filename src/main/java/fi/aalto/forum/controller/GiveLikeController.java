package fi.aalto.forum.controller;

import fi.aalto.forum.event.EventProducer;
import fi.aalto.forum.service.GiveLikeService;
import fi.aalto.forum.entity.Event;
import fi.aalto.forum.entity.User;
import fi.aalto.forum.util.HostHolder;
import fi.aalto.forum.util.ForumConstant;
import fi.aalto.forum.util.ForumUtil;
import fi.aalto.forum.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

@Controller
public class GiveLikeController implements ForumConstant {

    private final GiveLikeService giveLikeService;

    private final HostHolder hostHolder;

    private final EventProducer eventProducer;

    private final RedisTemplate redisTemplate;

    public GiveLikeController(GiveLikeService giveLikeService, HostHolder hostHolder, EventProducer eventProducer, RedisTemplate redisTemplate) {
        this.giveLikeService = giveLikeService;
        this.hostHolder = hostHolder;
        this.eventProducer = eventProducer;
        this.redisTemplate = redisTemplate;
    }

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String giveOrCancelLike(int entityType, int entityId, int entityUserId, int postId) {
        /* Get the current user */
        User user = hostHolder.getUser();
        giveLikeService.giveOrCancelLike(user.getId(), entityType, entityId, entityUserId);

        long likeNumberOfEntity = giveLikeService.findLikeNumberOfEntity(entityType, entityId);
        int status = giveLikeService.checkLikeStatusOfEntity(user.getId(), entityType, entityId);

        HashMap<String, Object> map = new HashMap<>();
        map.put("likeCount", likeNumberOfEntity);
        map.put("likeStatus", status);

        /* Only fire the given like event when the user is giving like(not cancelling like) */
        if (status == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            eventProducer.fireEvent(event);
        }

        /* 只有点赞对象为帖子的时候才重新计算帖子分数 */
        if(entityType == ENTITY_TYPE_POST) {
            // 计算帖子分数
            String redisKey = RedisUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }

        return ForumUtil.getJSONString(200, null, map);
    }
}
