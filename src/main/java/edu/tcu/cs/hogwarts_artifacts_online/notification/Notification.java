package edu.tcu.cs.hogwarts_artifacts_online.notification;

import java.time.Instant;
import java.util.UUID;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
@Builder
public class Notification {
		
	
	private String eventId;
	
	private String traceId;
	
	@Enumerated(EnumType.STRING)
	private NotificationChannel channel;
	
	private String recipient;
	
	private String templateId;
	
	private String payload;
	
	
}
