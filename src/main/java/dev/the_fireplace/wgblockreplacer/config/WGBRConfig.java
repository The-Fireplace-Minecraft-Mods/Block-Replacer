package dev.the_fireplace.wgblockreplacer.config;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageReadBuffer;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageWriteBuffer;
import dev.the_fireplace.lib.api.lazyio.injectables.ConfigStateManager;
import dev.the_fireplace.lib.api.lazyio.interfaces.Config;
import dev.the_fireplace.wgblockreplacer.WGBRConstants;
import dev.the_fireplace.wgblockreplacer.domain.config.ConfigValues;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Implementation("dev.the_fireplace.wgblockreplacer.domain.config.ConfigValues")
public final class WGBRConfig implements Config, ConfigValues {
    private final ConfigValues defaultConfig;

    private String currentReplacementKey;
    private boolean preventLoadOnFailure;

    @Inject
    public WGBRConfig(ConfigStateManager configStateManager, @Named("default") ConfigValues defaultConfig) {
        this.defaultConfig = defaultConfig;
        configStateManager.initialize(this);
    }

    @Override
    public String getId() {
        return WGBRConstants.MODID;
    }

    @Override
    public void readFrom(StorageReadBuffer buffer) {
        currentReplacementKey = buffer.readString("currentReplacementKey", defaultConfig.getCurrentReplacementKey());
        preventLoadOnFailure = buffer.readBool("preventLoadOnFailure", defaultConfig.isPreventLoadOnFailure());
    }

    @Override
    public void writeTo(StorageWriteBuffer buffer) {
        buffer.writeString("currentReplacementKey", currentReplacementKey);
        buffer.writeBool("preventLoadOnFailure", preventLoadOnFailure);
    }

    @Override
    public String getCurrentReplacementKey() {
        return currentReplacementKey;
    }

    @Override
    public boolean isPreventLoadOnFailure() {
        return preventLoadOnFailure;
    }
}
