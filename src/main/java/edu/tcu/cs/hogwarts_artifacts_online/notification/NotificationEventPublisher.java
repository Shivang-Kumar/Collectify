package edu.tcu.cs.hogwarts_artifacts_online.notification;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import edu.tcu.cs.hogwarts_artifacts_online.observability.logging.Logged;
import edu.tcu.cs.hogwarts_artifacts_online.observability.tracing.Traced;


@Service
public class NotificationEventPublisher {

    private final KafkaTemplate<String, Notification> kafkaTemplate;

    public NotificationEventPublisher(KafkaTemplate<String, Notification> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Traced("notification-event-published.publish")
    @Logged
    public void publish(Notification event) {
        kafkaTemplate.send("notification.events", event.getEventId(), event);
    }
}