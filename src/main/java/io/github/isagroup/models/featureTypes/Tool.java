package io.github.isagroup.models.featuretypes;

import io.github.isagroup.models.Feature;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Tool extends Feature {

    public String toString(){
        return "Tool[valueType: " + valueType + ", defaultValue: " + defaultValue + ", value: " + value + "]";
    }

}
