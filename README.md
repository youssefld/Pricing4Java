# pricingplans-4j

The aim of this package is to provide a simple and easy to use tools that allow users to set a pricing configuration on the spring server side of an app automatically. It also can evaluate the context of an user driven by pricing features and wrap the results inside a JWT. The package have been designed to be used with [pricingplans-react](https://github.com/Alex-GF/pricingplans-react), the frontend library that consumes the JWT and toggles on and off functionalities based on the user pricing plan.

## Installation

The package have been build to be used with maven. To install it, just add the following dependencies to your pom.xml file:

```xml
<dependencies>
    
    ...

    <!-- PRICINGPLANS-4J -->

    <dependency>
        <groupId>io.github.isa-group</groupId>
        <artifactId>pricingplans-4j</artifactId>
        <version>{version}</version>
    </dependency>

    ...

</dependencies>
```

## Pricing Configuration

The packages uses a YAML file to represent all the pricing configuration, which includes: plans specifications, features used, values of these features for each plan… 

The file must be placed inside the resources folder of the project, and must have the structure of this example:

```yaml
features:
  feature1:
    description: feature1 description
    expression: # SPEL expression
    type: NUMERIC # The value of this field can be NUMERIC, TEXT or CONDITION
    defaultValue: 2
  feature2:
    description: feature2 description
    expression: # SPEL expression
    type: CONDITION
    defaultValue: true
  feature3:
    description: feature3 description
    expression: '' # This feature will be evaluated as false by default
    type: TEXT
    defaultValue: LOW
  # ...
plans:
  BASIC:
    description: Basic plan
    price: 0.0
    currency: EUR
    features:
      feature1:
        value: null
      feature2:
        value: false
      feature3:
        value: null
  PRO:
    description: Pro plan
    price: 12.0
    currency: EUR
    features:
      feature1:
        value: 6
      feature2:
        value: null
      feature3:
        value: HIGH
```

Important notes to have in mind while configuring the YAML:

- The `features` section must contain all the features that are going to be used in the app. Each feature must have a `description`, a `type` and a `defaultValue` and a `expression`. 

  The `type` can be `NUMERIC` (handles Integer, Double, Long…), `TEXT` (handles String) or `CONDITION` (handles Boolean). 

  The `defaultValue` must be a value supported by the type of the feature:
  
  - If the `type` is `CONDITION`, the `defaultValue` must be a boolean. 
  - If the `type` is `NUMERIC`, the `defaultValue` must be: integer, double, long… 
  - If the `type` is `TEXT`, the `defaultValue` must be a string. 

  The `expression` must be a string `SPEL` expression that evaluates the value of the feature. It can access the data of the user context using the `userContext` variable, while the plan's is available through `planContext`. For example, considering a user context that contains the following information:

  ```json
  {
    "username": "John",
    "feature1use": 2,
  }
  ```

  If we want to check if the use of the feature exceeds its limit, the `SPEL` expression should be:

  ```yaml
  # ...
  feature1:
    # ...
    expression: userContext['feature1use'] <= planContext['feature1']
    # ...
  ```

- Each feature inside a plan must have a name that match with one of the declared in the `features` section. Each of this features must only contains a `value` attribute of a type supported by the feature. The `value` attribute can also can be set to `null` if you want the library to consider the `defaultValue` as the value of the field.

  The library will automatically add the rest of the attributes when parsing YAML to PricingManager.

## Java objects to manage pricing

The package provides a set of java objects that model the YAML configuration. These objects can be used to access information about the pricing all over the app.

### PricingManager

This class is the main object of the package. It contains all the information about the pricing configuration and can be used to evaluate the context of an user and generate a JWT with the results.

```java
public class PricingManager {
    public Map<String, Plan> plans;
    public Map<String, Feature> features;

    // Getters and setters...
}
```

The name of each plan and feature is used as a key to access the information of the object. For example, to access the price of the plan `BASIC` we can use:

```java
pricingManager.getPlans().get("BASIC").getPrice();
```

### Plan

This class models the information of a plan. It contains the name, description, price and currency of the plan, as well as a map of the features used by the plan.

```java
public class Plan {
    public String description;
    public Double price;
    public String currency;
    public Map<String, Feature> features;

    // Getters and setters...

    // toString()
}
```

### Feature

This class models the information of a feature.

```java
public class Feature {
    public String description;
    public FeatureType type;
    public Object defaultValue;
    public Object value;
    public String expression;

    // Getters and setters...

    // toString()
}
```

The class also includes a method called `prepareToPlanWriting()`. It is used to prepare the object to be written inside a plan in the YAML file by removing the setting the value of all the attributes to `null`, except `value`.

## Usage

The package provides up to three different main classes to manage pricing inside our application.

### PricingContext

This abstract class is the key to manage the YAML configuration inside a spring app. It provides a set of configurable methods that need to be implemented inside a new component that extends this class to use other classes of the package. Inside your spring project, create the following component:

```java

import io.github.isagroup.PricingContext;

@Component
public class PricingConfiguration extends PricingContext {

    @Override
    public String getJwtSecret(){
        // This method must return the JWT secret that should be used to create tokens
    }

    @Override
    public String getConfigFilePath(){
        // This method must return the configuration file path relative to the resources folder
    }

    @Override
    public Object getUserAuthorities() {
        // This method should return the object used inside the application to determine the authority of the user inside the JWT.
    }

    @Override
    public Map<String, Object> getUserContext() {
        // This method should return the user context that will be used to evaluate the pricing plan.
        // It should be considered which users has accessed the service and what information is available.
    }

    @Override
    public String getUserPlan() {
        // This method should return the plan name of the current user.
        // With this information, the library will be able to build the Plan object of the user from the configuration.
    }
    
}

```

By creating this component inside your project, spring will be able to use this information wherever it is needed.

The class also provides a set of methods that can be used to retrieve information about the pricing configuration anywhere in the app. By injecting the component in any class, the following methods can be used:

- **getPlanContext**: Returns a Map<String, Plan> that represents the plan context that is going to be evaluated.

- **getFeatures**: Returns the features declared on the pricing configuration.

- **getPricingManager**: Maps the information of the YAML configuration file to a PricingManager object to easily operate with pricing properties.

### PricingEvaluatorUtil

It can be used to evaluate the context of an user compared to his plan and generate a JWT with the results, using a single java method. This class consumes the information of the configured PricingContext to perform its operations.

Once a class that extends from PricingContext exists inside the spring app, PricingEvaluatorUtil can be injected in any bean by using @Autowired. Once declared, the token can be generated using the `generateUserToken` method anywhere. It requires no parameters and returns a `String` with the JWT token. This is an example:

```java
import io.github.isagroup.PricingEvaluatorUtil;

@Component
public class MyComponent {

    @Autowired
    private PricingEvaluatorUtil pricingEvaluatorUtil;

    public String myMethod() {
        String token = pricingEvaluatorUtil.generateUserToken();
        return token;
    }
}
```

The class also contains a method that modifies a given JWT by changing the evaluation of the given feature by a String expression that will be evaluated on the client side of the application and returns the new version. The following snippet is an example of this method:

```java
// ...

String firstToken = pricingEvaluatorUtil.generateUserToken();
String newToken = pricingEvaluatorUtil.addExpressionToToken(firstToken, "feature1", "userContext['feature1use'] < planContext['feature1']");

Map<String, Map<String, Object>> features = jwtUtils.getFeaturesFromJwtToken(newToken);

// ...
```

Considering just two NUMERIC features, this function could have generated a JWT that has the following payload:

```
{
  "features": {
    "feature1": {
      "eval": "userContext['feature1use'] < planContext['feature1']",
      "limit": 2,
      "used": 2
    },
    "feature2": {
      "eval": true,
      "limit": 5,
      "used": 1
    },
  },
  "sub": "admin1",
  "exp": 1687705951,
  "userContext": {
    ...
  },
  "iat": 1687705864,
  "authorities": {
    "password": "4dm1n",
    "role": "admin",
    "username": "admin1"
  },
  "planContext": {
    ...
  }
}
```

### PricingService

This class offers a set of methods that can be used to manage the pricing configuration without manually modifying the YAML file. It can be used to retrieve, add, remove or modify plans and features.

| **Method**                                                                  | **Description**                                                                                                                                                                                                                                                                  |
|-----------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Plan getPlanFromName(String planName)                                       | Returns the plan of the configuration that matchs the given name.                                                                                                                                                                                                                |
| void addPlanToConfiguration(String name, Plan plan)                         | Adds a new plan to the current pricing configuration. The plan must not exist and must contain all the  features declared on the configuration. It is recommended  to use the PricingContext.getFeatures() method to get the  list of features that appear in the configuration. |
| void addFeatureToConfiguration(String name, Feature feature)                | Creates a new global feature in the pricing configuration and adds  it to all the plans using its default value.                                                                                                                                                                 |
| void setPlanFeatureValue(String planName, String featureName, Object value) | Modifies a plan's feature value. In order to do that, the plan  must exist in the PricingContext that is being used. A feature  with the given feature name must also exist.                                                                                                     |
| void setPlanPrice(String planName, Double newPrice)                         | Modifies a plan's price. In order to do that, the plan must exist  in the PricingContext that is being used.                                                                                                                                                                     |
| void setFeatureExpression(String featureName, String expression)            | Modifies a feature's expression. In order to do that, the feature  must exist in the PricingContext that is being used.                                                                                                                                                          |
| void setFeatureType(String featureName, FeatureType newType)                | Modifies a feature's type. In order to do that, the feature must  exist in the PricingContext that is being used.                                                                                                                                                                |
| void removePlanFromConfiguration(String name)                               | Removes a plan from the pricing configuration. In order to do that,  it must exist in the PricingContext that is being used.                                                                                                                                                     |
| void removeFeatureFromConfiguration(String name)                            | Removes a feature from the pricing configuration. In order to do  that, it must exist in the PricingContext that is being used. The  method also removes the feature from all the plans that include it.                                                                         |

As any other spring service, to use this class it must be injected in any bean using @Autowired. Once declared, the methods can be used to manage the pricing configuration.