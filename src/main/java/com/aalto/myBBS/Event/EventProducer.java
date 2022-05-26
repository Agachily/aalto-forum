package com.aalto.myBBS.Event;

import com.aalto.myBBS.service.entity.Event;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    // Send message to a certain topic
    public void fireEvent(Event event) {
        // Convert the Event object to a json string and then send
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
