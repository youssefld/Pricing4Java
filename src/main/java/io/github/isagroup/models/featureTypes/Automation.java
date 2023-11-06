package io.github.isagroup.models.featuretypes;

import io.github.isagroup.models.Feature;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Automation extends Feature{
    private AutomationType automationType;

    public String toString(){
        return "Automation[valueType: " + valueType + ", defaultValue: " + defaultValue + ", automationType: " + automationType + "]";
    }
}
