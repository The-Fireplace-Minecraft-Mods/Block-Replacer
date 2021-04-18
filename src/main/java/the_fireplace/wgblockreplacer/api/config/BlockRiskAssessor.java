package the_fireplace.wgblockreplacer.api.config;

import net.minecraft.block.Block;
import the_fireplace.wgblockreplacer.config.RiskAssessor;

public interface BlockRiskAssessor {
    static BlockRiskAssessor getInstance() {
        //noinspection deprecation
        return RiskAssessor.INSTANCE;
    }
    boolean isRisky(Block block);
}
