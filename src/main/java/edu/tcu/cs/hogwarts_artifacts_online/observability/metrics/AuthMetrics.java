package edu.tcu.cs.hogwarts_artifacts_online.observability.metrics;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class AuthMetrics {
			
	
	private final Counter authSuccessCounter;
	private final Counter authFailureCounter;
	private  final Counter tokenFailureCounter;

	AuthMetrics(MeterRegistry meterRegistry)
	{
		this.authSuccessCounter=Counter.builder("auth.attempts")
				.description("Number of Authentication attempts")
				.tag("result", "success")
				.register(meterRegistry);
		this.authFailureCounter=Counter.builder("auth.attempts")
				.description("Number of authentication attempts")
				.tag("result", "login_failure")
				.register(meterRegistry);
		this.tokenFailureCounter=Counter.builder("auth.attempts")
				.description("Number of authentication attempts")
				.tag("result", "token_failure")
				.register(meterRegistry);
		
	}
	
	public void incrementSuccessCounter()
	{
		authSuccessCounter.increment(1);
	}
	public void incrementFailureCounter()
	{
		authFailureCounter.increment(1);
	}
	public void incrementTokenFailureCounter()
	{
		tokenFailureCounter.increment(1);
	}

	
}
