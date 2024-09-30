package io.github.isagroup;

import io.github.isagroup.models.Plan;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.services.yaml.YamlUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AnnotatedElementContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.io.TempDirFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PricingService2Tests {


    @TempDir(factory = Factory.class)
    private Path tempDir;

    private PricingService pricingService;
    private PricingContextTestImpl pricingConfig;

    private static PricingManager petClinic;
    private static Plan newPlan;

    @Test
    void factoryTest() {
        assertTrue(tempDir.getFileName().toString().startsWith("temp"));
    }

    static class Factory implements TempDirFactory {

        @Override
        public Path createTempDirectory(AnnotatedElementContext elementContext, ExtensionContext extensionContext)
            throws IOException {


            return Files.createDirectory(Path.of("src", "main", "resources", "temp"));
        }

    }

    private String getTempPricingPath(String yamlName) {
        return this.tempDir.getFileName() + "/" + yamlName + ".yml";
    }

    @BeforeAll
    static void beforeAll() {

        petClinic = YamlUtils.retrieveManagerFromYaml("pricing/petclinic.yml");

        newPlan = new Plan();
        newPlan.setName("NEW_PLAN");
        newPlan.setDescription("New plan description");
        newPlan.setMonthlyPrice(2.0);
        newPlan.setAnnualPrice(1.0);
        newPlan.setUnit("clinic/month");
    }

    @BeforeEach
    void setUp() {
        this.pricingConfig = new PricingContextTestImpl();
        this.pricingService = new PricingService(pricingConfig);
    }


    @Test
    void givenAPlanNameServiceShouldReturnPlan() {

        YamlUtils.writeYaml(petClinic, getTempPricingPath("petclinic"));

        pricingConfig.setConfigFilePath(getTempPricingPath("petclinic"));

        assertEquals(petClinic.getPlans().get("BASIC"), pricingService.getPlanFromName("BASIC"));

    }

    @Test
    void givenNonExistentPlanShouldThrow() {

        YamlUtils.writeYaml(petClinic, getTempPricingPath("petclinic"));

        String nonExistentPlan = "nonExistentPlan";
        pricingConfig.setConfigFilePath(getTempPricingPath("petclinic"));

        try {
            pricingService.getPlanFromName(nonExistentPlan);
        } catch (IllegalArgumentException e) {
            assertEquals("The plan " + nonExistentPlan + " does not exist in the current pricing configuration",
                e.getMessage());
        }
    }

    @Test
    void givenPlanShouldAddPlanToConfigFile() {

        YamlUtils.writeYaml(petClinic, getTempPricingPath("petclinic"));
        pricingConfig.setConfigFilePath(getTempPricingPath("petclinic"));

        pricingService.addPlanToConfiguration(newPlan);

        assertTrue(pricingService.getPricingPlans().containsKey(newPlan.getName()),
            "Pricing config does not have " + newPlan.getName());

    }

    @Test
    void givenDuplicatePlanShouldThrowExceptionWhenAddingPlan() {

        YamlUtils.writeYaml(petClinic, getTempPricingPath("petclinic"));
        pricingConfig.setConfigFilePath(getTempPricingPath("petclinic"));

        try {
            pricingService.addPlanToConfiguration(newPlan);
        } catch (IllegalArgumentException e) {
           fail(e.getMessage());
        }

        try {
            pricingService.addPlanToConfiguration(newPlan);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("The plan " + newPlan.getName() + " already exists in the current pricing configuration",
                e.getMessage());
        }



    }
}
