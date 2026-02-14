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

@Configuration
public class SecurityConfiguration {

	private final RSAPublicKey publicKey;
	private final RSAPrivateKey privateKey;

	private final CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint;
	private final CustomBearerTokenAuthenticationEntryPoint customBearerTokenAuthenticationEntryPoint;
	private final CustomBearerTokenAccessDeniedException customBearerTokenAccessDeniedException;

	public SecurityConfiguration(CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint,
			CustomBearerTokenAuthenticationEntryPoint customBearerTokenAuthenticationEntryPoint,
			CustomBearerTokenAccessDeniedException customBearerTokenAccessDeniedException)
			throws NoSuchAlgorithmException {
		super();
		this.customBasicAuthenticationEntryPoint = customBasicAuthenticationEntryPoint;
		this.customBearerTokenAuthenticationEntryPoint = customBearerTokenAuthenticationEntryPoint;
		this.customBearerTokenAccessDeniedException = customBearerTokenAccessDeniedException;
		// Generate a public/Private key pair
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);// Generated key will have size of 2048 bits
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		this.publicKey = (RSAPublicKey) keyPair.getPublic();
		this.privateKey = (RSAPrivateKey) keyPair.getPrivate();

	}

	@Value("${api.endpoint.base-url}")
	private String baseUrl;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.authorizeHttpRequests(authorizeHttpRequest -> authorizeHttpRequest
						.requestMatchers(HttpMethod.GET, this.baseUrl + "/artifacts/**").permitAll()
						.requestMatchers(HttpMethod.GET, this.baseUrl + "/users/**").hasAuthority("ROLE_admin")// Protecting
																												// this
																												// end
																												// point
						.requestMatchers(HttpMethod.POST, this.baseUrl + "/users").hasAuthority("ROLE_admin")
						.requestMatchers(HttpMethod.PUT, this.baseUrl + "/users/**").hasAuthority("ROLE_admin")
						.requestMatchers(HttpMethod.DELETE, this.baseUrl + "/users/**").hasAuthority("ROLE_admin")
						.requestMatchers(
				                "/v3/api-docs/**",
				                "/swagger-ui/**",
				                "/swagger-ui.html"
				        ).permitAll()
						.requestMatchers(EndpointRequest.to("health","info","prometheus")).permitAll()
						.requestMatchers(EndpointRequest.toAnyEndpoint().excluding("health","info")).hasAnyAuthority("ROLE_admin")
						.requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
						// All other request is to be authenticated
						.anyRequest().authenticated())
				.headers(headers -> headers.frameOptions().disable()).csrf(csrf -> csrf.disable()).cors(Customizer.withDefaults())
				.httpBasic(httpBasic -> httpBasic.authenticationEntryPoint(this.customBasicAuthenticationEntryPoint))
				.oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer.jwt().and()
						.authenticationEntryPoint(customBearerTokenAuthenticationEntryPoint)
						.accessDeniedHandler(this.customBearerTokenAccessDeniedException))
				.sessionManagement(
						sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}

	@Bean
	JwtEncoder jwtEncoder() {
		JWK jwk = new RSAKey.Builder(this.publicKey).privateKey(this.privateKey).build();
		JWKSource<SecurityContext> jwkSet = new ImmutableJWKSet<>(new JWKSet(jwk));
		return new NimbusJwtEncoder(jwkSet);
	}

	@Bean
	JwtDecoder jwtDecoder() {
		return NimbusJwtDecoder.withPublicKey(this.publicKey).build();
	}

	@Bean
	JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
		jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
		jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
		JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
		jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
		return jwtAuthenticationConverter;
	}

}
