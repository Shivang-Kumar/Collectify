package edu.tcu.cs.hogwarts_artifacts_online.system.exception;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tcu.cs.hogwarts_artifacts_online.observability.logging.Logged;
import edu.tcu.cs.hogwarts_artifacts_online.observability.tracing.Traced;
import edu.tcu.cs.hogwarts_artifacts_online.system.ObjectNotFoundException;
import edu.tcu.cs.hogwarts_artifacts_online.system.Result;
import edu.tcu.cs.hogwarts_artifacts_online.system.StatusCode;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

	@ExceptionHandler(ObjectNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@Traced("ExceptionHandlerAdvice.handleObjectNotFoundException")
	@Logged
	Result handleObjectNotFoundException(ObjectNotFoundException ex) {
		return new Result(false, StatusCode.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@Traced("ExceptionHandlerAdvice.handleValidationException")
	@Logged
	Result handleValidationException(MethodArgumentNotValidException ex) {
		List<ObjectError> errors = ex.getBindingResult().getAllErrors();
		Map<String, String> map = new HashMap<>(errors.size());
		errors.forEach((error) -> {
			String key = ((FieldError) error).getField();
			String val = error.getDefaultMessage();
			map.put(key, val);
		});
		return new Result(false, StatusCode.INVALID_ARGUMENT, "Provided arguments are invalid , see data for details.",
				map);
	}

	@ExceptionHandler({ InsufficientAuthenticationException.class })
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@Traced("ExceptionHandlerAdvice.handleInsufficientAuthenticationException")
	@Logged
	Result handleInsufficientAuthenticationException(InsufficientAuthenticationException ex) {
		return new Result(false, StatusCode.UNAUTHORIZED, "Log in credentials are missing", ex.getMessage());
	}

	@ExceptionHandler({ UsernameNotFoundException.class, BadCredentialsException.class })
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@Traced("ExceptionHandlerAdvice.handleAuthenticationException")
	@Logged
	Result handleAuthenticationException(Exception ex) {
		return new Result(false, StatusCode.UNAUTHORIZED, "username or password is incorrect", ex.getMessage());
	}

	@ExceptionHandler({ AccountStatusException.class })
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@Traced("ExceptionHandlerAdvice.handlAccountStatusException")
	@Logged
	Result handlAccountStatusException(AccountStatusException ex) {
		return new Result(false, StatusCode.UNAUTHORIZED, "User account is abnormal", ex.getMessage());
	}

	// Related t open API exception
	@ExceptionHandler({HttpClientErrorException.class, HttpStatusCodeException.class})
	@Traced("ExceptionHandlerAdvice.handlRestClientExceptionException")
	@Logged
	ResponseEntity<Result> handlRestClientExceptionException(HttpStatusCodeException ex)
	{
		String errorResponse = ex.getResponseBodyAsString();
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode root;
		String message;
		try {
		    root = objectMapper.readTree(errorResponse);
		    message = root.path("error").path("message").asText();
		    System.out.println("Extracted Message: " + message);
		} catch (JsonProcessingException e) {
		    e.printStackTrace();
		    message = "Could not process the request";
		}
		return new ResponseEntity<>(new Result(false, ex.getStatusCode().value(), 
		    "A rest client error occurs, see data for details", message), ex.getStatusCode());

	          
	}
	
	
	
	@ExceptionHandler({InvalidBearerTokenException.class})
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@Traced("ExceptionHandlerAdvice.handleInvalidBearerTokenException")
	@Logged
	Result handleInvalidBearerTokenException(InvalidBearerTokenException ex)
	{
		return new Result(false,StatusCode.UNAUTHORIZED,"The access token provided is expired, revoked , malformed or invalid for other reasons.",ex.getMessage());
	}

	@ExceptionHandler({ AccessDeniedException.class })
	@ResponseStatus(HttpStatus.FORBIDDEN)
	@Traced("ExceptionHandlerAdvice.handleAccessDeniedException")
	@Logged
	Result handleAccessDeniedException(AccessDeniedException ex) {
		return new Result(false, StatusCode.FORBIDDEN, "No permission.", ex.getMessage());
	}
	
	
	@ExceptionHandler({PasswordChangeIllegalArgumentException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@Traced("ExceptionHandlerAdvice.handlePasswordChangeIllegalArgumentException")
	@Logged
	public Result handlePasswordChangeIllegalArgumentException(PasswordChangeIllegalArgumentException ex)
	{
		return new Result(false,StatusCode.INVALID_ARGUMENT,ex.getMessage());
	}
	
	
	

	@ExceptionHandler({ Exception.class })
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@Traced("ExceptionHandlerAdvice.handleOtherException")
	@Logged
	Result handleOtherException(Exception ex) {
		return new Result(false, StatusCode.INTERNAL_SERVER_ERROR, "A server internal error occurs.", ex.getMessage());
	}
	
	
	
	

}
