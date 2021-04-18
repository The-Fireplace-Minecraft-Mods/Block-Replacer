package the_fireplace.wgblockreplacer.api.config;

import the_fireplace.wgblockreplacer.config.Validator;

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
