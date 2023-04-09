package fi.aalto.forum.event;

import fi.aalto.forum.entity.Event;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {

    private final KafkaTemplate kafkaTemplate;

    public EventProducer(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Send message to a certain topic
     */
    public void fireEvent(Event event) {
        /* Convert the Event object to a json string and then send */
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
