package io.github.isagroup.models.featuretypes;

import io.github.isagroup.models.Feature;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Information extends Feature {
    
    public String toString(){
        return "Information[valueType: " + valueType + ", defaultValue: " + defaultValue + ", value: " + value + "]";
    }

}
