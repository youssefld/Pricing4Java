package io.github.isagroup.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;

import io.github.isagroup.exceptions.PricingParsingException;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.services.parsing.PricingManagerParser;
import io.github.isagroup.services.serializer.PricingManagerSerializer;
import io.github.isagroup.services.updaters.Updater;
import io.github.isagroup.services.updaters.V11Updater;
import io.github.isagroup.services.updaters.V12Updater;
import io.github.isagroup.services.updaters.Version;
import io.github.isagroup.services.updaters.YamlUpdater;
import io.github.isagroup.services.yaml.SkipNullRepresenter;

public class VersionTransformer {

    public static void main(String[] args) {

        Version version = null;

        if (args.length != 3) {
            System.out.println("""
                    Arguments
                    sourceFolder targetFolder version

                    Version format is major.minor

                    Examples:

                    ./pricings ./pricing-updated 1.1

                    """);
            return;
        }

        if (args[0] == null) {
            System.out.println(
                    "Source folder is null. You have not specified the source directory to read configuration files");
            return;
        }

        if (args[1] == null) {
            System.out.println(
                    "Destination folder is null. You have not specified a directory to dump the configuration files");
            return;
        }

        try {
            version = Version.version(args[2]);

        } catch (Exception e) {
            System.out.println("Unrecognized version");
            return;
        }

        try {
            transformFiles("src/main/resources/pricing", "src/test/resources/temp", version);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void transformFiles(String srcPath, String destinyPath, Version targVersion) throws Exception {

        File sourceDirectory = new File(srcPath);
        File destinationDirectory = new File(destinyPath);

        if (!sourceDirectory.exists()) {
            System.out.println(String.format("Specified path '%s' does not exists directory does not exists",
                    sourceDirectory.getAbsolutePath()));
            return;
        }

        if (sourceDirectory.isFile()) {
            System.out.println(String.format("Specified path '%s' is a file, specify a directory",
                    sourceDirectory.getAbsolutePath()));
            return;
        }

        System.out.println(String.format("Reading files from '%s' directory at path '%s'", sourceDirectory.getName(),
                sourceDirectory.getAbsolutePath()));
        try {
            processFiles(sourceDirectory.toURI(), destinyPath, targVersion);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (destinationDirectory.exists()) {
            if (destinationDirectory.isFile()) {
                System.out.println(
                        String.format("Specified path '%s' is a file", destinationDirectory.getAbsolutePath()));
                return;

            } else {
                System.out.println(
                        String.format("Dumping configuration files to '%s'", destinationDirectory.getAbsolutePath()));
            }
        } else {
            destinationDirectory.mkdir();
            System.out.println(String.format("Creating folder '%s' at path '%s'",
                    destinationDirectory.getName(),
                    destinationDirectory.getAbsolutePath()));
        }

    }

    private static Map<String, Object> updater(Map<String, Object> configFile, Version version) {

        Updater updater = new YamlUpdater(configFile);

        switch (version) {
            case V1_0 -> updater = new YamlUpdater(configFile);
            case V1_1 -> updater = new V11Updater(configFile);
            case V1_2 -> updater = new V12Updater(configFile);
        }

        try {
            return updater.update();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private static boolean hasYamlExtension(String name) {
        return name.endsWith(".yml") || name.endsWith(".yaml");
    }

    private static void processFiles(URI srcPath, String destiniyPath, Version targetVersion) throws Exception {

        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(FlowStyle.BLOCK);
        Yaml yaml = new Yaml(new SkipNullRepresenter(), options);

        try (Stream<Path> stream = Files.walk(Paths.get(srcPath), FileVisitOption.FOLLOW_LINKS)) {
            stream.forEach((path) -> {
                File file = path.toFile();
                if (file.isFile() && hasYamlExtension(file.getName())) {
                    try (FileInputStream fileInput = new FileInputStream(file)) {
                        Map<String, Object> configFile = yaml.load(fileInput);
                        PricingManager pricingManager = PricingManagerParser.parseMapToPricingManager(configFile);

                        String header = "| trying to convert '%s' from version %s to %s |";
                        String formattedMessage = String.format(header, file.getName(),
                                pricingManager.getVersion(),
                                targetVersion);
                        Map<String, Object> updatedFile = updater(configFile, targetVersion);
                        PricingManager updatedPricing = PricingManagerParser.parseMapToPricingManager(updatedFile);
                        System.out.println("=".repeat(formattedMessage.length()));
                        System.out.println(formattedMessage);
                        System.out.println("=".repeat(formattedMessage.length()));

                        PricingManagerSerializer serializer = new PricingManagerSerializer();

                        try (FileWriter fileWritter = new FileWriter(destiniyPath + "/" + file.getName())) {
                            yaml.dump(serializer.serialize(updatedPricing), fileWritter);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } catch (PricingParsingException e) {
                        System.out.print(String.format("file '%s' at path '%s' could not be parsed",
                                file.getName(), file.getAbsolutePath()));
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
