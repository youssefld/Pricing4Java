package io.github.isagroup.services.updaters;

public enum Version {
    V1_0(1, 0), V1_1(1, 1), V1_2(1, 2);

    private final int major;
    private final int minor;

    private Version(int major, int minor) {
        if (!isValid(major, minor)) {
            throw new IllegalStateException(String.format("Version of yaml {}.{} is unsupported", major, minor));
        }

        this.major = major;
        this.minor = minor;

    }

    public static Version version(Object version) throws Exception {

        if (version instanceof Double) {
            return Version.version((Double) version);
        } else if (version instanceof String) {
            return Version.version((String) version);
        } else {
            throw new Exception("Unexpected type of class " + version.getClass().getName());
        }

    }

    private static Version version(Double version) throws Exception {
        String tempVer = String.valueOf(version);
        return Version.version(tempVer);
    }

    private static Version version(String version) throws Exception {

        if (version.isEmpty() || version.isBlank()) {
            throw new Exception("Version is blank");
        }

        StringBuilder majorBuilder = new StringBuilder();
        StringBuilder minorBuilder = new StringBuilder();
        int dots = 0;
        char[] charVersion = version.trim().toCharArray();

        for (int i = 0; i < charVersion.length; i++) {

            if (charVersion[i] == '.') {
                dots++;
                continue;
            }

            if (charVersion[i] < '0' || charVersion[i] > '9') {
                throw new Exception(
                        String.format(
                                "Invalid character \"%s\" at position %d in version \"%s\"",
                                charVersion[i], i, version));
            }

            if (dots == 0) {
                majorBuilder.append(charVersion[i]);
            } else if (dots == 1) {
                minorBuilder.append(charVersion[i]);
            }
        }

        int major = -1;
        int minor = -1;
        try {
            major = Integer.parseInt(majorBuilder.toString());
        } catch (NumberFormatException e) {
            throw new Exception(String.format("Unable to parse major \"%s\"", majorBuilder.toString()));
        }

        try {
            minor = Integer.parseInt(minorBuilder.toString());
        } catch (NumberFormatException e) {
            throw new Exception(String.format("Unable to parse minor \"%s\"", minorBuilder.toString()));
        }

        if (!isValid(major, minor)) {
            throw new Exception(String.format("Version %d.%d is not valid", major, minor));
        }

        switch (major) {
            case 1:
                switch (minor) {
                    case 0:
                        return V1_0;
                    case 1:
                        return V1_1;
                    case 2:
                        return V1_2;
                    default:
                        return null;
                }
            default:
                throw new Exception("Unrecognized version");
        }

    }

    public static boolean isValid(int major, int minor) {
        return major == 1 && (minor == 0 || minor == 1 || minor == 2);
    }

    @Override
    public String toString() {
        return this.major + "." + this.minor;
    }
}
