package io.github.isagroup.models.usagelimittypes;

import java.io.Serializable;

import io.github.isagroup.models.UsageLimit;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Capacity extends UsageLimit{
    @Override
    public String toString() {
        return "Capacity[valueType: " + this.getValueType() + ", defaultValue: " + this.getDefaultValue() + ", value: " + this.getValue() + ", linkedFeature: " + this.getLinkedFeatures() + "]";
    }
}
