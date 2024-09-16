package io.github.isagroup.services.parsing;

import io.github.isagroup.exceptions.PricingParsingException;

public interface Validator {
    void validate() throws PricingParsingException;
}
