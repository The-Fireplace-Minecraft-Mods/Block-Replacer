package the_fireplace.wgblockreplacer.api.config;

import java.util.Collection;

public interface ConfigValidator {
    static ConfigValidator getInstance() {
        return null;
    }
    boolean validate();
    Collection<String> getValidationErrors();
}
