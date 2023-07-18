package io.github.isagroup.annotations;

import java.util.List;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.isagroup.PricingContext;
import io.github.isagroup.exceptions.PricingPlanEvaluationException;
import io.github.isagroup.models.Evaluator;
import io.github.isagroup.models.Feature;
import io.github.isagroup.models.Plan;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.services.yaml.YamlUtils;

@Aspect
@Component
public class PricingPlanAwareAspect {
    
    @Autowired
    private PricingContext pricingContext;

    @Around("@annotation(pricingPlanAware)")
    public Object validatePricingPlan(ProceedingJoinPoint joinPoint, PricingPlanAware pricingPlanAware) throws Throwable {
        String featureId = pricingPlanAware.featureId();
        
        // Realizar la evaluación del contexto utilizando el valor de "featureId"
        boolean contextEvaluation = false;
        
        if (!contextEvaluation) {
            throw new PricingPlanEvaluationException("Context evaluation failed for featureId: " + featureId);
        }

        return joinPoint.proceed();
    }

    private boolean evaluateContext() {
        
        Map<String, Object> userContext = pricingContext.getUserContext();
        String userPlan = pricingContext.getUserPlan();
        String configFilePath = pricingContext.getConfigFilePath();

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(configFilePath);

        Plan plan = pricingManager.getPlans().get(userPlan);
        Map<String, Feature> features = plan.getFeatures();
        Map<String, Evaluator> evaluators = pricingManager.getEvaluators();


        
        return false;
    }
}