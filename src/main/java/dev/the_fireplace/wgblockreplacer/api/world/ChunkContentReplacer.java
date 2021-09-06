package dev.the_fireplace.wgblockreplacer.api.world;

public interface ChunkContentReplacer {
    static ChunkContentReplacer getInstance() {
        return null;
    }

    void replace();
}
