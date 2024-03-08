# Pricing4Java

Pricing4Java is a Java-based toolkit designed to enhance the server-side functionality of a pricing-driven SaaS by enabling the seamless integration of pricing plans into the application logic. The package provides a suite of components that are predicated on the Yaml4SaaS syntax, a specification that facilitates the definition of system's pricing and its features alongside their respective evaluation expressions, grouping them within plans and add-ons, as well as establishing usage limits.

Pricing4Java has been designed to be used with [pricingplans-react](https://github.com/isa-group/pricingplans-react.git), a frontend library that consumes the generated JWT and toggles on and off features based on the user pricing plan.

<!--The cornerstone of the package is the \textbf{PricingContext}, an abstract class that empowers developers to tailor the library's capabilities to the pricing framework of their applications. Once it is configured, it unlocks a suite of business logic tools, such as: a service for managing the pricing plan or custom annotations that automate the back-end validation of the pricing rules.

In addition to these components, the package can also manage the JWTs that contain the result of the feature evaluation for an user alongside the authentication data defined within the application. Thus, we can automatically keep the feature evaluation data up to date without needing to implement a polling mechanism that overloads the server with additional API calls.-->

## Index

- [Installation](#installation)
- [Getting Started](#started)
- [Yaml4SaaS](#yaml4saas)
  - [Configuring feature evaluation](#feature-evaluation)
  - [Yaml4SaaS Validator](#yaml-validator)
- [Java objects to manage pricing](#java-objects-to-manage-pricing)
  - [PricingManager](#pricingManager)
  - [Feature](#feature)
  - [UsageLimit](#usageLimit)
  - [Plan](#plan)
  - [AddOn](#addOn)
- [YamlUtils](#yaml-utils)
- [PricingContext](#pricingContext)
- [Pricing4Java components](#pricing4java-components)
  - [PricingEvaluatorUtil](#pricingEvaluatorUtil)
  - [PricingService](#pricingService)
  - [PricingPlanAware Annotation](#pricingPlanAware)

## Installation

<a id="#installation"></a>

The package have been build to be used with maven. To install it, just add the following dependencies to your `pom.xml`file:

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

## Getting Started

<a id="#started"></a>

### 1. Declare a pricing configuration file

Pricing4Java uses a YAML file written in the [Yaml4SaaS](#yaml4saas) syntax to represent and manage the whole pricing. The file must be placed inside the resources folder of the project. Create the file `pricing/pricing.yml` withn such directory and copy the snippet below, which shows a basic structure of such syntax:

```yaml
saasName: My First Pricing Driven SaaS
day: 10
month: 03
year: 2024
currency: USD
features:
  cloudStorage:
    description: "This is a fancy description of the cloudStorage feature"
    valueType: BOOLEAN
    defaultValue: true
    type: DOMAIN
    expression: "userContext['cloudStorageUse'] < planContext['cloudStorage']"
    serverExpression: "userContext['cloudStorageUse'] <= planContext['cloudStorage']"
  adminDashboard:
    description: "The admin dashboard is a powerful tool to manage the SaaS"
    valueType: BOOLEAN
    defaultValue: false
    type: MANAGEMENT
    expression: "userContext['cloudStorageUse'] < planContext['cloudStorage']"
    serverExpression: "userContext['cloudStorageUse'] <= planContext['cloudStorage']"
usageLimits:
  cloudStorageMaxCapacity:
    description: "This is the usage limit imposed to the cloudStorage feature"
    valueType: NUMERIC
    unit: GB
    defaultValue: 1
    type: NON_RENEWABLE
    linkedFeatures:
      - cloudStorage
plans:
  FREE:
    description: "The free plan is allowed to all users"
    monthlyPrice: 0
    unit: "user/month"
  PREMIUM:
    description: "Unlock all our features with the premium plan"
    monthlyPrice: 15
    unit: "user/month"
    features:
      adminDashboard:
        value: true
    usageLimits:
      cloudStorageMaxCapacity:
        value: 10
addOns:
  extraCloudStorageCapacity:
    availableFor:
      - FREE
      - PREMIUM
    price: 20
    unit: user/month
    usageLimitsExtensions:
      cloudStorageMaxCapacity:
        value: 100
```

### 2. Configure the pricing context

Once the pricing configuration file is ready, the next step is to create a component that extends the [PricingContext](#pricingContext) abstract class. This component will be the key to manage all the pricing configuration, including user context evaluation, JWT generation, pricing operations, etc.

```java

import io.github.isagroup.PricingContext;

@Component
public class PricingConfiguration extends PricingContext {

    @Override
    public String getJwtSecret(){
        return "mySecret";
    }

    @Override
    public String getConfigFilePath(){
        return "pricing/pricing.yml";
    }

    @Override
    public Object getUserAuthorities() {

        Map<String, String> authorities = new HashMap<>();

        authorities.put("username", "John");
        authorities.put("role", "admin");

        return authorities;
    }

    @Override
    public Map<String, Object> getUserContext() {

        Map<String, Object> userContext = new HashMap<>();

        // Add the logic to retrieve all the data needed within the user context, such as his current usage of the feature cloudStorage.

        return userContext;
    }

    @Override
    public String getUserPlan() {

        // Should be replace by the logic that returns the plan name of the current user.
        return "BASIC";
    }

}

```

### 3. Generate JWT token with evaluation

Once the PricingConfiguration is set, you can inject in any component all the features included in Pricing4Java. For example, to generate the JWT that can be sent to a frontend that implements [Pricing4React](https://github.com/isa-group/pricingplans-react):

```java
@Service
public class UserService {

	//...

	@Autowired
	private PricingEvaluatorUtil pricingEvaluatorUtil;

    //...

    public String login(String username, String password) {

        if(validCredentials(username, password)){
            return pricingEvaluatorUtil.generateUserToken();
        } else {
            return null;
        }
    }
}
```

In this example, the `generateUserToken` method is used to generate a JWT that contains the result of the feature evaluation for the user.

```
{
  "features": {
    "cloudStorage": {
      "eval": true,
      "limit": 1,
      "used": 0.1
    },
    "adminDashboad": {
      "eval": false,
      "limit": null,
      "used": null
    },
  },
  "sub": "testUser",
  "exp": 1687705951,
  "userContext": {
    ...
  },
  "iat": 1687705864,
  "authorities": {
    "username": "testUser"
    "role": "customer",
  },
  "planContext": {
    ...
  }
}
```

## Yaml4SaaS

<a id="#yaml4saas"></a>

Yaml4SaaS emerges as a pragmatic application of the [Pricing4SaaS model](), aligning with the overarching objective of formalizing and structuring pricing information for SaaS platforms. Building upon the foundational principles articulated in Pricing4SaaS, Yaml4SaaS embodies a simplified and versatile YAML-based syntax designed for serializing comprehensive details about SaaS offerings. The essence of Yaml4SaaS lies in its capacity to encapsulate pricing plans, add-ons, features and usage limits within a concise and human-readable YAML format. Here is a tempalte specification of the Yaml4SaaS syntax:

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
    expression: #SPEL Expression
    serverExpression: #OPTIONAL: SPEL Expression to be evaluated on the server side
  standardSupport:
    description: ...
    valueType: BOOLEAN
    defaultValue: false
    type: SUPPORT
    expression: #SPEL Expression
    serverExpression: #OPTIONAL: SPEL Expression to be evaluated on the server side
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

**Features** enumerate all the functionalities encompassed in the pricing, classifying them into the types defined in Pricing4SaaS:

- INFORMATION
- INTEGRATION
- DOMAIN
- AUTOMATION
- MANAGEMENT
- GUARANTEE
- SUPPORT
- PAYMENT

detailing each feature's `description`, `valueType` (BOOLEAN, NUMERIC TEXT), and `defaultValue`, whose data type has to be aligned with the `valueType` defined:

- If the `type` is `CONDITION`, the `defaultValue` must be a Boolean.
- If the `type` is `NUMERIC`, the `defaultValue` must be Integer or Double
- If the `type` is `TEXT`, the `defaultValue` must be a String.

<!-- Notably, features do not handle NUMERIC values, which are reserved for limits. -->

In addition, depending on each type of feature, the syntax extends expressiveness for each feature type with additional fields:

- For **integration** features, an `IntegrationType` can be specified through the `integrationType` field. If its value is WEB_SAAS, a list of SaaS pricing URLs can be included.
- **Automation** features do also allow to assign theirselves an `AutomationType`.
- For **guarantee** features can reference the corresponding documentation section describing them via the `docURL` field.
- **Payment** features differ from others, requiring values as a list of `PaymentTypes` for standardization.

Similar to features, **UsageLimits** expounds on limitations affecting plans, add-ons, or features in the pricing, tagging each with the corresponding Pricing4SaaS type:

- NON_RENEWABLE
- RENEWABLE
- RESPONSE_DRIVEN
- TIME_DRIVEN

For each limit, similar to features, a `description`, `valueType` (BOOLEAN, TEXT, NUMERIC), and `defaultValue` are provided, accompanied by additional fields such as `unit` or `linkedFeatures`. The latter must be a list of previously described features affected by the limitation.

The **plans** section provides comprehensive details about the distinct pricing plans offered within the SaaS. Each plan is identified by a unique `name`, allowing for easy reference and differentiation. For each one, essential information is specified, including a brief `description`, the `monthlyPrice`, the `annualPrice` (if different from monthly) and the `unit` affected by them, typically expressed as "user/month".

In the `features` and `usageLimits` subsections of each plan, only those requiring a modification in their `defaultValue` should be explicitly listed. For those not mentioned, the `defaultValue` is understood to be equivalent to the `value`.

Within the **addOns** section, the focus is on delineating the specific details of additional offerings beyond the core plans. Each add-on is characterized by its unique features and usage limits, which have to be listed in the structure established in the `features` and `usageLimits` sections, but not included on plans. Similar to the approach taken in the previous section of the file, only those `features` or `usageLimits` necessitating an alteration in the `defaultValue` are explicitly outlined. As an extra field, add-ons also allow to extent a usageLimit. This is extremely powerful for modeling overage cost to some limits.

In conclusion, Yaml4SaaS stands as a practical implementation of the Pricing4SaaS model, providing a YAML-based syntax to formalize SaaS pricing structures in a human-readable format that enhances clarity and simplicity.

### Configuring feature evaluation

<a id="#feature-evaluation"></a>

Since the primary goal of the Pricing4Java is to automatically manage the access of an user to a feature, Yaml4SaaS allows to define an evaluation expression within each feature through the `expression` and `serverExpression` fields.

The `expression` field must contain a string [SPEL](https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/expressions.html) expression that will be used to evaluate wether the feature is available for an user or not. It can access the data of the user context using the `userContext` variable, while the plan's is available through `planContext`.For example, considering a user context that contains the following information:

```json
{
  "username": "John",
  "feature1use": 2
}
```

If we want to check if the use of the feature exceeds its limit, the `SPEL` expression should be:

```yaml
# ...
cloudStorage:
  # ...
  expression: "userContext['cloudStorageUse'] <= planContext['cloudStorage']"
  # ...
```

Similarly, the `serverExpresion` field can handle expressions with the same syntax, but its specification will only be used to evaluate the system's consistency using [@PricingPlanAware](#pricingPlanAware) annotation. This use can be interesting on NUMERIC features, let's see an example.

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

To handle this type of situations, the use of the field `serverExpression` is necessary, as its expression will be used to evaluate the feature on the server side (when using [@PricingPlanAware](#pricingPlanAware) annotation). If `serverEspression` is not defined, the `expression` will be used instead on any evaluation context. The snippet below shows how to define the situation described above:

```yaml
# ...
feature1:
  # ...
  expression: userContext['feature1use'] < planContext['feature1']
  serverExpression: userContext['feature1use'] <= planContext['feature1']
  # ...
```

### Yaml4SaaS Validator

<a id="#yaml-validator"></a>

Pricing4Java includes a validator of the Yaml4SaaS syntax that can be easily run by creating a test inside the `saasYamlParsingTest.java` file following the next structure:

```java
@Test
@Order(X)
void parsePostmanYamlToClassTest() {
    PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/{NAME_OF_YOUR_FILE}.yml");
}
```

The test will fail if the YAML file does not correctly follow the Yaml4SaaS syntax, and will throw an exception explaining the problem.

## Java objects to manage pricing
<a id="#java-objects-to-manage-pricing"></a>

### PricingManager
<a id="#pricingManager"></a>

This class is the main object of the package. It contains all the information about the pricing configuration and can be used to evaluate the context of an user and generate a JWT with the results.

```java
private String saasName;
    private int day;
    private int month;
    private int year;
    private String currency;
    private Boolean hasAnnualPayment;
    private Map<String, Feature> features;
    private Map<String, UsageLimit> usageLimits;
    private Map<String, Plan> plans;
    private Map<String, AddOn> addOns;

    // Getters and setters...
```

The name of each feature, usage limit, plan or addon is used as a key to access the information of the object. For example, to access the price of the plan `BASIC` we can use:

```java
pricingManager.getPlans().get("BASIC").getPrice();
```

### Feature
<a id="#feature"></a>

This abstract class models the information of a feature.

```java
public abstract class Feature implements Serializable {
    protected String name;
    protected String description;
    protected ValueType valueType;
    protected Object defaultValue;
    protected transient Object value;
    protected String expression;
    protected String serverExpression;

    // Getters and setters...

    // Extra methods
}
```

The class also contains a set of extra methods that can be used to prepare the object to be written inside a plan in the YAML file by removing the setting the value of all the attributes to `null`, except `value`. However, one of them can be useful: `featureAttributesMap`, as it returns a `Map<String,Object>` with the attributes of the feature and their values.

Each feature type supported by Yaml4SaaS is represented by a class that extends this abstract one. The following list shows the classes that extend Feature:

- **Information**
- **Integration**
- **Domain**
- **Automation**
- **Management**
- **Guarantee**
- **Support**
- **Payment**

Each of these objects contains the specific attributes of the feature type, such as `integrationType` in the **Integration** feature class, `automationType` in the **Automation** class, etc.

### UsageLimit
<a id="#usageLimit"></a>

This abstract class models the information of an usageLimit.

```java
public abstract class UsageLimit {
    private String name;
    private String description;
    private ValueType valueType;
    private Object defaultValue;
    protected UsageLimitType type;
    private String unit;
    private transient Object value;
    private List<String> linkedFeatures = new ArrayList<>();
    private String expression;
    private String serverExpression;

    // Getters and setters...

    public boolean isLinkedToFeature(String featureName);

    // Extra methods...
}
```

The method isLinkedToFeature can be used to check if the feature whose name is received as parameter is linked to the usageLimit.

Besides, each usage limit type supported by Yaml4SaaS is represented by a class that extends this abstract one. The following list shows the classes that extend Feature:

- **NonRenewable**
- **Renewable**
- **ResponseDriven**
- **TimeDriven**

### Plan
<a id="#plan"></a>

This class models the information of a plan.

```java
public class Plan {
    private String name;
    private String description;
    private Object monthlyPrice;
    private Object annualPrice;
    private String unit;
    private Map<String, Feature> features;
    private Map<String, UsageLimit> usageLimits;

    // Getters and setters...
}
```

### AddOn
<a id="#addOn"></a>

This class models the information of an addOn.

```java
public class AddOn {
    private String name;
    private List<String> availableFor;
    private Object price;
    private Object monthlyPrice;
    private Object annualPrice;
    private String unit;
    private Map<String, Feature> features;
    private Map<String, UsageLimit> usageLimits;
    private Map<String, UsageLimit> usageLimitsExtensions;

    // Getters and setters...
}
```

## YamlUtils

This class contains two static methods that can be used to parse the YAML specification of a pricing in the Yaml4SaaS syntax to a PricingManager object and vice versa. To extract the information of the YAML file:

```java
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.services.yaml.YamlUtils;

@Component
public class MyComponent {

    public String myMethod() {
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/pricing.yml");
    }
}
```

And to write the information of a PricingManager object into the YAML file:

```java
@Component
public class MyComponent {

    public String myMethod() {

        PricingManager pricingManager = new PricingManager(); // This should be your dynamically retrieved pricingManager object

        YamlUtils.writeYaml(pricingManager, "pricing/pricing.yml");
    }
}
```

## PricingContext

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

## Pricing4Java components
<a id="#pricing4java-components"></a>

Once the [PricingContext](#pricingContext) has been defined, all the components of Pricing4Java can be injected anywhere within the app to perform operations with the pricing using **PricingManager**.

### PricingEvaluatorUtil
<a id="#pricingEvaluatorUtil"></a>

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
    "role": "admin",
    "username": "admin1"
  },
  "planContext": {
    ...
  }
}
```

### PricingService
<a id="#pricingService"></a>

This class offers a set of methods that can be used to manage the pricing configuration without manually modifying the YAML file. It can be used to retrieve, add, remove or modify plans and features.

| **Method**                                                                                   | **Description**                                                                                                                                                                                        |
| -------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Plan getPlanFromName(String planName)                                                        | Returns the plan of the configuration that matchs the given name.                                                                                                                                      |
| void addFeatureToConfiguration(String name, Feature feature)                                 | Creates a new global feature in the pricing configuration and adds it to all the plans using its default value.                                                                                        |
| void addUsageLimitToConfiguration(UsageLimit usageLimit)                                     | Creates a new global usageLimit in the pricing configuration and adds it to all the plans using its default value.                                                                                     |
| void addPlanToConfiguration(String name, Plan plan)                                          | Adds a new plan to the current pricing configuration.                                                                                                                                                  |
| void addAddOnToConfiguration(AddOn addOn)                                                    | Adds a new add on to the current pricing configuration.                                                                                                                                                |
| void updateFeatureFromConfiguration(String previousName, Feature feature)                    | Updates a feature in the pricing configuration.                                                                                                                                                        |
| void updateUsageLimitFromConfiguration(String previousUsageLimitName, UsageLimit usageLimit) | Update an existing usage limit in the pricing configuration.                                                                                                                                           |
| void updatePlanFromConfiguration(String previousName, Plan plan)                             | Updates a plan in the pricing configuration.                                                                                                                                                           |
| void updateAddOnFromConfiguration(String previousName, AddOn addOn)                          | Updates an add on of the pricing configuration.configuration.                                                                                                                                          |
| void removeFeatureFromConfiguration(String name)                                             | Removes a feature from the pricing configuration. In order to do that, it must exist in the PricingContext that is being used. The method also removes the feature from all the plans that include it. |     |
| void removeUsageLimitFromConfiguration(String name)                                          | Deletes an usage limit from the configuration.                                                                                                                                                         |
| void removePlanFromConfiguration(String name)                                                | Removes a plan from the pricing configuration. In order to do that, it must exist in the PricingContext that is being used.                                                                            |
| void removeAddOnFromConfiguration(String addOnName)                                          | Deletes an add on from the configuration.                                                                                                                                                              |

As any other spring service, to use this class it must be injected in any bean using @Autowired. Once declared, the methods can be used to manage the pricing configuration.

### PricingPlanAware Annotation
<a id="#pricingPlanAware"></a>

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
```

# Disclaimer

This project is part of the [ISA Group](https://www.isa.us.es) research activities. It is still under development and should be used with caution. We are not responsible for any damage caused by the use of this software. If you find any bugs or have any suggestions, please let us know by opening an issue on the [GitHub repository](https://github.com/isa-group/pricingplans-4j).
