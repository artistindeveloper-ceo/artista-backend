package com.artist_in.app.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String resourceName, Object identifier) {
        return new ResourceNotFoundException(resourceName + " not found with identifier: " + identifier);
    }
}
