package the_fireplace.wgblockreplacer;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.ArrayUtils;
import static the_fireplace.wgblockreplacer.WGBlockReplacer.cfg.*;

import java.util.Objects;
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
	public static void onEvent(ChunkGeneratorEvent.ReplaceBiomeBlocks event) {
		if(replaceBlocks.size() != replacements.size() || replacements.size() !=  replacePercents.size() || replacePercents.size() != dimensionFilters.size() || dimensionFilters.size() != multiplyChances.size() || multiplyChances.size() != minYs.size() || minYs.size() != maxYs.size() || maxYs.size() != biomeFilters.size()) {
			if (displayWarning) {
				WGBlockReplacer.LOGGER.error("Array sizes do not match!");
				displayWarning = false;
				int maxLength = max(replaceBlocks.size(), replacements.size(), replacePercents.size(), dimensionFilters.size(), multiplyChances.size(), minYs.size(), maxYs.size(), biomeFilters.size());
				if(replaceBlocks.size() < maxLength)
					WGBlockReplacer.LOGGER.error("replaceBlocks size was %s, expected %s", replaceBlocks.size(), maxLength);
				if(replacements.size() < maxLength)
					WGBlockReplacer.LOGGER.error("replacements size was %s, expected %s", replacements.size(), maxLength);
				if(replacePercents.size() < maxLength)
					WGBlockReplacer.LOGGER.error("replacePercents size was %s, expected %s", replacePercents.size(), maxLength);
				if(dimensionFilters.size() < maxLength)
					WGBlockReplacer.LOGGER.error("dimensionFilters size was %s, expected %s", dimensionFilters.size(), maxLength);
				if(multiplyChances.size() < maxLength)
					WGBlockReplacer.LOGGER.error("multiplyChances size was %s, expected %s", multiplyChances.size(), maxLength);
				if(minYs.size() < maxLength)
					WGBlockReplacer.LOGGER.error("minYs size was %s, expected %s", minYs.size(), maxLength);
				if(maxYs.size() < maxLength)
					WGBlockReplacer.LOGGER.error("maxYs size was %s, expected %s", maxYs.size(), maxLength);
				if(biomeFilters.size() < maxLength)
					WGBlockReplacer.LOGGER.error("biomeFilters size was %s, expected %s", biomeFilters.size(), maxLength);
			}
			if(preventLoadOnFailure)
				killServer();
			return;
		}

		IChunk chunk = event.getChunk();

		for(int i = 0; i < replaceBlocks.size(); i++) {
			boolean doEvent = ArrayUtils.contains(dimensionFilters.get(i).split(","), "*");
			for (String dim : dimensionFilters.get(i).split(","))
				try {
					if (event.getWorld().getDimension().getType().getId() == Integer.parseInt(dim)) {
						doEvent = !doEvent;
						break;
					}
				} catch (NumberFormatException e) {
					if (!dim.equals("*") && event.getWorld().getDimension().getType().getRegistryName() != null && (event.getWorld().getDimension().getType().getRegistryName().getPath().toLowerCase().equals(dim.toLowerCase()) || event.getWorld().getDimension().getType().getRegistryName().toString().toLowerCase().equals(dim.toLowerCase()))) {
						doEvent = !doEvent;
						break;
					}
				}
			if (!doEvent)
				continue;

			if(!biomeFilterPrecision) {
				doEvent = ArrayUtils.contains(biomeFilters.get(i).split(","), "*");
				for (String biome : biomeFilters.get(i).split(","))
					try {
						if (Objects.requireNonNull(chunk.getWorldForge()).getBiome(new BlockPos(chunk.getPos().getXStart(), 64, chunk.getPos().getZStart())) == ForgeRegistries.BIOMES.getValue(new ResourceLocation(biome))) {
							doEvent = !doEvent;
							break;
						}
					} catch (Exception e) {
						if (!biome.equals("*")) {
							if(displayWarning) {
								WGBlockReplacer.LOGGER.error("WorldGen Block Replacer is improperly configured. The biome (" + biome + ") was not found.");
								displayWarning = false;
							}
							if(preventLoadOnFailure)
								killServer();
						}
					}
				if (!doEvent)
					continue;
			}

			Block toBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(replacements.get(i)));
			Block fromBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(replaceBlocks.get(i)));
			if (fromBlock == null) {
				if (displayWarning) {
					WGBlockReplacer.LOGGER.error("WorldGen Block Replacer is improperly configured. The block to replace ("+i+") was not found.");
					displayWarning = false;
				}
				if(preventLoadOnFailure)
					killServer();
				continue;
			}
			if (toBlock == null) {
				if (displayWarning) {
					WGBlockReplacer.LOGGER.error("WorldGen Block Replacer is improperly configured. The replacement ("+i+") was not found.");
					displayWarning = false;
				}
				if(preventLoadOnFailure)
					killServer();
				continue;
			}
			if (toBlock == fromBlock) {
				if(displayWarning) {
					WGBlockReplacer.LOGGER.error("WorldGen Block Replacer is improperly configured. The replacement for (%d) is itself.", i);
					displayWarning = false;
				}
				if(preventLoadOnFailure)
					killServer();
				continue;
			}
			if (!allowRiskyReplacements && WGBlockReplacer.isBlockRisky(toBlock)) {
				if (displayWarning) {
					WGBlockReplacer.LOGGER.error("WorldGen Block Replacer is configured not to use risky blocks, but you have specified a risky block to be used ("+i+"). Defaulting to Stone instead.");
					displayWarning = false;
				}
				if(preventLoadOnFailure)
					killServer();
				toBlock = Blocks.STONE;
			}

			IBlockState fromState = fromBlock.getDefaultState();
			IBlockState toState = toBlock.getDefaultState();

			int chunkNum = 0;
			boolean modified = false;
			for (ChunkSection storage : chunk.getSections()) {
				if (storage != null)
					for (int x = 0; x < 16; x++)
						for (int y = 0; y < 16; y++)
							for (int z = 0; z < 16; z++)
								if (storage.get(x, y, z).equals(fromState))
									if (minYs.get(i) <= (chunkNum * 16 + y) && maxYs.get(i) >= (chunkNum * 16 + y) && rand.nextDouble() * (multiplyChances.get(i) ? (chunkNum * 16 + y) : 1) <= replacePercents.get(i)) {
										if(biomeFilterPrecision) {
											doEvent = ArrayUtils.contains(biomeFilters.get(i).split(","), "*");
											for (String biome : biomeFilters.get(i).split(","))
												try {
													if (Objects.requireNonNull(chunk.getWorldForge()).getBiome(new BlockPos(x, y, z)).equals(ForgeRegistries.BIOMES.getValue(new ResourceLocation(biome)))) {
														doEvent = !doEvent;
														break;
													}
												} catch (Exception e) {
													if (!biome.equals("*")) {
														if(displayWarning) {
															WGBlockReplacer.LOGGER.error("WorldGen Block Replacer is improperly configured. The biome (" + biome + ") was not found.");
															displayWarning = false;
														}
														if(preventLoadOnFailure)
															killServer();
													}
												}
											if (doEvent) {
												storage.set(x, y, z, toState);
												modified = true;
											}
										} else {
											storage.set(x, y, z, toState);
											modified = true;
										}
									}
				chunkNum++;
			}
			if(modified)
				if(chunk instanceof Chunk)
					((Chunk) chunk).markDirty();
				else if(chunk instanceof ChunkPrimer)
					((ChunkPrimer) chunk).setModified(true);
		}
	}

	private static void killServer() {
		ServerLifecycleHooks.getCurrentServer().initiateShutdown();
	}
}
