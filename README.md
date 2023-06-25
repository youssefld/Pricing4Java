# pricingplans-4j

The aim of this package is to provide a simple and easy to use java class that evaluates the context of an user driven by pricing features and wraps the results inside a JWT token. The package have been designed to be used with [pricingplans-react](https://github.com/Alex-GF/pricingplans-react), the frontend library that consumes the JWT token and toggles on and off functionalities based on the user pricing plan.

## Installation

The package have been build to be used with maven. To install it, just add the following dependencies to your pom.xml file:

```xml
<dependencies>
    
    ...

    <!-- PRICINGPLANS-4J -->

    <dependency>
        <groupId>es.us.isagroup</groupId>
        <artifactId>pricingplans-4j</artifactId>
        <version>1.0.0</version>
    </dependency>

    ...

</dependencies>
```

## Usage

The package provides a simple class called PricingEvaluatorUtil that can be used to evaluate the context of an user compared to his plan and generate a JWT token with the results, using a single java method. The class constructor requires the following parameters:

- **(REQUIRED) planContext**: A `Map<String, Object>` that contains the context of the plan. The keys must be the `featureId` of each feature (i.e myFeature) and the values could be integers, strings or booleans.

- **(REQUIRED) evaluationConext**: A `Map<String, Object>` that contains the evaluation context. The keys must be the `featureId` of each feature (i.e myFeature) and the values must be strings in `SPEL` format. The `SPEL` expressions can access the data of the user context using the `userContext` variable, while the plan's is available through `planContext`. 

- **(REQUIRED) userContext**: A `Map<String, Object>` that contains the context of the user. The keys don't have to be the same as `planContext` and `evaluationContext`, they are only needed inside the `SPEL` expressions. The values could be integers, strings or booleans.

For example, if we have the following maps that represent the user and plan contexts:

```java
Map<String, Object> userContext = new HashMap<>();
userContext.put("feature1use", 2);
userContext.put("feature2", true);

Map<String, Object> planContext = new HashMap<>();
planContext.put("feature1", 5);
planContext.put("feature2", false);
```

Then, if we want to check that the `feature1` user context does not exceeds the limit of the plan, the `SPEL` expression to evaluate the context could be:

```java
Map<String, Object> evaluationContext = new HashMap<>();
evaluationContext.put("feature1", "userContext['feature1use'] <= planContext['feature1']");
```

It is important to note that the keys of the `evaluationContext` map must be the same as the keys of the `planContext`. This is because the class will use them as the matching criteria between the evaluation to apply and the plan feature.

- **(REQUIRED) userAuthorities**: An `Object` that represents the authorities of the user inside the aplication. Depending on which framework is being used to create the API (i.e Spring Security), the type of this object could be different. The class will only use this object to include its content inside the token's body.

- **jwtSecret**: A `String` that represents the secret with which the token will be signed. If this parameter is not provided, the class will set it to a default value: `secret`. **Not providing a secret for the JWT token introduces a major vulnerability to the system. The use of default secret is strongly discouraged**.

- **jwtExpirationMs**: An `int` that represents the expiration time of the token in miliseconds. If this parameter is not provided, the class will set it to a default value: `86400000` (1 day).

## Example

In this section we provide a simple code snippet on which the class is used to generate a JWT token based on a pricing template made for `spring petclinic`.

```java
import java.util.Map;
import java.util.HashMap;

public class main {

    public static void main(String[] args) {

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

        String token = togglingUtil.generateUserToken();

        System.out.println(token);
    }
}

```

This function generates a JWT token that has the follogin payload:

```
{
  "features": {
    "haveCalendar": {
      "eval": true,
      "limit": null,
      "used": null
    },
    "haveVetSelection": {
      "eval": true,
      "limit": null,
      "used": null
    },
    "haveOnlineConsultation": {
      "eval": null,
      "limit": null,
      "used": null
    },
    "maxPets": {
      "eval": true,
      "limit": 6,
      "used": 2
    },
    "havePetsDashboard": {
      "eval": true,
      "limit": null,
      "used": null
    },
    "maxVisitsPerMonthAndPet": {
      "eval": true,
      "limit": null,
      "used": null
    },
    "supportPriority": {
      "eval": true,
      "limit": null,
      "used": null
    }
  },
  "sub": "admin1",
  "exp": 1687705951,
  "iat": 1687705864,
  "authorities": {
    "password": "4dm1n",
    "role": "admin",
    "username": "admin1"
  }
}
```