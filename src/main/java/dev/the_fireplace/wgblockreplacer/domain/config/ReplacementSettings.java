package dev.the_fireplace.wgblockreplacer.domain.config;

import dev.the_fireplace.wgblockreplacer.config.ReplacementSetting;

import java.util.List;

public interface ReplacementSettings
{
    List<ReplacementSetting> findBy(String dimensionId);
}
