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
    public void testSendMessage() throws InterruptedException {
        for (int i = 0; i < 20; i++) {
            kafkaProducer.sendMessage("test-topic", "hello kafka-eagle " + i);
            Thread.sleep(5000);
        }
    }


    @Test
    public void testSendMessagePartition() throws InterruptedException {
        for (int i = 0; i < 20; i++) {
            kafkaProducer.sendMessagePartition("test-topic", i % 2, " Partition: " + i%2 +" ,hello java");
        }
    }
}
