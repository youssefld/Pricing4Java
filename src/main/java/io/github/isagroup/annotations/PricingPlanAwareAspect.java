package io.github.isagroup.annotations;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.samples.petclinic.configuration.services.UserDetailsImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.github.isagroup.exceptions.PricingPlanEvaluationException;

@Aspect
@Component
public class PricingPlanAwareAspect {

    @Around("@annotation(pricingPlanAware)")
    public Object validatePricingPlan(ProceedingJoinPoint joinPoint, PricingPlanAware pricingPlanAware) throws Throwable {
        String featureId = pricingPlanAware.featureId();
        
        // Realizar la evaluación del contexto utilizando el valor de "featureId"
        boolean contextEvaluation = evaluateContext(featureId);
        
        if (!contextEvaluation) {
            throw new PricingPlanEvaluationException("Context evaluation failed for featureId: " + featureId);
        }

        return joinPoint.proceed();
    }

    private boolean evaluateContext(String featureId) {
        try {
			String jwt = parseJwt(request);
			String newToken = null;
			if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
				Authentication userAuth = SecurityContextHolder.getContext().getAuthentication();

				Object userAuthorities = new HashMap<>();

				if (!(userAuth.getPrincipal() instanceof String)) {
					UserDetailsImpl userDetails = (UserDetailsImpl) userAuth.getPrincipal();
					userAuthorities = userDetails.getAuthorities().stream().map(auth -> auth.getAuthority())
							.collect(Collectors.toList());
				}

				Map<String, Object> userContext = userService.findUserContext();
				Plan userPlan = userService.findUserPlan();
				ParserPlan planParser = planService.findPlanParserById(1);
				
				Map<String, Object> planContext = new HashMap<>();

				if(userPlan != null){
					planContext = userPlan.parseToMap();
				}

				PricingEvaluatorUtil util = new PricingEvaluatorUtil(planContext,
						planParser.parseToMap(), userContext, userAuthorities, jwtSecret);

				util.addExpressionToToken("maxVisitsPerMonthAndPet", "userContext['pets'] < planContext['maxPets']");

				newToken = util.generateUserToken();

				String newTokenFeatures = jwtUtils.getFeaturesFromJwtToken(newToken);
				String jwtFeatures = jwtUtils.getFeaturesFromJwtToken(jwt);

				System.out.println("New token features: " + newTokenFeatures);
				System.out.println("Old token features: " + jwtFeatures);

				if (!newTokenFeatures.equals(jwtFeatures)) {
					response.addHeader("New-Token", newToken);
				}
			}
		} catch (Exception e) {
			logger.error("Cannot set user authentication: {}", e);
			logger.info("Anonymous user logged");
		}
    }
}
