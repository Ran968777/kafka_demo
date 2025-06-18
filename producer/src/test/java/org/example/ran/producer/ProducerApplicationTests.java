package org.example.ran.producer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProducerApplicationTests {

    @Test
    void contextLoads() {
    }


    @Autowired
    KafkaProducer kafkaProducer;

    @Test
    public void testSendMessage() {
        for (int i = 0; i < 20; i++) {
            kafkaProducer.sendMessage("test-topic", "hello kafka-eagle " + i);
        }
    }
}
