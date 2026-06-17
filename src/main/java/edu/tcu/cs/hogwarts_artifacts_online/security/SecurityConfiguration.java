package edu.tcu.cs.hogwarts_artifacts_online.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import edu.tcu.cs.hogwarts_artifacts_online.observability.logging.Logged;
import edu.tcu.cs.hogwarts_artifacts_online.observability.tracing.Traced;

@Configuration
public class SecurityConfiguration {

	private final RSAPublicKey publicKey;
	private final RSAPrivateKey privateKey;

	private final CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint;
	private final CustomBearerTokenAuthenticationEntryPoint customBearerTokenAuthenticationEntryPoint;
	private final CustomBearerTokenAccessDeniedException customBearerTokenAccessDeniedException;
	private final UserRequestAuthorizationManager userRequestAuthorizationManager;

	public SecurityConfiguration(CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint,
			CustomBearerTokenAuthenticationEntryPoint customBearerTokenAuthenticationEntryPoint,
			CustomBearerTokenAccessDeniedException customBearerTokenAccessDeniedException,UserRequestAuthorizationManager userRequestAuthorizationManager)
			throws NoSuchAlgorithmException {
		super();
		this.customBasicAuthenticationEntryPoint = customBasicAuthenticationEntryPoint;
		this.customBearerTokenAuthenticationEntryPoint = customBearerTokenAuthenticationEntryPoint;
		this.customBearerTokenAccessDeniedException = customBearerTokenAccessDeniedException;
		this.userRequestAuthorizationManager=userRequestAuthorizationManager;
		// Generate a public/Private key pair
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);// Generated key will have size of 2048 bits
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		this.publicKey = (RSAPublicKey) keyPair.getPublic();
		this.privateKey = (RSAPrivateKey) keyPair.getPrivate();

	}

	@Value("${api.endpoint.base-url}")
	private String baseUrl;
	
	
	
	//This security filter chain runs to provide basic authentication for login endpoint
	@Bean
	@Order(1)
	@Traced("SecurityConfiguration.loginSecurityFilterChain")
	@Logged
	SecurityFilterChain loginSecurityFilterChain(HttpSecurity http) throws Exception {

	    return http
	            .securityMatcher( new AntPathRequestMatcher(
                        this.baseUrl + "/users/login",
                        "POST"))
	            .authorizeHttpRequests(auth -> auth
	                    .anyRequest().authenticated())
	            .httpBasic(httpBasic -> httpBasic
	                    .authenticationEntryPoint(
	                            this.customBasicAuthenticationEntryPoint))
	            .csrf(csrf -> csrf.disable())
	            .sessionManagement(session -> session
	                    .sessionCreationPolicy(
	                            SessionCreationPolicy.STATELESS))
	            .build();
	}

	
	//This security filter chain handles all other endpoints
	@Bean
	@Traced("SecurityConfiguration.securityFilterChain")
	@Logged
	@Order(2)
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.authorizeHttpRequests(authorizeHttpRequest -> authorizeHttpRequest
						.requestMatchers(HttpMethod.GET, this.baseUrl + "/artifacts/**").permitAll()
						.requestMatchers(HttpMethod.POST, this.baseUrl + "/users").permitAll()
						.requestMatchers(HttpMethod.GET, this.baseUrl + "/users/**").access(this.userRequestAuthorizationManager)		  //Authorization rule is defined in userRequestAuthorizationManager																	// this
						.requestMatchers(HttpMethod.PUT, this.baseUrl + "/users/**").access(this.userRequestAuthorizationManager)
						.requestMatchers(HttpMethod.DELETE, this.baseUrl + "/users/**").access(this.userRequestAuthorizationManager)
						.requestMatchers(
				                "/v3/api-docs/**",
				                "/swagger-ui/**",
				                "/swagger-ui.html",
				                "/openapi.yaml"
				                
				        ).permitAll()
						.requestMatchers(EndpointRequest.to("health","info","prometheus")).permitAll()
						.requestMatchers(EndpointRequest.toAnyEndpoint().excluding("health","info")).hasAnyAuthority("ROLE_admin")
						.requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
						.requestMatchers(HttpMethod.POST,this.baseUrl+"/auth/logout").authenticated()
						// All other request is to be authenticated
						.anyRequest().authenticated())
				.headers(headers -> headers.frameOptions().disable()).csrf(csrf -> csrf.disable()).cors(Customizer.withDefaults())
				.oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer.jwt().and()
						.authenticationEntryPoint(customBearerTokenAuthenticationEntryPoint)
						.accessDeniedHandler(this.customBearerTokenAccessDeniedException))
				.sessionManagement(
						sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.build();
	}

	@Bean
	@Traced("SecurityConfiguration.passwordEncoder")
	@Logged
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}

	@Bean
	@Traced("SecurityConfiguration.jwtEncoder")
	@Logged
	JwtEncoder jwtEncoder() {
		JWK jwk = new RSAKey.Builder(this.publicKey).privateKey(this.privateKey).build();
		JWKSource<SecurityContext> jwkSet = new ImmutableJWKSet<>(new JWKSet(jwk));
		return new NimbusJwtEncoder(jwkSet);
	}

	@Bean
	@Traced("SecurityConfiguration.jwtDecoder")
	@Logged
	JwtDecoder jwtDecoder() {
		return NimbusJwtDecoder.withPublicKey(this.publicKey).build();
	}

	@Bean
	@Traced("SecurityConfiguration.jwtAuthenticationConverter")
	@Logged
	JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
		jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
		jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
		JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
		jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
		return jwtAuthenticationConverter;
	}

}
