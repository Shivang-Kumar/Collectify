package edu.tcu.cs.hogwarts_artifacts_online.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import edu.tcu.cs.hogwarts_artifacts_online.user.MyUserPrincipal;

@Component
public class JWTProvider {
	private final JwtEncoder jwtEncoder;
	

	public JWTProvider(JwtEncoder jwtEncoder) {
		super();
		this.jwtEncoder = jwtEncoder;
	}


	public String createToken(Authentication authentication) {
		Instant now=Instant.now();
		long expiresIn=2; // 2 hours
		//Prepare a claim called authorities
		String authorities=authentication.getAuthorities().stream().map(grantedAuthority -> grantedAuthority.getAuthority()).collect(Collectors.joining(" ")); //Must be space-delimited
		JwtClaimsSet claims=JwtClaimsSet.builder()
		.issuer("self")
		.issuedAt(now)
		.expiresAt(now.plus(expiresIn,ChronoUnit.HOURS))
		.subject(authentication.getName())
		.claim("userId", ((MyUserPrincipal)authentication.getPrincipal()).getUser().getId())
		.claim("authorities", authorities)
		.build();
		
		
		return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
	}

}
