package the_fireplace.wgblockreplacer.config;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockSponge;
import the_fireplace.wgblockreplacer.api.config.BlockRiskAssessor;

public final class RiskAssessor implements BlockRiskAssessor {
    @Deprecated
    public static final BlockRiskAssessor INSTANCE = new RiskAssessor();
    private RiskAssessor() {}

    @Override
    public boolean isRisky(Block block) {
        return !(block instanceof BlockAir)
            && (
            !block.getDefaultState().isOpaqueCube()
                || !block.getDefaultState().isFullCube()
                || !block.isCollidable()
                || block.hasTileEntity(block.getDefaultState())
                || block instanceof BlockSponge
        );
    }
}
