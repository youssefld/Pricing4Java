package io.github.isagroup.models.usagelimittypes;

import java.io.Serializable;

import io.github.isagroup.models.UsageLimit;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NonRenewable extends UsageLimit{
    @Override
    public String toString() {
        return "NonRenewable[valueType: " + this.getValueType() + ", defaultValue: " + this.getDefaultValue() + ", value: " + this.getValue() + ", linkedFeature: " + this.getLinkedFeatures() + "]";
    }
}
