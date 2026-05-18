package edu.tcu.cs.hogwarts_artifacts_online.observability.metrics;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;


@Component
public class ArtifactMetrics {
	
	private final Counter artifactSearchedCounter;
	private final Counter artifactCreatedCounter;
	private final Counter artifactDeletedCounter;
	
	public ArtifactMetrics(MeterRegistry meterRegister)
	{
		this.artifactSearchedCounter=Counter.builder("artifact.operations")
				.description("Number of artifact searched")
				.tag("operation", "searched")
				.register(meterRegister);
		this.artifactCreatedCounter=Counter.builder("artifact.operations")
				.description("Number of artifact created")
				.tag("operation", "created")
				.register(meterRegister);
		this.artifactDeletedCounter=Counter.builder("artifact.operations")
				.description("Number of artifact deleted")
				.tag("operation", "deleted")
				.register(meterRegister);
	}
	
	public void incrementSearch()
	{
		artifactSearchedCounter.increment(1);
	}
	public void incrementCreated()
	{
		artifactCreatedCounter.increment(1);
	}
	public void incrementDeleted()
	{
		artifactDeletedCounter.increment(1);
	}
	
}
