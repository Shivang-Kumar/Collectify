package edu.tcu.cs.hogwarts_artifacts_online.notification;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class NotificationEventPublisher {

    private final KafkaTemplate<String, Notification> kafkaTemplate;

    public NotificationEventPublisher(KafkaTemplate<String, Notification> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(Notification event) {
        kafkaTemplate.send("notification.events", event.getEventId(), event);
    }
}