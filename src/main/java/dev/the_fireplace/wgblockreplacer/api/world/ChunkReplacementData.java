package dev.the_fireplace.wgblockreplacer.api.world;

import dev.the_fireplace.wgblockreplacer.capability.ChunkReplacedCapabilityHandler;
import net.minecraft.world.chunk.Chunk;

public interface ChunkReplacementData {
    static ChunkReplacementData getInstance() {
        //noinspection deprecation
        return ChunkReplacedCapabilityHandler.INSTANCE;
    }

    boolean isReplaced(Chunk chunk);
    void markAsReplaced(Chunk chunk);
}
