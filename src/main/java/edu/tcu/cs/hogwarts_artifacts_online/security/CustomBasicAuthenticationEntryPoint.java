package edu.tcu.cs.hogwarts_artifacts_online.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import edu.tcu.cs.hogwarts_artifacts_online.observability.logging.Logged;
import edu.tcu.cs.hogwarts_artifacts_online.observability.metrics.AuthMetrics;
import edu.tcu.cs.hogwarts_artifacts_online.observability.tracing.Traced;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomBasicAuthenticationEntryPoint implements AuthenticationEntryPoint{

	//This class is used because security exception occurs before hitting controllers thus RestControllerAdvice cannot be used so we create our own authentication entry point.
	
	private final HandlerExceptionResolver resolver;
	private final AuthMetrics authMetrics;
	
	
	public CustomBasicAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver")HandlerExceptionResolver resolver,AuthMetrics authMetrics) {
		super();
		this.resolver = resolver;
		this.authMetrics=authMetrics;
	}


	@Override
	@Traced("customBasicAuthenticationEntryPoint.commence")
	@Logged
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
      response.addHeader("WWW-Authenticate", "Basic realm=\"Realm\""); 
      authMetrics.incrementFailureCounter();
      this.resolver.resolveException(request, response, null, authException);
	}

}
