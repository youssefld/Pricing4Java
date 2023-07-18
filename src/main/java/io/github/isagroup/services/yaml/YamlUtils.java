package io.github.isagroup.services.yaml;

import java.io.FileWriter;
import java.io.IOException;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import io.github.isagroup.models.PricingManager;

public class YamlUtils {
    public static PricingManager retrieveManagerFromYaml(String yamlPath){
        Yaml yaml = new Yaml(new Constructor(PricingManager.class, new LoaderOptions()));

        PricingManager pricingManager = yaml.load(
            YamlUtils.class.getClassLoader().getResourceAsStream(yamlPath)
        );

        return pricingManager;
    }

    public static void writeYaml(PricingManager pricingManager, String yamlPath){
        DumperOptions dump = new DumperOptions();
        dump.setIndent(2);
        dump.setPrettyFlow(true);
        dump.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Representer representer = new Representer(dump);
        representer.addClassTag(PricingManager.class, Tag.MAP);
        try {
            Yaml yaml = new Yaml(representer, dump);
            FileWriter writer = new FileWriter("src/test/resources/"+ yamlPath);
            yaml.dump(pricingManager, writer);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
