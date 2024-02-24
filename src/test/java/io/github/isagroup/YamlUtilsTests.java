package io.github.isagroup;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.github.isagroup.models.PricingManager;
import io.github.isagroup.services.yaml.YamlUtils;

public class YamlUtilsTests {

    @Test
    @Disabled
    void givenNullPathWhenReadingConfigShouldThrowIOException() {

        assertThrows(IllegalArgumentException.class, () -> YamlUtils.retrieveManagerFromYaml(null));

    }

    @Test
    @Disabled
    void givenNullPathWhenWritingConfigShouldThrowIOException() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/petclinic.yml");
        assertThrows(IllegalArgumentException.class, () -> YamlUtils.writeYaml(pricingManager, null));

    }

}
