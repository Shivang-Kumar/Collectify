package edu.tcu.cs.hogwarts_artifacts_online.security;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import edu.tcu.cs.hogwarts_artifacts_online.observability.logging.Logged;
import edu.tcu.cs.hogwarts_artifacts_online.observability.metrics.AuthMetrics;
import edu.tcu.cs.hogwarts_artifacts_online.observability.tracing.Traced;
import edu.tcu.cs.hogwarts_artifacts_online.rediscache.RedisCacheClient;
import edu.tcu.cs.hogwarts_artifacts_online.user.MyUserPrincipal;
import edu.tcu.cs.hogwarts_artifacts_online.user.User;
import edu.tcu.cs.hogwarts_artifacts_online.user.DTO.UserDto;
import edu.tcu.cs.hogwarts_artifacts_online.user.converter.UserToUserDtoConverter;

@Service
public class AuthService {
	
	private final JWTProvider jwtProvider;
	
	private final UserToUserDtoConverter userToUserDtoConverter;
	
	private final AuthMetrics authMetrics;
	
	private final RedisCacheClient redisCacheClient;
	

	public AuthService(JWTProvider jwtProvider,RedisCacheClient redisCacheClient,AuthMetrics authMetrics) {
		super();
		this.jwtProvider = jwtProvider;
		this.userToUserDtoConverter = new UserToUserDtoConverter();
		this.redisCacheClient=redisCacheClient;
		this.authMetrics=authMetrics;
	}

	@Traced("authService.createLoginInfo")
	@Logged
	public Map<String,Object> createLoginInfo(Authentication authentication) {
		//Create User info object
		MyUserPrincipal principal=(MyUserPrincipal) authentication.getPrincipal();
		User user=principal.getUser();
		UserDto userDto=this.userToUserDtoConverter.convert(user);
		//Create a JWT
		String token=this.jwtProvider.createToken(authentication);
		// Save the token in redis,Key is "whitelist:{userId} and value is the token"
		this.redisCacheClient.set("whitelist:"+user.getUsername(), token, 2, TimeUnit.HOURS);
		
		
		
		
		Map<String,Object> loginResultMap=new HashMap<>();
		loginResultMap.put("userInfo",userDto);
		loginResultMap.put("token", token);
		
		
		//Incrementing the counter
		authMetrics.incrementSuccessCounter();
		return loginResultMap;
		
	}
	

}
