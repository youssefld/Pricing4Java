package io.github.isagroup.models.usagelimittypes;

import io.github.isagroup.models.UsageLimit;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseDriven extends UsageLimit{
    @Override
    public String toString() {
        return "ResponseDriven[valueType: " + this.getValueType() + ", defaultValue: " + this.getDefaultValue() + ", value: " + this.getValue() + ", linkedFeature: " + this.getLinkedFeature() + "]";
    }
}
