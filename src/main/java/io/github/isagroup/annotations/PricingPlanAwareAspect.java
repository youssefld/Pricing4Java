package io.github.isagroup.annotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.github.isagroup.PricingContext;
import io.github.isagroup.exceptions.PricingPlanEvaluationException;
import io.github.isagroup.models.Evaluator;
import io.github.isagroup.models.PlanContextManager;

@Aspect
@Component
public class PricingPlanAwareAspect {
    
    @Autowired
    private PricingContext pricingContext;

    @Around("@annotation(pricingPlanAware)")
    @Transactional
    public Object validatePricingPlan(ProceedingJoinPoint joinPoint, PricingPlanAware pricingPlanAware) throws Throwable {
        
        Object proceed = joinPoint.proceed();
        
        String featureId = pricingPlanAware.featureId();
        
        // Realizar la evaluación del contexto utilizando el valor de "featureId"
        Boolean contextEvaluation = evaluateContext(featureId);
        
        if (!contextEvaluation) {
            throw new RuntimeException("Context evaluation failed for featureId: " + featureId);
        }

        return proceed;
    }

    private Boolean evaluateContext(String featureId) {

        Map<String, Object> userContext = pricingContext.getUserContext();
        Map<String, Object> planContext = pricingContext.getPlanContext();
        Map<String, Evaluator> evaluationContext = pricingContext.getEvaluators();

        PlanContextManager planContextManager = new PlanContextManager();

        planContextManager.userContext = userContext;
        planContextManager.planContext = planContext;

        String expression = evaluationContext.get(featureId).getExpression();

        if (!expression.trim().equals("")) {

            ExpressionParser parser = new SpelExpressionParser();
            EvaluationContext context = SimpleEvaluationContext.forReadOnlyDataBinding().build();

            return parser.parseExpression(expression).getValue(context, planContextManager, Boolean.class);
        }else{
            return false;
        }

    }
}