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
import the_fireplace.wgblockreplacer.translation.SimpleTranslationUtil;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static the_fireplace.wgblockreplacer.WGBlockReplacer.ConfigValues.*;

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
        if (lateReplacement > 0) {
            queueReplacement(event.getWorld(), event.getChunk());
        } else {
            doReplacement(event.getWorld(), event.getChunk());
        }
	}

    private static boolean validateConfig() {
        if (replaceblock.length == replacewith.length
            && replacewith.length == replacewithmeta.length
            && replacewithmeta.length == replaceblockmeta.length
            && replaceblockmeta.length == replacepercent.length
            && replacepercent.length == dimension_list.length
            && dimension_list.length == multiplychance.length
            && multiplychance.length == miny.length
            && miny.length == maxy.length
            && maxy.length == biomefilter.length
        ) {
            return true;
        }
        ArrayList<String> errors = new ArrayList<>(9);
        if (displayWarning) {
            WGBlockReplacer.LOGGER.error(SimpleTranslationUtil.getStringTranslation("wgbr.array_length_mismatch"));
            displayWarning = false;
            int maxLength = max(replaceblock.length, replacewith.length, replacewithmeta.length, replaceblockmeta.length, replacepercent.length, dimension_list.length, multiplychance.length, miny.length, maxy.length, biomefilter.length);
            if (replaceblock.length < maxLength)
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "replaceblock", replaceblock.length, maxLength));
            if (replacewith.length < maxLength)
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "replacewith", replacewith.length, maxLength));
            if (replacewithmeta.length < maxLength)
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "replacewithmeta", replacewithmeta.length, maxLength));
            if (replaceblockmeta.length < maxLength)
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "replaceblockmeta", replaceblockmeta.length, maxLength));
            if (replacepercent.length < maxLength)
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "replacepercent", replacepercent.length, maxLength));
            if (dimension_list.length < maxLength)
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "dimension_list", dimension_list.length, maxLength));
            if (multiplychance.length < maxLength)
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "multiplychance", multiplychance.length, maxLength));
            if (miny.length < maxLength)
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "miny", miny.length, maxLength));
            if (maxy.length < maxLength)
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "maxy", maxy.length, maxLength));
            if (biomefilter.length < maxLength)
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "biomefilter", biomefilter.length, maxLength));

            for (String error: errors) {
                WGBlockReplacer.LOGGER.error(error);
            }
        }
        if (preventLoadOnFailure) {
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
	    TIMERS.putIfAbsent(p, lateReplacement);
    }

    private static void doReplacement(World world, Chunk chunk) {
        for (int i = 0; i < replaceblock.length; i++) {
            if (!canReplaceInDimension(world, dimension_list[i])) {
                continue;
            }

            if (!biomeprecision) {
                Biome approximateBiome = chunk.getBiome(new BlockPos(chunk.getPos().getXStart(), 64, chunk.getPos().getZStart()), world.getBiomeProvider());
                if (!canReplaceInBiome(approximateBiome, biomefilter[i]))
                    continue;
            }

            Block toBlock = Block.getBlockFromName(replacewith[i]);
            Block fromBlock = Block.getBlockFromName(replaceblock[i]);
            if (toBlock == fromBlock && replaceblockmeta == replacewithmeta) {
                continue;
            }
            ArrayList<String> errors = new ArrayList<>();
            ArrayList<String> warnings = new ArrayList<>();
            if (fromBlock == null) {
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.replaceblock_not_found", replaceblock[i]));
            }
            if (toBlock == null) {
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.replacewith_not_found", replacewith[i]));
            }
            if (replaceblockmeta[i] < -1 || replaceblockmeta[i] > 15) {
                warnings.add(SimpleTranslationUtil.getStringTranslation("wgbr.replaceblockmeta_out_of_range", replaceblockmeta[i]));
            }
            if (replacewithmeta[i] < -1 || replacewithmeta[i] > 15) {
                warnings.add(SimpleTranslationUtil.getStringTranslation("wgbr.replacewithmeta_out_of_range", replacewithmeta[i]));
            }
            if (!riskyblocks && toBlock != null && WGBlockReplacer.isBlockRisky(toBlock)) {
                errors.add(SimpleTranslationUtil.getStringTranslation("wgbr.disallowed_block", replacewith[i]));
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
                if (preventLoadOnFailure) {
                    stopServer(errors.toArray(new String[0]));
                }
                continue;
            }

            assert fromBlock != null;
            assert toBlock != null;

            IBlockState fromState;
            if (replaceblockmeta[i] == -1)
                fromState = fromBlock.getDefaultState();
            else
                fromState = fromBlock.getStateFromMeta(replaceblockmeta[i]);

            IBlockState toState;
            if (replacewithmeta[i] == -1)
                toState = toBlock.getDefaultState();
            else
                toState = toBlock.getStateFromMeta(replacewithmeta[i]);

            int storageYIndex = 0;
            for (ExtendedBlockStorage storage : chunk.getBlockStorageArray()) {
                if (storage != null) {
                    for (int storageX = 0; storageX < 16; storageX++) {
                        for (int storageY = 0; storageY < 16; storageY++) {
                            for (int storageZ = 0; storageZ < 16; storageZ++) {
                                if (storage.get(storageX, storageY, storageZ).equals(fromState)) {
                                    int worldY = storageYIndex * 16 + storageY;
                                    if (miny[i] <= worldY
                                        && maxy[i] >= worldY
                                        && rand.nextDouble() * (multiplychance[i] ? worldY : 1) <= replacepercent[i]
                                    ) {
                                        if (biomeprecision) {
                                            Biome biome = chunk.getBiome(new BlockPos(storageX, storageY, storageZ), world.getBiomeProvider());
                                            if (!canReplaceInBiome(biome, biomefilter[i])) {
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
                    if (preventLoadOnFailure)
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
