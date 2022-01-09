package dev.the_fireplace.wgblockreplacer.config;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.lib.api.io.injectables.ConfigBasedStorageReader;
import dev.the_fireplace.lib.api.lazyio.injectables.ConfigStateManager;
import dev.the_fireplace.wgblockreplacer.domain.config.ReplacementSettings;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Singleton
@Implementation
public final class ReplacementSettingRepository implements ReplacementSettings
{
    private final Map<String, ReplacementSetting> replacementSettings = new HashMap<>();
    private final ConfigBasedStorageReader configBasedStorageReader;
    private final ConfigStateManager configStateManager;

    @Inject
    public ReplacementSettingRepository(ConfigBasedStorageReader configBasedStorageReader, ConfigStateManager configStateManager) {
        this.configBasedStorageReader = configBasedStorageReader;
        this.configStateManager = configStateManager;
    }

    @Override
    public List<ReplacementSetting> findBy(String dimensionId) {
        loadSettingsFromStorage();
        return replacementSettings.values().stream()
            .filter(replacementSetting -> isDimensionAllowed(replacementSetting, dimensionId))
            .toList();
    }

    private boolean isDimensionAllowed(ReplacementSetting replacementSetting, String dimensionId) {
        List<String> dimensionList = replacementSetting.getDimensionList();
        return dimensionList.contains("*") != dimensionList.contains(dimensionId);
    }

    private void loadSettingsFromStorage() {
        Iterator<String> it = configBasedStorageReader.getStoredConfigs(ReplacementSetting.SUBFOLDER);
        while (it.hasNext()) {
            String identifier = it.next();
            if (!replacementSettings.containsKey(identifier)) {
                ReplacementSetting replacementSetting = new ReplacementSetting(configStateManager, identifier);
                replacementSettings.put(identifier, replacementSetting);
            }
        }
    }
}
