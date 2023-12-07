package io.github.isagroup.models.featuretypes;

import io.github.isagroup.models.Feature;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Payment extends Feature{

    public String toString(){
        return "Payment[name: " + name + ", valueType: " + valueType + ", defaultValue: " + defaultValue + ", value: " + value + "]";
    }
}
