package edu.tcu.cs.hogwarts_artifacts_online.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import edu.tcu.cs.hogwarts_artifacts_online.observability.logging.Logged;
import edu.tcu.cs.hogwarts_artifacts_online.observability.tracing.Traced;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


//This class handles unsuccessfull JWT authorization.
@Component
public class CustomBearerTokenAccessDeniedException implements AccessDeniedHandler{

	
	private final HandlerExceptionResolver resolver;
	
	public CustomBearerTokenAccessDeniedException(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
		super();
		this.resolver = resolver;
	}

	@Override
	@Traced("CustomBearerTokenAccessDeniedException.handle")
	@Logged
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		this.resolver.resolveException(request, response, null, accessDeniedException);
		
	}

	
}
