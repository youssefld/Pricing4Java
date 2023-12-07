package io.github.isagroup.exceptions;

public class CloneUsageLimitException extends RuntimeException {
    public CloneUsageLimitException(String message) {
        super(message);
    }
}