package io.github.isagroup.services.jwt;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.isagroup.PricingContext;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

@Component
public class JwtUtils {

	@Autowired
	private PricingContext pricingContext;

	private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

	public String getSubjectFromJwtToken(String token) {
		return Jwts.parser().setSigningKey(pricingContext.getJwtSecret()).parseClaimsJws(token).getBody().getSubject();
	}

	public String generateTokenFromUsername(String username) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("authorities", pricingContext.getUserAuthorities());
		return Jwts.builder().setClaims(claims).setSubject(username).setIssuedAt(new Date())
				.setExpiration(new Date((new Date()).getTime() + pricingContext.getJwtExpiration()))
				.signWith(SignatureAlgorithm.HS512, pricingContext.getJwtSecret()).compact();
	}

	public Map<String, Map<String, Object>> getFeaturesFromJwtToken(String token) {
		return (Map<String, Map<String, Object>>) Jwts.parser().setSigningKey(pricingContext.getJwtSecret()).parseClaimsJws(token).getBody().get("features");
	}

	public Map<String, Object> getPlanContextFromJwtToken(String token) {
		return (Map<String, Object>) Jwts.parser().setSigningKey(pricingContext.getJwtSecret()).parseClaimsJws(token).getBody().get("planContext");
	}

	public Map<String, Object> getUserContextFromJwtToken(String token) {
		return (Map<String, Object>) Jwts.parser().setSigningKey(pricingContext.getJwtSecret()).parseClaimsJws(token).getBody().get("userContext");
	}

	public String getUserNameFromJwtToken(String token) {
		return Jwts.parser().setSigningKey(pricingContext.getJwtSecret()).parseClaimsJws(token).getBody().getSubject();
	}

	public boolean validateJwtToken(String authToken) {
		try {
			Jwts.parser().setSigningKey(pricingContext.getJwtSecret()).parseClaimsJws(authToken);
			return true;
		} catch (SignatureException e) {
			logger.error("Invalid JWT signature: {}", e.getMessage());
		} catch (MalformedJwtException e) {
			logger.error("Invalid JWT token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			logger.error("JWT token is expired: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			logger.error("JWT token is unsupported: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("JWT claims string is empty: {}", e.getMessage());
		}

		return false;
	}
}
