package io.github.isagroup.models.featureTypes;

import java.util.List;

import io.github.isagroup.models.Feature;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Integration extends Feature{
    private IntegrationType integrationType;
    private List<String> pricingUrls;
}
