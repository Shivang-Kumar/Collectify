package edu.tcu.cs.hogwarts_artifacts_online.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

public class AuthMetrics {
			
	
	private final Counter authSuccessCounter;
	private final Counter authFailureCounter;
	
	AuthMetrics(MeterRegistry meterRegistry)
	{
		this.authSuccessCounter=Counter.builder("auth.attempts")
				.description("Number of Authentication attempts")
				.tag("result", "success")
				.register(meterRegistry);
		this.authFailureCounter=Counter.builder("auth.attempts")
				.description("Number of authentication attempts")
				.tag("result", "failure")
				.register(meterRegistry);
	}
	
	void incrementSuccessCounter()
	{
		authSuccessCounter.increment();
	}
	void incrementFailureCounter()
	{
		authFailureCounter.increment();
	}
	
	
}
