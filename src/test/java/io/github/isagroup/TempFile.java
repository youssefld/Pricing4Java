package io.github.isagroup;

public class TempFile {

    private String sourcePath;
    private String destinationPath;

    public TempFile(String sourcePath, String destinationPath) {
        this.sourcePath = sourcePath;
        this.destinationPath = destinationPath;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getDestinationPath() {
        return destinationPath;
    }
}
