package io.github.isagroup.models.featuretypes;

import java.util.List;

import io.github.isagroup.exceptions.PricingParsingException;
import io.github.isagroup.models.Feature;
import io.github.isagroup.models.ValueType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Payment extends Feature{

    public String toString(){
        return "Payment[valueType: " + valueType + ", defaultValue: " + defaultValue + "]";
    }
}
