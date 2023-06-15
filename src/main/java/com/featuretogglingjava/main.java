package com.featuretogglingjava;

import java.util.Map;
import java.util.HashMap;

public class main {

    public static void main(String[] args) {

        Map<String, String> userAuthorities = new HashMap<>();
        userAuthorities.put("role", "admin");
        userAuthorities.put("username", "admin1");
        userAuthorities.put("password", "4dm1n");

        Map<String, Object> userContext = new HashMap<>();
        userContext.put("pets", 2);
        userContext.put("haveVetSelection", true);
        userContext.put("haveCalendar", true);
        userContext.put("havePetsDashboard", true);
        userContext.put("haveOnlineConsultations", true);

        FeatureTogglingUtil togglingUtil = new FeatureTogglingUtil("src/main/java/com/featuretogglingjava/plans.json", "src/main/java/com/featuretogglingjava/plansParser.json", userContext, "secret", userAuthorities);

        String token = togglingUtil.generateUserToken();

        System.out.println(token);
    }
}
