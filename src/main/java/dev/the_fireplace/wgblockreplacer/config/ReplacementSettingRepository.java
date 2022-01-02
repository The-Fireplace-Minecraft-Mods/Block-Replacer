package dev.the_fireplace.wgblockreplacer.config;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.lib.api.io.injectables.ConfigBasedStorageReader;
import dev.the_fireplace.wgblockreplacer.domain.config.ReplacementSettings;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
@Implementation
public final class ReplacementSettingRepository implements ReplacementSettings
{
    private final List<ReplacementSetting> replacementSettings = new ArrayList<>();
    private final ConfigBasedStorageReader configBasedStorageReader;

    @Inject
    public ReplacementSettingRepository(ConfigBasedStorageReader configBasedStorageReader) {
        this.configBasedStorageReader = configBasedStorageReader;
    }

    @Override
    public List<ReplacementSetting> findBy(String dimensionId) {
        loadSettingsFromStorage();
        return replacementSettings.stream()
            .filter(replacementSetting -> isDimensionAllowed(replacementSetting, dimensionId))
            .toList();
    }

    private boolean isDimensionAllowed(ReplacementSetting replacementSetting, String dimensionId) {
        List<String> dimensionList = replacementSetting.getDimensionList();
        return dimensionList.contains("*") != dimensionList.contains(dimensionId);
    }

    private void loadSettingsFromStorage() {
        /*for (String identifier : configBasedStorageReader.getStoredIdsIterator()) {

        }*/
    }
}
