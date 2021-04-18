package the_fireplace.wgblockreplacer.api.config;

import the_fireplace.wgblockreplacer.config.ValidationFailureHandlerImpl;

public interface ValidationFailureHandler {
    static ValidationFailureHandler getInstance() {
        //noinspection deprecation
        return ValidationFailureHandlerImpl.INSTANCE;
    }
    void revalidate();
}
