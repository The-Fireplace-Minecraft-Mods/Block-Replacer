package the_fireplace.wgblockreplacer.events;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import the_fireplace.wgblockreplacer.WGBlockReplacer;
import the_fireplace.wgblockreplacer.api.config.ConfigAccess;
import the_fireplace.wgblockreplacer.translation.SimpleTranslationUtil;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = WGBlockReplacer.MODID)
public class CommonEvents {
	private static final Random rand = new Random();
	private static boolean displayWarning = true;

	private static int max(int... args) {
		int max = Integer.MIN_VALUE;
		for (int arg: args) {
            if (arg > max) {
                max = arg;
            }
        }
		return max;
	}

	@SuppressWarnings("Duplicates")
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onEvent(ChunkEvent.Load event) {
	    if (event.getWorld().isRemote
            || WGBlockReplacer.hasBeenReplaced(event.getChunk())
            || !validateConfig()
        ) {
            return;
        }
        if (ConfigAccess.getInstance().getLateReplacementTicks() > 0) {
            queueReplacement(event.getWorld(), event.getChunk());
        } else {
            doReplacement(event.getWorld(), event.getChunk());
        }
	}

    private static boolean validateConfig() {
        if (ConfigAccess.getInstance().getReplaceBlockIds().length == ConfigAccess.getInstance().getReplaceWithIds().length
            && ConfigAccess.getInstance().getReplaceWithIds().length == ConfigAccess.getInstance().getReplaceWithMetas().length
            && ConfigAccess.getInstance().getReplaceWithMetas().length == ConfigAccess.getInstance().getReplaceBlockMetas().length
            && ConfigAccess.getInstance().getReplaceBlockMetas().length == ConfigAccess.getInstance().getReplaceChances().length
            && ConfigAccess.getInstance().getReplaceChances().length == ConfigAccess.getInstance().getDimensionLists().length
            && ConfigAccess.getInstance().getDimensionLists().length == ConfigAccess.getInstance().getMultiplyChances().length
            && ConfigAccess.getInstance().getMultiplyChances().length == ConfigAccess.getInstance().getMinYs().length
            && ConfigAccess.getInstance().getMinYs().length == ConfigAccess.getInstance().getMaxYs().length
            && ConfigAccess.getInstance().getMaxYs().length == ConfigAccess.getInstance().getBiomeFilterLists().length
        ) {
            return true;
        }
        ArrayList<String> errors = new ArrayList<>(9);
        if (displayWarning) {
            WGBlockReplacer.LOGGER.error(SimpleTranslationUtil.getStringTranslation("wgbr.array_length_mismatch"));
            displayWarning = false;
            int maxLength = max(ConfigAccess.getInstance().getReplaceBlockIds().length, ConfigAccess.getInstance().getReplaceWithIds().length, ConfigAccess.getInstance().getReplaceWithMetas().length, ConfigAccess.getInstance().getReplaceBlockMetas().length, ConfigAccess.getInstance().getReplaceChances().length, ConfigAccess.getInstance().getDimensionLists().length, ConfigAccess.getInstance().getMultiplyChances().length, ConfigAccess.getInstance().getMinYs().length, ConfigAccess.getInstance().getMaxYs().length, ConfigAccess.getInstance().getBiomeFilterLists().length);
            if (ConfigAccess.getInstance().getReplaceBlockIds().length < maxLength)
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "replaceblock", ConfigAccess.getInstance().getReplaceBlockIds().length, maxLength));
            if (ConfigAccess.getInstance().getReplaceWithIds().length < maxLength)
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "replacewith", ConfigAccess.getInstance().getReplaceWithIds().length, maxLength));
            if (ConfigAccess.getInstance().getReplaceWithMetas().length < maxLength)
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "replacewithmeta", ConfigAccess.getInstance().getReplaceWithMetas().length, maxLength));
            if (ConfigAccess.getInstance().getReplaceBlockMetas().length < maxLength)
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "replaceblockmeta", ConfigAccess.getInstance().getReplaceBlockMetas().length, maxLength));
            if (ConfigAccess.getInstance().getReplaceChances().length < maxLength)
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "replacepercent", ConfigAccess.getInstance().getReplaceChances().length, maxLength));
            if (ConfigAccess.getInstance().getDimensionLists().length < maxLength)
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "dimension_list", ConfigAccess.getInstance().getDimensionLists().length, maxLength));
            if (ConfigAccess.getInstance().getMultiplyChances().length < maxLength)
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "multiplychance", ConfigAccess.getInstance().getMultiplyChances().length, maxLength));
            if (ConfigAccess.getInstance().getMinYs().length < maxLength)
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "miny", ConfigAccess.getInstance().getMinYs().length, maxLength));
            if (ConfigAccess.getInstance().getMaxYs().length < maxLength)
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "maxy", ConfigAccess.getInstance().getMaxYs().length, maxLength));
            if (ConfigAccess.getInstance().getBiomeFilterLists().length < maxLength)
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "biomefilter", ConfigAccess.getInstance().getBiomeFilterLists().length, maxLength));

            for (String error: errors) {
                WGBlockReplacer.LOGGER.error(error);
            }
        }
        if (ConfigAccess.getInstance().preventLoadOnFailure()) {
            stopServer(errors.toArray(new String[0]));
        }
        return false;
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.ServerTickEvent event) {
	    for (Map.Entry<Pair<World, Chunk>, Integer> p: TIMERS.entrySet()) {
	        if (p.getValue() <= 0) {
                TIMERS.remove(p.getKey());
	            doReplacement(p.getKey().getLeft(), p.getKey().getRight());
            } else {
                TIMERS.put(p.getKey(), p.getValue() - 1);
            }
        }
    }

	private static final Map<Pair<World, Chunk>, Integer> TIMERS = new ConcurrentHashMap<>();

    private static void queueReplacement(World world, Chunk chunk) {
	    Pair<World, Chunk> p = Pair.of(world, chunk);
	    TIMERS.putIfAbsent(p, ConfigAccess.getInstance().getLateReplacementTicks());
    }

    private static void doReplacement(World world, Chunk chunk) {
        for (int i = 0; i < ConfigAccess.getInstance().getReplaceBlockIds().length; i++) {
            if (!canReplaceInDimension(world, ConfigAccess.getInstance().getDimensionLists()[i])) {
                continue;
            }

            if (!ConfigAccess.getInstance().useBiomePrecision()) {
                Biome approximateBiome = chunk.getBiome(new BlockPos(chunk.getPos().getXStart(), 64, chunk.getPos().getZStart()), world.getBiomeProvider());
                if (!canReplaceInBiome(approximateBiome, ConfigAccess.getInstance().getBiomeFilterLists()[i]))
                    continue;
            }

            Block toBlock = Block.getBlockFromName(ConfigAccess.getInstance().getReplaceWithIds()[i]);
            Block fromBlock = Block.getBlockFromName(ConfigAccess.getInstance().getReplaceBlockIds()[i]);
            if (toBlock == fromBlock && ConfigAccess.getInstance().getReplaceBlockMetas() == ConfigAccess.getInstance().getReplaceWithMetas()) {
                continue;
            }
            ArrayList<String> errors = new ArrayList<>();
            ArrayList<String> warnings = new ArrayList<>();
            if (fromBlock == null) {
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.replaceblock_not_found", ConfigAccess.getInstance().getReplaceBlockIds()[i]));
            }
            if (toBlock == null) {
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.replacewith_not_found", ConfigAccess.getInstance().getReplaceWithIds()[i]));
            }
            if (ConfigAccess.getInstance().getReplaceBlockMetas()[i] < -1 || ConfigAccess.getInstance().getReplaceBlockMetas()[i] > 15) {
                warnings.add(SimpleTranslationUtil.getStringTranslation("wgbr.replaceblockmeta_out_of_range", ConfigAccess.getInstance().getReplaceBlockMetas()[i]));
            }
            if (ConfigAccess.getInstance().getReplaceWithMetas()[i] < -1 || ConfigAccess.getInstance().getReplaceWithMetas()[i] > 15) {
                warnings.add(SimpleTranslationUtil.getStringTranslation("wgbr.replacewithmeta_out_of_range", ConfigAccess.getInstance().getReplaceWithMetas()[i]));
            }
            if (!ConfigAccess.getInstance().allowRiskyBlocks() && toBlock != null && WGBlockReplacer.isBlockRisky(toBlock)) {
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.disallowed_block", ConfigAccess.getInstance().getReplaceWithIds()[i]));
                toBlock = Blocks.STONE;
            }

            if (displayWarning) {
                displayWarning = false;
                for (String error: errors) {
                    WGBlockReplacer.LOGGER.error(error);
                }
                for (String warning: warnings) {
                    WGBlockReplacer.LOGGER.warn(warning);
                }
            }

            if (!errors.isEmpty()) {
                if (ConfigAccess.getInstance().preventLoadOnFailure()) {
                    stopServer(errors.toArray(new String[0]));
                }
                continue;
            }

            assert fromBlock != null;
            assert toBlock != null;

            IBlockState fromState;
            if (ConfigAccess.getInstance().getReplaceBlockMetas()[i] == -1)
                fromState = fromBlock.getDefaultState();
            else
                fromState = fromBlock.getStateFromMeta(ConfigAccess.getInstance().getReplaceBlockMetas()[i]);

            IBlockState toState;
            if (ConfigAccess.getInstance().getReplaceWithMetas()[i] == -1)
                toState = toBlock.getDefaultState();
            else
                toState = toBlock.getStateFromMeta(ConfigAccess.getInstance().getReplaceWithMetas()[i]);

            int storageYIndex = 0;
            for (ExtendedBlockStorage storage : chunk.getBlockStorageArray()) {
                if (storage != null) {
                    for (int storageX = 0; storageX < 16; storageX++) {
                        for (int storageY = 0; storageY < 16; storageY++) {
                            for (int storageZ = 0; storageZ < 16; storageZ++) {
                                if (storage.get(storageX, storageY, storageZ).equals(fromState)) {
                                    int worldY = storageYIndex * 16 + storageY;
                                    if (ConfigAccess.getInstance().getMinYs()[i] <= worldY
                                        && ConfigAccess.getInstance().getMaxYs()[i] >= worldY
                                        && rand.nextDouble() * (ConfigAccess.getInstance().getMultiplyChances()[i] ? worldY : 1) <= ConfigAccess.getInstance().getReplaceChances()[i]
                                    ) {
                                        if (ConfigAccess.getInstance().useBiomePrecision()) {
                                            Biome biome = chunk.getBiome(new BlockPos(storageX, storageY, storageZ), world.getBiomeProvider());
                                            if (!canReplaceInBiome(biome, ConfigAccess.getInstance().getBiomeFilterLists()[i])) {
                                                continue;
                                            }
                                        }
                                        storage.set(storageX, storageY, storageZ, toState);
                                    }
                                }
                            }
                        }
                    }
                }
                storageYIndex++;
            }
            chunk.markDirty();
        }
        WGBlockReplacer.setReplaced(chunk);
    }

    private static boolean canReplaceInBiome(Biome biome1, String s) {
        boolean doEvent = ArrayUtils.contains(s.split(","), "*");
        for (String biome : s.split(",")) {
            String error = SimpleTranslationUtil.getStringTranslation("wgbr.biome_not_found", biome);
            try {
                if (biome1 == Biome.REGISTRY.getObject(new ResourceLocation(biome))) {
                    doEvent = !doEvent;
                    break;
                }
            } catch (Exception e) {
                if (!biome.equals("*")) {
                    if (displayWarning) {
                        WGBlockReplacer.LOGGER.error(error);
                        displayWarning = false;
                    }
                    if (ConfigAccess.getInstance().preventLoadOnFailure())
                        stopServer(new String[]{error});
                }
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

    private static void stopServer(String[] errors) {
		FMLCommonHandler.instance().getMinecraftServerInstance().stopServer();
		throw new RuntimeException(SimpleTranslationUtil.getStringTranslation("wgbr.shutdown", String.join(", ", errors)));
	}
}
