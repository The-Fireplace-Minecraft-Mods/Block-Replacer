package dev.the_fireplace.wgblockreplacer.world;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.wgblockreplacer.domain.world.ChunkReplacementData;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import javax.inject.Singleton;

@Implementation
@Singleton
public class ChunkReplacementDataStorage implements ChunkReplacementData
{
    @Override
    public boolean needsReplacement(Chunk chunk) {
        //TODO
        return false;
    }

    @Override
    public boolean needsRetrogen(Chunk chunk) {
        return chunk.getStatus().isAtLeast(ChunkStatus.FULL) && needsReplacement(chunk);
    }

    @Override
    public void markAsReplaced(Chunk chunk) {
        //TODO
    }
}
