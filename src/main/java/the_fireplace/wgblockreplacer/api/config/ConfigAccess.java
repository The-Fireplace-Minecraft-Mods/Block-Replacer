package the_fireplace.wgblockreplacer.api.config;

import the_fireplace.wgblockreplacer.WGBlockReplacer;

public interface ConfigAccess {
    static ConfigAccess getInstance() {
        //noinspection deprecation
        return WGBlockReplacer.ConfigValues.INSTANCE;
    }

    int[] getReplaceBlockMetas();

    String[] getReplaceWithIds();

    int[] getReplaceWithMetas();

    boolean allowRiskyBlocks();

    String[] getDimensionLists();

    double[] getReplaceChances();

    boolean[] getMultiplyChances();

    int[] getMinYs();

    int[] getMaxYs();

    String[] getBiomeFilterLists();

    boolean useBiomePrecision();

    boolean preventLoadOnFailure();

    int getLateReplacementTicks();

    String getReplacementChunkKey();

    String[] getReplaceBlockIds();
}
