package io.github.isagroup;

import java.util.Map;

import io.github.isagroup.annotations.PricingPlanAware;

import java.util.HashMap;

public class main {

    public static void main(String[] args) {

        System.out.println(buildToken());

    }

    private static String buildToken(){
        Map<String, String> userAuthorities = new HashMap<>();
        userAuthorities.put("role", "admin");
        userAuthorities.put("username", "admin1");
        userAuthorities.put("password", "4dm1n");

        Map<String, Object> userContext = new HashMap<>();
        userContext.put("username", "admin1");
        userContext.put("pets", 2);
        userContext.put("haveVetSelection", true);
        userContext.put("haveCalendar", true);
        userContext.put("havePetsDashboard", true);
        userContext.put("haveOnlineConsultations", true);

        Map<String, Object> planContext = new HashMap<>();
        planContext.put("maxPets", 6);
        planContext.put("maxVisitsPerMonthAndPet", 2);
        planContext.put("supportPriority", "HIGH");
        planContext.put("haveCalendar", true);
        planContext.put("havePetsDashboard", true);
        planContext.put("haveVetSelection", true);
        planContext.put("haveOnlineConsultation", true);
        
        Map<String, String> evaluationContext = new HashMap<>();
        evaluationContext.put("maxPets", "userContext['pets'] < planContext['maxPets']");
        evaluationContext.put("maxVisitsPerMonthAndPet", "");
        evaluationContext.put("supportPriority", "");
        evaluationContext.put("haveCalendar", "planContext['haveVetSelection']");
        evaluationContext.put("havePetsDashboard", "planContext['haveCalendar']");
        evaluationContext.put("haveVetSelection", "planContext['havePetsDashboard']");
        evaluationContext.put("haveOnlineConsultation", "planContext['haveOnlineConsultations']");

        PricingEvaluatorUtil togglingUtil = new PricingEvaluatorUtil(planContext, evaluationContext, userContext, userAuthorities, "secret", 86400);

        togglingUtil.addExpressionToToken("maxVisitsPerMonthAndPets", "userContext['pets'] < planContext['maxPets']");

        String token = togglingUtil.generateUserToken();

        return token;
    }
}
