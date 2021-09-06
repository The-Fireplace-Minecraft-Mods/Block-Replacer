package dev.the_fireplace.wgblockreplacer.api.config;

import dev.the_fireplace.wgblockreplacer.config.RiskAssessor;
import net.minecraft.block.Block;

public interface BlockRiskAssessor {
    static BlockRiskAssessor getInstance() {
        //noinspection deprecation
        return RiskAssessor.INSTANCE;
    }
    boolean isRisky(Block block);
}
