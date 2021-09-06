package dev.the_fireplace.wgblockreplacer.api.config;

import dev.the_fireplace.wgblockreplacer.config.Validator;

import java.util.Collection;

public interface ConfigValidator {
    static ConfigValidator getInstance() {
        //noinspection deprecation
        return Validator.INSTANCE;
    }
    boolean calculateValidity();
    boolean isValid();
    Collection<String> getValidationErrors();
}
