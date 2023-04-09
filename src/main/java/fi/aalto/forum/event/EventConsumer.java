package fi.aalto.forum.event;

import fi.aalto.forum.service.DiscussPostService;
import fi.aalto.forum.service.ElasticsearchService;
import fi.aalto.forum.service.MessageService;
import fi.aalto.forum.entity.DiscussPost;
import fi.aalto.forum.entity.Event;
import fi.aalto.forum.entity.Message;
import fi.aalto.forum.util.ForumConstant;
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements ForumConstant {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    private final MessageService messageService;

    private final DiscussPostService discussPostService;

    private final ElasticsearchService elasticsearchService;

    public EventConsumer(MessageService messageService, DiscussPostService discussPostService, ElasticsearchService elasticsearchService) {
        this.messageService = messageService;
        this.discussPostService = discussPostService;
        this.elasticsearchService = elasticsearchService;
    }

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_FOLLOW, TOPIC_LIKE})
    public void handleCommentMessage(ConsumerRecord record) {
        if (isRecordEmpty(record)) return;
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null) {
            logger.error("The message format is not correct");
            return;
        }

        /* The system should send message to the user in the background */
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        /* Set the content of the message */
        HashMap<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        // 将Event中的Map集合中的数据也存入到Content中
        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }

        /* Convert the content object to JSON string and store it as the content of the message */
        message.setContent(JSONObject.toJSONString(content));
        /* Add the message to database */
        messageService.addMessage(message);
    }

    /**
     * 消费发帖事件
     */
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record) {
        if (isRecordEmpty(record)) return;

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null) {
            logger.error("The message format is not correct");
            return;
        }

        /* 从MySQL中查询出帖子数据，并存储到ES中 */
        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);
    }

    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record) {
        if (isRecordEmpty(record)) return;

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null) {
            logger.error("The message format is not correct");
            return;
        }

        /* 从ES中删除帖子 */
        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }

    private boolean isRecordEmpty(ConsumerRecord record) {
        if(record == null || record.value() == null) {
            logger.error("The obtained message is empty");
            return true;
        }
        return false;
    }

}
