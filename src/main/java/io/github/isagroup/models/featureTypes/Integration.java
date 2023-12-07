package io.github.isagroup.models.featuretypes;

import java.util.List;

import io.github.isagroup.models.Feature;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Integration extends Feature{
    private IntegrationType integrationType;
    private List<String> pricingUrls;

    public String toString(){
        return "Integration[name: " + name + ", valueType: " + valueType + ", defaultValue: " + defaultValue + ", value: " + value + ", integrationType: " + integrationType + ", pricingUrls: " + pricingUrls + "]";
    }
        
}
