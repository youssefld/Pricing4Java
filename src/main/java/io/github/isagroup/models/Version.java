package io.github.isagroup.models;

import java.util.Optional;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Version {

    private final int major;
    private final int minor;

    public Version(int major, int minor) {
        if (!isValid(major, minor)) {
            throw new IllegalStateException(String.format("Version of yaml {}.{} is unsupported", major, minor));
        }

        this.major = major;
        this.minor = minor;

    }

    public static Optional<Version> valueOf(String version) {

        if (version.isEmpty() || version.isBlank()) {
            return Optional.empty();
        }

        StringBuilder majorBuilder = new StringBuilder();
        StringBuilder minorBuilder = new StringBuilder();
        int dots = 0;

        for (char character : version.trim().toCharArray()) {

            if (character == '.') {
                dots++;
                continue;
            }

            if (character < '0' || character > '9') {
                return Optional.empty();
            }

            if (dots == 0) {
                majorBuilder.append(character);
            } else if (dots == 1) {
                minorBuilder.append(character);
            }
        }

        int major = -1;
        int minor = -1;
        try {
            major = Integer.parseInt(majorBuilder.toString());
            minor = Integer.parseInt(minorBuilder.toString());
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

        if (!isValid(major, minor)) {
            Optional.empty();
        }

        return Optional.of(new Version(major, minor));

    }

    public static boolean isValid(int major, int minor) {
        return major == 1 && (minor == 0 || minor == 1);
    }

    @Override
    public String toString() {
        return this.major + "." + this.minor;
    }
}
