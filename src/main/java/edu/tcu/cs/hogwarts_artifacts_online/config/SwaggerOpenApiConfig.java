package edu.tcu.cs.hogwarts_artifacts_online.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerOpenApiConfig {

	@Bean
	public OpenAPI customOpenAPI() {

		return new OpenAPI().info(new Info().title("Collectify").version("1.0").description("""
				This API provides access to the Collectify system.

				Authentication Flow

				1. Register a new user using POST /api/v1/users
				2. Authenticate using POST /api/v1/users/login with HTTP Basic Authentication
				3. Copy the JWT token returned in the response
				4. Click the Authorize button in Swagger
				5. Provide the JWT token using the Bearer scheme
				6. Access protected endpoints

				Notes

				- Some endpoints are public and require no authentication.
				- The login endpoint requires HTTP Basic Authentication.
				- Protected endpoints require a valid JWT Bearer token.
				"""))

				.components(new Components()

						.addSecuritySchemes("basicAuth",
								new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic"))

						.addSecuritySchemes("bearerAuth", new SecurityScheme().type(SecurityScheme.Type.HTTP)
								.scheme("bearer").bearerFormat("JWT")));
	}
}