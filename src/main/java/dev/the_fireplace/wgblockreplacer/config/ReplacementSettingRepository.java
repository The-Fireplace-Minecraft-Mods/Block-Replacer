package dev.the_fireplace.wgblockreplacer.config;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.wgblockreplacer.domain.config.ReplacementSettings;

import javax.inject.Singleton;
import java.util.List;

@Singleton
@Implementation
public final class ReplacementSettingRepository implements ReplacementSettings
{

    @Override
    public List<ReplacementSetting> findBy(String dimensionId) {
        return null;
    }
}
