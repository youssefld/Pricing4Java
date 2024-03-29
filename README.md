# Pricing4Java

Pricing4Java is a Java-based toolkit designed to enhance server-side functionality for pricing-driven SaaS applications, facilitating seamless integration of pricing plans into the application logic. This package offers a suite of components based on the Yaml4SaaS syntax, making it easy to define system pricing, features along with their evaluation expressions, grouping them within plans and add-ons, and setting usage limits.

This toolkit is intended for use with [Pricing4React](https://github.com/isa-group/Pricing4React.git), a frontend library that consumes the generated JWT to toggle features on and off based on the user's pricing plan.

For detailed information on how to get started with Pricing4Java, advanced configurations, and integration guides, please visit our [official documentation website](https://pricing4saas-docs.vercel.app).

## Installation

Pricing4Java is built to be used with Maven. To install, simply add the following dependencies to your `pom.xml` file:

```xml
<dependencies>
    ...
    <!-- Pricing4Java -->
    <dependency>
        <groupId>io.github.isa-group</groupId>
        <artifactId>Pricing4Java</artifactId>
        <version>{version}</version>
    </dependency>
    ...
</dependencies>
```

## Getting Started

To begin using Pricing4Java, you will need to declare a pricing configuration file in YAML format using the Yaml4SaaS syntax and configure the pricing context in your application. These initial steps are designed to be quick and straightforward, following our step-by-step guides in the [documentation website](https://pricing4saas-docs.vercel.app).

## Contributions

This project is part of the research activities of the [ISA Group](https://www.isa.us.es/3.0/). It is still under development and should be used with caution. We are not responsible for any damage caused by the use of this software. If you find any bugs or have any suggestions, please let us know by opening an issue in the [GitHub repository](https://github.com/isa-group/Pricing4Java/issues).