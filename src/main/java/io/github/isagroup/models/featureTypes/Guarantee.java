package io.github.isagroup.models.featuretypes;

import io.github.isagroup.models.Feature;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Guarantee extends Feature {
    private String docURL;

    public String toString() {
        return "Guarantee[name: " + name + ", valueType: " + valueType + ", defaultValue: " + defaultValue + ", value: "
                + value + ", docURL: " + docURL + "]";
    }
}
