package io.github.isagroup;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.isagroup.annotations.PricingPlanAware;
import io.github.isagroup.annotations.PricingPlanAwareAspect;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Import({io.github.isagroup.annotations.PricingPlanAwareAspect.class})
public class PricingPlanAwareTests {

    private static final String JWT_SECRET_TEST = "secret";
    private static final Integer JWT_EXPIRATION_TEST = 86400;
    private static final String JWT_SUBJECT_TEST = "admin1";
    
    @Configuration
    public static class TestConfiguration {
        
        @Component
        public class PricingContextImpl extends PricingContext {
            
            @Override
            public String getConfigFilePath(){
                return "pricing/models.yml";
            };
            
            @Override
            public String getJwtSecret(){
                return JWT_SECRET_TEST;
            };
            
            @Override
            public int getJwtExpiration(){
                return JWT_EXPIRATION_TEST;
            };

            @Override
            public Map<String, Object> getUserContext() {
                Map<String, Object> userContext = new HashMap<>();
                
                userContext.put("username", JWT_SUBJECT_TEST);
                userContext.put("pets", 200);
                userContext.put("haveVetSelection", true);
                userContext.put("haveCalendar", true);
                userContext.put("havePetsDashboard", true);
                userContext.put("haveOnlineConsultations", true);
                
                return userContext;
            }
        
            @Override
            public String getUserPlan() {
                return "ADVANCED";
            }
        
            @Override
            public Object getUserAuthorities() {
                Map<String, String> userAuthorities = new HashMap<>();
                userAuthorities.put("role", "admin");
                userAuthorities.put("username", "admin1");
                userAuthorities.put("password", "4dm1n");

                return userAuthorities;
            }

        }
        
    }

    @Autowired
    private PricingPlanAwareAspect pricingPlanAwareAspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    void test1() throws Throwable{
        System.out.println("Test 1");
        Object testObject = new Object();

        Mockito.when(joinPoint.proceed()).thenReturn("Result");

        // Obtener el valor del parámetro featureId que deseas probar
        String featureId = "haveVetSelection";

        pricingPlanAwareAspect.validatePricingPlan(joinPoint, new PricingPlanAware() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return PricingPlanAware.class;
            }

            @Override
            public String featureId() {
                return featureId;
            }
        });
    }

}
