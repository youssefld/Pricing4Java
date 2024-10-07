package io.github.isagroup.services.updaters;

import io.github.isagroup.exceptions.VersionException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Version {
    V1_0(1, 0), V1_1(1, 1);

    private final int major;
    private final int minor;

    public static final Version LATEST = V1_1;

    Version(int major, int minor) {
        if (!isValid(major, minor)) {
            throw new IllegalStateException(String.format("Version of yaml %d.%d is unsupported", major, minor));
        }

        this.major = major;
        this.minor = minor;

    }

    public static Version version(Object version) throws VersionException {

        if (version == null) {
            throw new IllegalArgumentException("Cannot parse a null version");
        } else if (version instanceof Double) {
            return Version.version((Double) version);
        } else if (version instanceof String) {
            return Version.version((String) version);
        } else {
            throw new VersionException("Cannot parse " + version + " to a version");
        }

    }

    private static Version version(Double version) throws VersionException {
        String tempVer = String.valueOf(version);
        return Version.version(tempVer);
    }

    private static Version version(String version) throws VersionException {

        if (version.isEmpty() || version.isBlank()) {
            throw new VersionException("Version is blank");
        }

        String regex = "(\\d+)\\.(\\d+)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(version);

        if (!matcher.matches()) {
            throw new VersionException("Invalid version \"" + version + "\", use <major>.<minor> version format");
        }

        int major;
        int minor;

        try {
            major = Integer.parseInt(matcher.group(1));

        } catch (NumberFormatException e) {
            throw new VersionException("major " + matcher.group(1) + " overflows an int");
        }

        try {
            minor = Integer.parseInt(matcher.group(2));

        } catch (NumberFormatException e) {
            throw new VersionException("minor " + matcher.group(2) + " overflows an int");
        }


        if (major == 1) {
            return switch (minor) {
                case 0 -> Version.V1_0;
                case 1 -> Version.V1_1;
                default -> throw new VersionException("Unsupported minor version " + minor);
            };
        } else {
            throw new VersionException("Unsupported major version " + major);
        }
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public static boolean isValid(int major, int minor) {
        return major == 1 && (minor == 0 || minor == 1 || minor == 2);
    }

    public int compare(Version version) {

        if (this.major > version.getMajor()) {
            return 1;
        }

        if (this.major < version.getMajor()) {
            return -1;
        }

        if (this.minor > version.getMinor()) {
            return 1;
        } else if (this.minor < version.getMinor()) {
            return -1;
        }

        return 0;
    }

    @Override
    public String toString() {
        return this.major + "." + this.minor;
    }
}
