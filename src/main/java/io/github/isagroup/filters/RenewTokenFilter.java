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
			String jwt = parseJwt(request);

			if (jwt != null && jwtUtils.validateJwtToken(jwt) && pricingContext.userAffectedByPricing()) {
				
				String newToken = pricingEvaluatorUtil.generateUserToken();

				String newTokenFeatures = jwtUtils.getFeaturesFromJwtToken(newToken).toString();
				String jwtFeatures = jwtUtils.getFeaturesFromJwtToken(jwt).toString();

				if (!newTokenFeatures.equals(jwtFeatures)) {
					response.addHeader("New-Token", newToken);
				}
			}
		} catch (Exception e) {
			logger.error("Cannot set user authentication: {}", e);
			logger.info("Anonymous user logged");
		}

		filterChain.doFilter(request, response);
	}

	private String parseJwt(HttpServletRequest request) {
		String headerAuth = request.getHeader("Authorization");

		if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
			return headerAuth.substring(7, headerAuth.length());
		}

		return null;
	}

}

