package edu.tcu.cs.hogwarts_artifacts_online.security;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.tcu.cs.hogwarts_artifacts_online.observability.logging.Logged;
import edu.tcu.cs.hogwarts_artifacts_online.observability.tracing.Traced;
import edu.tcu.cs.hogwarts_artifacts_online.system.Result;
import edu.tcu.cs.hogwarts_artifacts_online.system.StatusCode;

@RestController
@RequestMapping("${api.endpoint.base-url}/users")
public class AuthController {
	
	private final AuthService authService;
	private static final Logger LOGGER=LoggerFactory.getLogger(AuthController.class);



	public AuthController(AuthService authService) {
		super();
		this.authService = authService;
	}


	
	@PostMapping("/login")
	@Traced("authController.getLoginInfo")
	@Logged
	public Result getLoginInfo(Authentication authentication) {
		LOGGER.debug("Authenticated User: '{}'",authentication.getName());
		System.out.println(authentication);
		return new Result(true,StatusCode.SUCCESS,"User info and Json web token",this.authService.createLoginInfo(authentication));
	}
}
