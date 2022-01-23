package dev.the_fireplace.wgblockreplacer.domain.world;

import net.minecraft.world.chunk.Chunk;

public interface ChunkReplacementData
{

    boolean needsReplacement(Chunk chunk);

    boolean needsRetrogen(Chunk chunk);

    void markAsReplaced(Chunk chunk);
}
