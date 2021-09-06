package the_fireplace.wgblockreplacer.logic;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.wgblockreplacer.api.config.ConfigAccess;
import the_fireplace.wgblockreplacer.api.world.ChunkReplacementData;

import java.util.Random;

public final class Replacer {
    private static final Random rand = new Random();
    private static final ConfigAccess CONFIG = ConfigAccess.getInstance();

    public static void doReplacement(World world, Chunk chunk) {
        for (int index = 0; index < CONFIG.getReplaceBlockIds().length; index++) {
            runReplacementForIndex(world, chunk, index);
        }
        ChunkReplacementData.getInstance().markAsReplaced(chunk);
    }

    private static void runReplacementForIndex(World world, Chunk chunk, int i) {
        if (!canReplaceInDimension(world, CONFIG.getDimensionLists()[i])) {
            return;
        }

        if (!CONFIG.useBiomePrecision()) {
            Biome approximateBiome = chunk.getBiome(new BlockPos(chunk.getPos().getXStart(), 64, chunk.getPos().getZStart()), world.getBiomeProvider());
            if (!canReplaceInBiome(approximateBiome, CONFIG.getBiomeFilterLists()[i])) {
                return;
            }
        }

        Block fromBlock = Block.getBlockFromName(CONFIG.getReplaceBlockIds()[i]);
        Block toBlock = Block.getBlockFromName(CONFIG.getReplaceWithBlockIds()[i]);
        int fromMeta = CONFIG.getReplaceBlockMetas()[i];
        int toMeta = CONFIG.getReplaceWithMetas()[i];
        if (toBlock == fromBlock && fromMeta == toMeta) {
            return;
        }

        assert fromBlock != null;
        assert toBlock != null;

        IBlockState fromState = getBlockState(fromBlock, fromMeta);
        IBlockState toState = getBlockState(toBlock, toMeta);

        boolean modifiedChunk = false;
        for (ExtendedBlockStorage storage : chunk.getBlockStorageArray()) {
            if (storage != null) {
                modifiedChunk = replaceBlocksInStorage(world, chunk, i, fromState, toState, storage) || modifiedChunk;
            }
        }
        if (modifiedChunk) {
            chunk.markDirty();
        }
    }

    private static boolean replaceBlocksInStorage(World world, Chunk chunk, int configArrayIndex, IBlockState fromState, IBlockState toState, ExtendedBlockStorage storage) {
        boolean replaced = false;
        for (int storageX = 0; storageX < 16; storageX++) {
            for (int storageZ = 0; storageZ < 16; storageZ++) {
                if (CONFIG.useBiomePrecision()) {
                    Biome biome = chunk.getBiome(new BlockPos(storageX, 64, storageZ), world.getBiomeProvider());
                    if (!canReplaceInBiome(biome, CONFIG.getBiomeFilterLists()[configArrayIndex])) {
                        continue;
                    }
                }
                for (int storageY = 0; storageY < 16; storageY++) {
                    if (storage.get(storageX, storageY, storageZ).equals(fromState)) {
                        int worldY = storage.getYLocation() + storageY;
                        boolean withinConfiguredYRange = CONFIG.getMinYs()[configArrayIndex] <= worldY && worldY <= CONFIG.getMaxYs()[configArrayIndex];
                        int chanceMultiplier = CONFIG.getMultiplyChances()[configArrayIndex] ? worldY : 1;
                        double replacementChance = CONFIG.getReplaceChances()[configArrayIndex] * chanceMultiplier;
                        if (withinConfiguredYRange
                            && rand.nextDouble() <= replacementChance
                        ) {
                            storage.set(storageX, storageY, storageZ, toState);
                            if (!replaced) {
                                replaced = true;
                            }
                        }
                    }
                }
            }
        }

        return replaced;
    }

    private static IBlockState getBlockState(Block block, int meta) {
        if (meta == -1) {
            return block.getDefaultState();
        }

        return block.getStateFromMeta(meta);
    }

    private static boolean canReplaceInBiome(Biome biome1, String biomeList) {
        boolean doEvent = ArrayUtils.contains(biomeList.split(","), "*");
        for (String biome : biomeList.split(",")) {
            if ("*".equals(biome)) {
                continue;
            }
            if (biome1 == Biome.REGISTRY.getObject(new ResourceLocation(biome))) {
                doEvent = !doEvent;
                break;
            }
        }

        return doEvent;
    }

    private static boolean canReplaceInDimension(World world, String s) {
        boolean doEvent = ArrayUtils.contains(s.split(","), "*");
        for (String dim : s.split(",")) {
            try {
                if (world.provider.getDimension() == Integer.parseInt(dim)) {
                    doEvent = !doEvent;
                    break;
                }
            } catch (NumberFormatException e) {
                if (!dim.equals("*") && world.provider.getDimensionType().getName().equalsIgnoreCase(dim)) {
                    doEvent = !doEvent;
                    break;
                }
            }
        }
        return doEvent;
    }
}
