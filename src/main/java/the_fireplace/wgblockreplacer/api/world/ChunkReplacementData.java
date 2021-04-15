package the_fireplace.wgblockreplacer.api.world;

import net.minecraft.world.chunk.Chunk;
import the_fireplace.wgblockreplacer.capability.ChunkReplacedCapabilityHandler;

public interface ChunkReplacementData {
    static ChunkReplacementData getInstance() {
        //noinspection deprecation
        return ChunkReplacedCapabilityHandler.INSTANCE;
    }

    boolean isReplaced(Chunk chunk);
    void markAsReplaced(Chunk chunk);
}
