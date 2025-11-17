package edu.tcu.cs.hogwarts_artifacts_online.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import edu.tcu.cs.hogwarts_artifacts_online.rediscache.RedisCacheClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class JwtInterceptor  implements HandlerInterceptor{
	
	private final RedisCacheClient redisCacheClient;
	
	
	public JwtInterceptor(RedisCacheClient redisCacheClient) {
		super();
		this.redisCacheClient = redisCacheClient;
	}


	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		
		//Get the token from the request header
		
		String authorizationHeader=request.getHeader("Authorization");
		//If token is not null and starts with "Bearer " , then we need to verify if this token is present in redis
		
		if(authorizationHeader !=null && authorizationHeader.startsWith("Bearer "))
		{
			Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
			Jwt jwt=(Jwt)authentication.getPrincipal();
			
			//Retrieve the userId from the JWT claims and check if the token is in the redis whitelist or not
			String userId=jwt.getSubject();
			if(!this.redisCacheClient.isUserTokenInWhiteList(userId, jwt.getTokenValue()))
			{
				throw new BadCredentialsException("Invadlid Token");
			}
		}
		//Else this request is just a public request that does not need a token Eg: Login , register etc
		return true;
	}
	

}
