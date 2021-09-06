package dev.the_fireplace.wgblockreplacer.api.config;

import dev.the_fireplace.wgblockreplacer.config.ValidationFailureHandlerImpl;

public interface ValidationFailureHandler {
    static ValidationFailureHandler getInstance() {
        //noinspection deprecation
        return ValidationFailureHandlerImpl.INSTANCE;
    }
    void revalidate();
}
