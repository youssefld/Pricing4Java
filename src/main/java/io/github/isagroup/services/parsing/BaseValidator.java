package io.github.isagroup.services.parsing;

import java.util.Map;

import io.github.isagroup.exceptions.PricingParsingException;

public class BaseValidator implements Validator {

    private Map<String, Object> yamlFile;

    public BaseValidator(Map<String, Object> yamlFile) {
        this.yamlFile = yamlFile;
    }

    @Override
    public void validate() throws PricingParsingException {

        if (yamlFile.get("version") instanceof String || yamlFile.get("version") instanceof Double) {
            throw new PricingParsingException(
                    "version has to be a string or a float formmated like <major.minor>.");
        }

        if (yamlFile.get("saasName") == null) {
            throw new PricingParsingException("SaasName was not defined");
        }

        if (!(yamlFile.get("saasName") instanceof String)) {
            throw new PricingParsingException("'saasName' has to be a string");
        }

        if (yamlFile.get("currency") == null) {
            throw new PricingParsingException("Currency was not defined");
        }

        if (!(yamlFile.get("currency") instanceof String)) {
            throw new PricingParsingException("'currency' has to be a string");
        }

        if (yamlFile.get("hasAnnualPayment") == null) {
            throw new PricingParsingException("'hasAnnualPayment' was not defined");
        }

        if (!(yamlFile.get("hasAnnualPayment") instanceof Boolean)) {
            throw new PricingParsingException("'hasAnnualPayment' was not defined");
        }

    }

}
