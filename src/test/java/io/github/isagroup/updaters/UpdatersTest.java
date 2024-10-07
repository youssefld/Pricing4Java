package io.github.isagroup.updaters;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;

import io.github.isagroup.exceptions.VersionException;
import io.github.isagroup.services.updaters.Version;
import io.github.isagroup.services.updaters.YamlUpdater;
import io.github.isagroup.services.yaml.YamlUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.yaml.snakeyaml.Yaml;

import io.github.isagroup.exceptions.PricingParsingException;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.services.parsing.PricingManagerParser;
import io.github.isagroup.services.serializer.PricingManagerSerializer;

import static org.junit.jupiter.api.Assertions.*;

public class UpdatersTest {


    @Test
    void givenOutOfBoundsMajorShouldThrow() {

        try {
            Version.version("9999999999.0");
        } catch (VersionException e) {
            assertEquals("major 9999999999 overflows an int", e.getMessage());
        }
    }

    @Test
    void givenOutOfBoundsMinorShouldThrow() {

        try {
            Version.version("1.9999999999");
        } catch (VersionException e) {
            assertEquals("minor 9999999999 overflows an int", e.getMessage());
        }
    }


    @Test
    void givenInvalidVersionFormatShouldThrow() {
        try {
            Version.version("alpha");
        } catch (VersionException e) {
            assertEquals("Invalid version \"alpha\", use <major>.<minor> version format", e.getMessage());
        }
    }

}
