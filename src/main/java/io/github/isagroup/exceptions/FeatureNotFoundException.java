package io.github.isagroup.exceptions;

public class FeatureNotFoundException extends RuntimeException{
    public FeatureNotFoundException(String message) {
        super(message);
    }
}
