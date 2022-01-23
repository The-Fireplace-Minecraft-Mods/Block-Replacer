package dev.the_fireplace.wgblockreplacer.config;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.wgblockreplacer.domain.config.ConfigValues;

@Implementation(name = "default")
public final class WGBRConfigDefaults implements ConfigValues
{
    @Override
    public String getCurrentReplacementKey() {
        return "DEFAULT_REPLACEMENT_KEY";
    }

    @Override
    public boolean isPreventLoadOnFailure() {
        return true;
    }
}
