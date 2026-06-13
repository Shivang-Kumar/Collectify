package edu.tcu.cs.hogwarts_artifacts_online.security;

import java.util.Map;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;

import edu.tcu.cs.hogwarts_artifacts_online.observability.logging.Logged;
import edu.tcu.cs.hogwarts_artifacts_online.observability.tracing.Traced;
import jakarta.annotation.PostConstruct;

@Component
public class UserRequestAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext>{

	@Value("${api.endpoint.base-url}")
	private String baseUrl;
	private UriTemplate USER_URI_TEMPLATE;
	

	@PostConstruct
	public void init() {
	    this.USER_URI_TEMPLATE =
	            new UriTemplate(baseUrl + "/users/{userId}");
	}
	@Override
	@Traced("UserRequestAuthorizationManager.check")
	@Logged
	public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
		
		//Extract the userID from the request URI
		Map<String,String> uriVariables=USER_URI_TEMPLATE.match(context.getRequest().getRequestURI());
		String userIdFromRequestUri=uriVariables.get("userId");
		//Extract the userID from the  Authentication Object, which here is JWT
		String jwtUserId=((Jwt) authenticationSupplier.get().getPrincipal()).getClaim("userId").toString();
		Authentication authentication=authenticationSupplier.get();
		//Check if user have the role "ROLE_user"
		boolean hasUserRole=authentication.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_user"));
		//Check if user have the role "ROLE_admin"
		boolean hasAdminRole=authentication.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_admin"));;

		
		//Compare the two userIds.
		boolean userIdsMatch=userIdFromRequestUri!=null && userIdFromRequestUri.equals(jwtUserId);
		
		
		return new AuthorizationDecision(hasAdminRole||(hasUserRole&&userIdsMatch));
	}

}
