package io.github.isagroup.services.yaml;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import io.github.isagroup.exceptions.FilepathException;
import io.github.isagroup.exceptions.SerializerException;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.services.parsing.PricingManagerParser;
import io.github.isagroup.services.serializer.PricingManagerSerializer;

/**
 * Utility class to handle YAML files
 */
public class YamlUtils {

    private static final String DEFAULT_YAML_WRITE_PATH = "src/main/resources/";

    /**
     * This method maps the content of the YAML file located in {@code yamlPath}
     * into a {@link PricingManager} object.
     * 
     * @param yamlPath Path of the YAML file, relative to the resources folder
     * @return PricingManager object that represents the content of the YAML file
     */

    public static PricingManager retrieveManagerFromYaml(String yamlPath) {
        Yaml yaml = new Yaml();

        try {
            String result = new String(Files.readAllBytes(Paths.get(DEFAULT_YAML_WRITE_PATH + yamlPath)));
            Map<String, Object> test = yaml.load(result);

            return PricingManagerParser.parseMapToPricingManager(test);

        } catch (IOException e) {
            throw new FilepathException("Either the file path is invalid or the file does not exist.");
        }
    }

    /**
     * Writes a {@link PricingManager} object into a YAML file.
     * 
     * @param pricingManager a {@link PricingManager} object that represents a
     *                       pricing configuration
     * @param yamlPath       Path of the YAML file, relative to the resources folder
     */
    public static void writeYaml(PricingManager pricingManager, String yamlPath) {
        
        if (yamlPath == null) {
            throw new FilepathException("Either the file path is invalid or the file does not exist.");
        }

        DumperOptions dump = new DumperOptions();
        dump.setIndent(2);
        dump.setPrettyFlow(true);
        dump.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Representer representer = new SkipNullRepresenter();

        PricingManagerSerializer pricingManagerSerializer = new PricingManagerSerializer(pricingManager);
        try (FileWriter writer = new FileWriter(DEFAULT_YAML_WRITE_PATH + yamlPath);) {
            Map<String, Object> serializedPricingManager = pricingManagerSerializer.serialize();
            Yaml yaml = new Yaml(representer, dump);
            yaml.dump(serializedPricingManager, writer);

        } catch (IOException e) {
            throw new FilepathException("Either the file path is invalid or the file does not exist.");
        } catch (SerializerException e) {
            throw new SerializerException("An error occurred while serializing the PricingManager object.");
        }
    }
}
