# pricingplans-4j

The aim of this package is to provide a simple and easy to use tools that allow users to set a pricing configuration on the spring server side of an app automatically. It also can evaluate the context of an user driven by pricing features and wrap the results inside a JWT. The package have been designed to be used with [pricingplans-react](https://github.com/isa-group/pricingplans-react.git), the frontend library that consumes the JWT and toggles on and off functionalities based on the user pricing plan.

Right now, the library is in a very early stage of development, so it is not recommended to use it in production environments. However, it is possible to use it in demo apps to test its functionalities. 

The only feature that is implemented right now and tested is the Yaml4SaaS syntax validator.

## Index

- [Yaml4SaaS](#yaml4saas)
- [Yaml4SaaS Syntax Validator Usage](#yaml4saas-syntax-validator-usage)

## Yaml4SaaS

Yaml4SaaS emerges as a pragmatic application of the Pricing4SaaS model (Figure \ref{fig:yaml4SaaS}), aligning with the overarching objective of formalizing and structuring pricing information for SaaS platforms. Building upon the foundational principles articulated in Pricing4SaaS, Yaml4SaaS embodies a simplified and versatile YAML-based syntax designed for serializing comprehensive details about SaaS offerings. The essence of Yaml4SaaS lies in its capacity to encapsulate pricing plans, add-ons, features and usage limits within a concise and human-readable YAML format. Here is a tempalte specification of the Yaml4SaaS syntax:

```yaml
saasName: GitHub
day: 15
month: 11
year: 2023
currency: USD
hasAnnualPayment: true
features:
    githubPackages:
        description: ...
        valueType: BOOLEAN
        defaultValue: true
        type: DOMAIN
    standardSupport:
        description: ...
        valueType: BOOLEAN
        defaultValue: false
        type: SUPPORT
    #...
usageLimits:
    githubPackagesLimit:
        description: ...
        valueType: NUMERIC
        unit: GB
        defaultValue: 0.5
        linkedFeatures:
            - githubPackages
    #...
plans:
    FREE:
        description: ...
        monthlyPrice: 0
        annualPrice: 0
        unit: "user/month"
    TEAM:
        description: ...
        monthlyPrice: 4
        annualPrice: 3.67
        unit: "user/month"
        features: 
            standardSupport:
                value: true
        usageLimits: 
            githubPackagesLimit:
                value: 2
    #...
addOns:
    extraGithubPackages:
        availableFor:
            - FREE
            - TEAM
        price: 0.5
        unit: GB/month
        features: null
        usageLimits: null
        usageLimitsExtensions:
            githubPackagesLimit:
                value: 1
    #...
```

Starting with the top-level placeholder, we can describe basic information about the pricing, features, usage limits, plans and add-ons.

`Features` enumerate all the functionalities encompassed in the pricing, classifying them into the types defined in Pricing4SaaS: 

- INFORMATION
- INTEGRATION
- DOMAIN
- AUTOMATION
- MANAGEMENT
- GUARANTEE
- SUPPORT
- PAYMENT

detailing each feature's `description`, `valueType` (BOOLEAN, TEXT), and `defaultValue`, whose data type has to be aligned with the `valueType` defined. Notably, features do not handle NUMERIC values, which are reserved for limits. In addition, depending on each type of feature, the syntax  extends expressiveness for each feature type with additional fields:

- For **integration** features, an `IntegrationType` (enum defined in Figure \ref{fig:yaml4SaaS}) can be specified through the `integrationType` field. If its value is WEB\_SAAS, a list of SaaS pricing URLs can be included.
- **Automation** features do also allow to assign theirselves an `AutomationType`.
- For **guarantee** features can reference the corresponding documentation section describing them via the `docURL` field.
- **Payment** features differ from others, requiring values as a list of `PaymentTypes` (also detailed in Figure \ref{fig:yaml4SaaS}) for standardization.


Similar to features, `UsageLimits` expounds on limitations affecting plans, add-ons, or features in the pricing, tagging each with the corresponding Pricing4SaaS type:


- NON_RENEWABLE
- RENEWABLE
- RESPONSE_DRIVEN
- TIME_DRIVEN


For each limit, similar to features, a `description`, `valueType` (BOOLEAN, TEXT, NUMERIC), and `defaultValue` are provided, accompanied by additional fields such as `unit` or `linkedFeatures`. The latter must be a list of previously described features affected by the limitation.

The `plans` section provides comprehensive details about the distinct pricing plans offered within the SaaS. Each plan is identified by a unique `name`, allowing for easy reference and differentiation. For each one, essential information is specified, including a brief `description`, the `monthlyPrice`, the `annualPrice` (if different from monthly) and the `unit` affected by them, typically expressed as "user/month".

In the `features` and `usageLimits` subsections of each plan, only those requiring a modification in their `defaultValue` should be explicitly listed. For those not mentioned, the `defaultValue` is understood to be equivalent to the `value`.

Within the `addOns` section, the focus is on delineating the specific details of additional offerings beyond the core plans. Each add-on is characterized by its unique features and usage limits, which have to be listed in the structure established in the `features` and `usageLimits` sections, but not included on plans. Similar to the approach taken in the previous section of the file, only those `features` or `usageLimits` necessitating an alteration in the `defaultValue` are explicitly outlined. As an extra field, add-ons also allow to extent a usageLimit, as can be seen in Figure \ref{fig:pricing4SaaSYAML}. This is extremely powerful for modeling overage cost to some limits.

In conclusion, Yaml4SaaS stands as a practical implementation of the Pricing4SaaS model, providing a YAML-based syntax to formalize SaaS pricing structures in a human-readable format that enhances clarity and simplicity.

## Yaml4SaaS Syntax Validator Usage

The validator can be easyily run by creating a test inside the `saasYamlParsingTest.java` file following the next structure:

```java
@Test
@Order(X)
void parsePostmanYamlToClassTest() {
    PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/{NAME_OF_YOUR_FILE}.yml");
}
```

The test will fail if the YAML file does not correctly follow the Yaml4SaaS syntax, and will throw an exception explaining the problem.

<!-- ## Installation

The package have been build to be used with maven. To install it, just add the following dependencies to your pom.xml file:

```xml
<dependencies>
    
    ...

    <!-- PRICINGPLANS-4J -->
<!--
    <dependency>
        <groupId>io.github.isa-group</groupId>
        <artifactId>pricingplans-4j</artifactId>
        <version>{version}</version>
    </dependency>

    ...

</dependencies>
``` -->

<!-- ## Pricing Configuration

The packages uses a YAML file to represent all the pricing configuration, which includes: plans specifications, features used, values of these features for each plan… 

The file must be placed inside the resources folder of the project, and must have the structure of this example:

```yaml
saasName: GitHub
day: 15
month: 11
year: 2023
currency: USD
hasAnnualPayment: true
features:
    githubPackages:
        description: ...
        valueType: BOOLEAN
        defaultValue: true
        type: DOMAIN
    standardSupport:
        description: ...
        valueType: BOOLEAN
        defaultValue: false
        type: SUPPORT
    #...
usageLimits:
    githubPackagesLimit:
        description: ...
        valueType: NUMERIC
        unit: GB
        defaultValue: 0.5
        linkedFeatures:
            - githubPackages
    #...
plans:
    FREE:
        description: ...
        monthlyPrice: 0
        annualPrice: 0
        unit: "user/month"
    TEAM:
        description: ...
        monthlyPrice: 4
        annualPrice: 3.67
        unit: "user/month"
        features: 
            standardSupport:
                value: true
        usageLimits: 
            githubPackagesLimit:
                value: 2
    #...
addOns:
    extraGithubPackages:
        availableFor:
            - FREE
            - TEAM
        price: 0.5
        unit: GB/month
        features: null
        usageLimits: null
        usageLimitsExtensions:
            githubPackagesLimit:
                value: 1
    #...
``` -->



<!-- Important notes to have in mind while configuring the YAML:

- The `features` section must contain all the features that are going to be used in the app. Each feature must have a `description`, a `valueType`, a `defaultValue`, a `unit` with whom the limit is measured and a list of `linkedFeatures` affected by the limit. 

  The `type` can be `NUMERIC` (handles Integer, Double, Long…), `TEXT` (handles String) or `BOOLEAN` (handles Boolean). 

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

  It's also possible to define a server side evaluation that will be used to evaluate any feature using @PricingPlanAware annotation. This use can be interesting on NUMERIC features, let's see an example.

  If we have a button on the UI to add items to a list, it should be only available while the amount of products is under the feature limit, so when it is reached, the button disapears. The expression that models this behaviour will be the following:

  ```yaml
  # ...
  feature1:
    # ...
    expression: userContext['feature1use'] < planContext['feature1']
    # ...
  ```

  However, on the server side, we should consider that the application has a valid state if the limit is not exceeded, which is evaluated with the following expression:

  ```yaml
  # ...
  feature1:
    # ...
    expression: userContext['feature1use'] <= planContext['feature1']
    # ...
  ```

  To handle this type of situations, features configuration includes an optional `serverExpression` attribute that will be used to evaluate the feature on the server side (when using @PricingPlanAware annotation). If this attribute is not defined, the `expression` will be used instead on any evaluation context. The snippet below shows how to define the situation described above:

  ```yaml
  # ...
  feature1:
    # ...
    expression: userContext['feature1use'] < planContext['feature1']
    serverExpression: userContext['feature1use'] <= planContext['feature1']
    # ...
  ```


- Each feature inside a plan must have a name that match with one of the declared in the `features` section. Each of this features must only contains a `value` attribute of a type supported by the feature. The `value` attribute can also can be set to `null` if you want the library to consider the `defaultValue` as the value of the field.

  The library will automatically add the rest of the attributes when parsing YAML to PricingManager. -->

<!-- ## Java objects to manage pricing

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
| void setPricingConfiguration(PricingManager pricingManager)                 | Receives a PricingManager object and writes it to the pricing configuration file.                                                                                                                                                                                                |
| void removePlanFromConfiguration(String name)                               | Removes a plan from the pricing configuration. In order to do that,  it must exist in the PricingContext that is being used.                                                                                                                                                     |
| void removeFeatureFromConfiguration(String name)                            | Removes a feature from the pricing configuration. In order to do  that, it must exist in the PricingContext that is being used. The  method also removes the feature from all the plans that include it.                                                                         |                                                                    |

As any other spring service, to use this class it must be injected in any bean using @Autowired. Once declared, the methods can be used to manage the pricing configuration.

## Extra functionalities

The library also provides an method level annotation called @PricingPlanAware that receives a string called `featureId` as paramater. This feature must exist inside the pricing configuration.
By combining the use of this annotation with the spring's @Transactional, it is possible to automate feature checking on the service layer of the application.

The annotation performs an evaluation of the feature right after the method is executed. If the evaluation determines that the pricing plan is not being respected, a PricingPlanEvaluationException is thrown, so all the changes made are removed by the @Transactional annotation rollback. On the other hand, if the evaluation is correct, the changes are commited and the method returns normally.

The following snippet is an example of the use of this annotation inside a demo app service:

```java
// ...

@PricingPlanAware(featureId = "maxPets")
@Transactional(rollbackFor = { DuplicatedPetNameException.class, PricingPlanEvaluationException.class })
public Pet savePet(Pet pet) throws DataAccessException, DuplicatedPetNameException {
  Pet otherPet = getPetWithNameAndIdDifferent(pet);
  if (otherPet != null && !otherPet.getId().equals(pet.getId())) {
    throw new DuplicatedPetNameException();
  } else
    petRepository.save(pet);

  

  return pet;
}

// ...
``` -->