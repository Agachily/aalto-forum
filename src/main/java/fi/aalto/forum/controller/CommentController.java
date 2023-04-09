package fi.aalto.forum.controller;

import fi.aalto.forum.event.EventProducer;
import fi.aalto.forum.service.CommentService;
import fi.aalto.forum.service.DiscussPostService;
import fi.aalto.forum.entity.Comment;
import fi.aalto.forum.entity.DiscussPost;
import fi.aalto.forum.entity.Event;
import fi.aalto.forum.util.HostHolder;
import fi.aalto.forum.util.ForumConstant;
import fi.aalto.forum.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements ForumConstant {

    private final CommentService commentService;

    private final EventProducer eventProducer;

    private final DiscussPostService discussPostService;

    /* This is used for getting the current user */
    private final HostHolder hostHolder;

    private final RedisTemplate redisTemplate;

    public CommentController(
            CommentService commentService,
            EventProducer eventProducer,
            DiscussPostService discussPostService,
            HostHolder hostHolder, RedisTemplate redisTemplate) {
        this.commentService = commentService;
        this.eventProducer = eventProducer;
        this.discussPostService = discussPostService;
        this.hostHolder = hostHolder;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Add comment
     */
    @RequestMapping(path = "/add/{id}", method = RequestMethod.POST)
    public String addComment(@PathVariable("id") int id, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        /* Trigger the comment event and send the system message to kafka */
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", id);
        /* Sent the entityUserId, namely the author of the entity according to different entity type */
        if(comment.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost targetDiscuss = discussPostService.findDiscussPostById(comment.getEntityId());
            /* Set the author of the entity */
            event.setEntityUserId(targetDiscuss.getUserId());
        }
        if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment targetComment = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(targetComment.getUserId());
        }
        /* Use Kafka to send the event to message queue */
        eventProducer.fireEvent(event);

        // 当对帖子进行评论的时候，帖子对象中存储的评论数量这一数据会发生改变，因此我们需要更新ES中存储的帖子内容
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(id);
            eventProducer.fireEvent(event);

            String redisKey = RedisUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, id);
        }

        /* Redirect the endpoint /discuss/detail/id to show the current discuss post */
        return "redirect:/discuss/detail/" + id;
    }
}
