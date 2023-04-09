package fi.aalto.forum.kafakatest;

import fi.aalto.forum.ForumApplication;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = ForumApplication.class)
public class KafkaTest {
    @Autowired
    private KafkaProducer kafkaProducer;

    @Test
    public void testKafka() {
       kafkaProducer.sendMessage("test", "hello");
       kafkaProducer.sendMessage("test", "world");

        try {
            Thread.sleep(1000*30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}



