package edu.tcu.cs.hogwarts_artifacts_online.system;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import edu.tcu.cs.hogwarts_artifacts_online.security.JwtInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
//This class is being used to register our Jwt interceptor to spring boot

	private JwtInterceptor jwtInterceptor;
	

	public WebConfig(JwtInterceptor jwtInterceptor) {
	super();
	this.jwtInterceptor = jwtInterceptor;
}


	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(this.jwtInterceptor).addPathPatterns("/**");
	}
}
