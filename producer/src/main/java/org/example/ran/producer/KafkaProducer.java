package org.example.ran.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
        System.out.println("Sent: " + message);
    }

    public void sendMessagePartition(String topic, Integer partition, String message) {
        kafkaTemplate.send(topic, partition, null, message);
        System.out.println("Sent: " + message);
    }
}