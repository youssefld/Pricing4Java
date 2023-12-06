package io.github.isagroup.models.featuretypes;

import io.github.isagroup.models.Feature;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Support extends Feature{
    
    public String toString(){
        return "Support[name: " + name + ", valueType: " + valueType + ", defaultValue: " + defaultValue + ", value: " + value + "]";
    }

}
