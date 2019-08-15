package the_fireplace.wgblockreplacer.events;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.wgblockreplacer.WGBlockReplacer;
import the_fireplace.wgblockreplacer.translation.SimpleTranslationUtil;

import static the_fireplace.wgblockreplacer.WGBlockReplacer.ConfigValues.*;

import java.util.Random;

@Mod.EventBusSubscriber(modid = WGBlockReplacer.MODID)
public class CommonEvents {
	private static final Random rand = new Random();
	private static boolean displayWarning = true;

	private static int max(int... args) {
		int max = Integer.MIN_VALUE;
		for(int arg: args)
			if(arg > max)
				max = arg;
		return max;
	}

	@SuppressWarnings("Duplicates")
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onEvent(ChunkEvent.Load event) {
        if(!WGBlockReplacer.hasBeenReplaced(event.getChunk())) {
            if (replaceblock.length != replacewith.length || replacewith.length != replacewithmeta.length || replacewithmeta.length != replaceblockmeta.length || replaceblockmeta.length != replacepercent.length || replacepercent.length != dimension_list.length || dimension_list.length != multiplychance.length || multiplychance.length != miny.length || miny.length != maxy.length || maxy.length != biomefilter.length) {
                if (displayWarning) {
                    WGBlockReplacer.LOGGER.error(SimpleTranslationUtil.getStringTranslation("wgbr.array_length_mismatch"));
                    displayWarning = false;
                    int maxLength = max(replaceblock.length, replacewith.length, replacewithmeta.length, replaceblockmeta.length, replacepercent.length, dimension_list.length, multiplychance.length, miny.length, maxy.length, biomefilter.length);
                    if (replaceblock.length < maxLength)
                        WGBlockReplacer.LOGGER.error(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "replaceblock", replaceblock.length, maxLength));
                    if (replacewith.length < maxLength)
                        WGBlockReplacer.LOGGER.error(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "replacewith", replacewith.length, maxLength));
                    if (replacewithmeta.length < maxLength)
                        WGBlockReplacer.LOGGER.error(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "replacewithmeta", replacewithmeta.length, maxLength));
                    if (replaceblockmeta.length < maxLength)
                        WGBlockReplacer.LOGGER.error(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "replaceblockmeta", replaceblockmeta.length, maxLength));
                    if (replacepercent.length < maxLength)
                        WGBlockReplacer.LOGGER.error(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "replacepercent", replacepercent.length, maxLength));
                    if (dimension_list.length < maxLength)
                        WGBlockReplacer.LOGGER.error(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "dimension_list", dimension_list.length, maxLength));
                    if (multiplychance.length < maxLength)
                        WGBlockReplacer.LOGGER.error(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "multiplychance", multiplychance.length, maxLength));
                    if (miny.length < maxLength)
                        WGBlockReplacer.LOGGER.error(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "miny", miny.length, maxLength));
                    if (maxy.length < maxLength)
                        WGBlockReplacer.LOGGER.error(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "maxy", maxy.length, maxLength));
                    if (biomefilter.length < maxLength)
                        WGBlockReplacer.LOGGER.error(SimpleTranslationUtil.getStringTranslation("wgbr.mismatch_length", "biomefilter", biomefilter.length, maxLength));
                }
                if (preventLoadOnFailure)
                    stopServer();
                return;
            }
            for (int i = 0; i < replaceblock.length; i++) {
                boolean doEvent = ArrayUtils.contains(dimension_list[i].split(","), "*");
                for (String dim : dimension_list[i].split(","))
                    try {
                        if (event.getWorld().provider.getDimension() == Integer.parseInt(dim)) {
                            doEvent = !doEvent;
                            break;
                        }
                    } catch (NumberFormatException e) {
                        if (!dim.equals("*") && event.getWorld().provider.getDimensionType().getName().toLowerCase().equals(dim.toLowerCase())) {
                            doEvent = !doEvent;
                            break;
                        }
                    }
                if (!doEvent)
                    continue;

                Chunk chunk = event.getChunk();

                if (!biomeprecision) {
                    doEvent = ArrayUtils.contains(biomefilter[i].split(","), "*");
                    for (String biome : biomefilter[i].split(","))
                        try {
                            if (chunk.getBiome(new BlockPos(chunk.getPos().getXStart(), 64, chunk.getPos().getZStart()), event.getWorld().getBiomeProvider()) == Biome.REGISTRY.getObject(new ResourceLocation(biome))) {
                                doEvent = !doEvent;
                                break;
                            }
                        } catch (Exception e) {
                            if (!biome.equals("*")) {
                                if (displayWarning) {
                                    WGBlockReplacer.LOGGER.error(SimpleTranslationUtil.getStringTranslation("wgbr.biome_not_found", biome));
                                    displayWarning = false;
                                }
                                if (preventLoadOnFailure)
                                    stopServer();
                            }
                        }
                    if (!doEvent)
                        continue;
                }

                Block toBlock = Block.getBlockFromName(replacewith[i]);
                Block fromBlock = Block.getBlockFromName(replaceblock[i]);
                if (toBlock == fromBlock && replaceblockmeta == replacewithmeta)
                    continue;
                if (fromBlock == null) {
                    if (displayWarning) {
                        WGBlockReplacer.LOGGER.error(SimpleTranslationUtil.getStringTranslation("wgbr.replaceblock_not_found", replaceblock[i]));
                        displayWarning = false;
                    }
                    if (preventLoadOnFailure)
                        stopServer();
                    continue;
                }
                if (toBlock == null) {
                    if (displayWarning) {
                        WGBlockReplacer.LOGGER.error(SimpleTranslationUtil.getStringTranslation("wgbr.replacewith_not_found", replacewith[i]));
                        displayWarning = false;
                    }
                    if (preventLoadOnFailure)
                        stopServer();
                    continue;
                }
                if (displayWarning && replaceblockmeta[i] < -1 || replaceblockmeta[i] > 15) {
                    WGBlockReplacer.LOGGER.error(SimpleTranslationUtil.getStringTranslation("wgbr.replaceblockmeta_out_of_range", replaceblockmeta[i]));
                    displayWarning = false;
                }
                if (displayWarning && replacewithmeta[i] < -1 || replacewithmeta[i] > 15) {
                    WGBlockReplacer.LOGGER.error(SimpleTranslationUtil.getStringTranslation("wgbr.replacewithmeta_out_of_range", replacewithmeta[i]));
                    displayWarning = false;
                }
                if (!riskyblocks && WGBlockReplacer.isBlockRisky(toBlock)) {
                    if (displayWarning) {
                        WGBlockReplacer.LOGGER.error(SimpleTranslationUtil.getStringTranslation("wgbr.disallowed_block", replacewith[i]));
                        displayWarning = false;
                    }
                    if (preventLoadOnFailure)
                        stopServer();
                    toBlock = Blocks.STONE;
                }

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

                int chunkNum = 0;
                for (ExtendedBlockStorage storage : chunk.getBlockStorageArray()) {
                    if (storage != null)
                        for (int x = 0; x < 16; x++)
                            for (int y = 0; y < 16; y++)
                                for (int z = 0; z < 16; z++)
                                    if (storage.get(x, y, z).equals(fromState))
                                        if (miny[i] <= (chunkNum * 16 + y) && maxy[i] >= (chunkNum * 16 + y) && rand.nextDouble() * (multiplychance[i] ? (chunkNum * 16 + y) : 1) <= replacepercent[i]) {
                                            if (biomeprecision) {
                                                doEvent = ArrayUtils.contains(biomefilter[i].split(","), "*");
                                                for (String biome : biomefilter[i].split(","))
                                                    try {
                                                        if (chunk.getBiome(new BlockPos(x, y, z), event.getWorld().getBiomeProvider()) == Biome.REGISTRY.getObject(new ResourceLocation(biome))) {
                                                            doEvent = !doEvent;
                                                            break;
                                                        }
                                                    } catch (Exception e) {
                                                        if (!biome.equals("*")) {
                                                            if (displayWarning) {
                                                                WGBlockReplacer.LOGGER.error(SimpleTranslationUtil.getStringTranslation("wgbr.biome_not_found", biome));
                                                                displayWarning = false;
                                                            }
                                                            if (preventLoadOnFailure)
                                                                stopServer();
                                                        }
                                                    }
                                                if (doEvent)
                                                    storage.set(x, y, z, toState);
                                            } else
                                                storage.set(x, y, z, toState);
                                        }
                    chunkNum++;
                }
                chunk.markDirty();
            }
            WGBlockReplacer.setReplaced(event.getChunk());
        }
	}

	private static void stopServer() {
		FMLCommonHandler.instance().getMinecraftServerInstance().stopServer();
		throw new RuntimeException(SimpleTranslationUtil.getStringTranslation("wgbr.shutdown"));
	}
}
