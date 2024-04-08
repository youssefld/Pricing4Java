package io.github.isagroup.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.beans.factory.annotation.Value;

import io.github.isagroup.PricingContext;
import io.github.isagroup.PricingEvaluatorUtil;
import io.github.isagroup.services.jwt.JwtUtils;

import java.util.Map;

public class RenewTokenFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtils jwtUtils;

	@Value("${petclinic.app.jwtSecret}")
	private String jwtSecret;

	@Autowired
	private PricingEvaluatorUtil pricingEvaluatorUtil;

	@Autowired
	private PricingContext pricingContext;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		try {
			String pricingJwt = parsePricingJwt(request);
			String authJwt = parseAuthJwt(request);

			if (authJwt != null && jwtUtils.validateJwtToken(authJwt) && pricingContext.userAffectedByPricing()) {
				
				String newToken = pricingEvaluatorUtil.generateUserToken();

				Map<String, Map<String, Object>> newTokenFeatures = jwtUtils.getFeaturesFromJwtToken(newToken);
				Map<String, Map<String, Object>> jwtFeatures = jwtUtils.getFeaturesFromJwtToken(pricingJwt);

				String newTokenFeaturesString = "";
				String jwtFeaturesString = "";

				if (newTokenFeatures != null) newTokenFeaturesString = newTokenFeatures.toString();
				
				if (jwtFeatures != null) jwtFeaturesString = jwtFeatures.toString();
				
				if (!newTokenFeaturesString.equals(jwtFeaturesString)) {
					response.addHeader("Pricing-Token", newToken);
				}
			}
		} catch (Exception e) {
			logger.error("Cannot set user authentication: {}", e);
			logger.info("Anonymous user logged");
		}

		filterChain.doFilter(request, response);
	}

	private String parsePricingJwt(HttpServletRequest request) {
		String headerPricing = request.getHeader("Pricing-Token");

		if (StringUtils.hasText(headerPricing)) {
			return headerPricing;
		}

		return null;
	}

	private String parseAuthJwt(HttpServletRequest request) {
		String headerAuth = request.getHeader("Authorization");

		if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
			return headerAuth.substring(7, headerAuth.length());
		}

		return null;
	}

}

