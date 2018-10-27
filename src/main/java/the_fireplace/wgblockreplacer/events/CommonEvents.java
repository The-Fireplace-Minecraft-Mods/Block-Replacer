package the_fireplace.wgblockreplacer.events;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.wgblockreplacer.WGBlockReplacer;
import static the_fireplace.wgblockreplacer.WGBlockReplacer.ConfigValues.*;

import java.util.Random;

@Mod.EventBusSubscriber(modid = WGBlockReplacer.MODID)
public class CommonEvents {
	private static final Random rand = new Random();
	private static boolean displayWarning = true;

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onEvent(PopulateChunkEvent.Pre event) {
		if(displayWarning && (replaceblock.length != replacewith.length || replacewith.length != replacewithmeta.length || replacewithmeta.length != replaceblockmeta.length || replaceblockmeta.length != replacepercent.length || replacepercent.length != dimension_list.length || dimension_list.length != multiplychance.length || multiplychance.length != miny.length || miny.length != maxy.length || maxy.length != biomefilter.length)){
			System.err.println("[WorldGen Block Replacer] Array lengths do not match!");
			displayWarning = false;
			return;
		}
		for(int i=0;i<replaceblock.length;i++) {
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

			Chunk chunk = event.getWorld().getChunkFromChunkCoords(event.getChunkX(), event.getChunkZ());

			if(!biomeprecision) {
				doEvent = ArrayUtils.contains(biomefilter[i].split(","), "*");
				for (String biome : biomefilter[i].split(","))
					try {
						if (chunk.getBiome(new BlockPos(chunk.getPos().getXStart(), 64, chunk.getPos().getZStart()), event.getWorld().getBiomeProvider()) == Biome.REGISTRY.getObject(new ResourceLocation(biome))) {
							doEvent = !doEvent;
							break;
						}
					} catch (Exception e) {
						if (!biome.equals("*")) {
							System.err.println("[WorldGen Block Replacer] WorldGen Block Replacer is improperly configured. The biome (" + biome + ") was not found.");
							displayWarning = false;
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
					System.err.println("[WorldGen Block Replacer] WorldGen Block Replacer is improperly configured. The block to replace ("+i+") was not found.");
					displayWarning = false;
				}
				continue;
			}
			if (toBlock == null) {
				if (displayWarning) {
					System.err.println("[WorldGen Block Replacer] WorldGen Block Replacer is improperly configured. The block to replace ("+i+") with was not found.");
					displayWarning = false;
				}
				continue;
			}
			if (displayWarning && replaceblockmeta[i] < -1 || replaceblockmeta[i] > 15) {
				System.err.println("[WorldGen Block Replacer] WorldGen Block Replacer might be improperly configured. The block meta ("+i+") is out of the standard range.");
				displayWarning = false;
			}
			if (displayWarning && replacewithmeta[i] < -1 || replacewithmeta[i] > 15) {
				System.err.println("[WorldGen Block Replacer] WorldGen Block Replacer might be improperly configured. The replacement block meta ("+i+") is out of the standard range.");
				displayWarning = false;
			}
			if (!riskyblocks && WGBlockReplacer.isBlockRisky(toBlock)) {
				if (displayWarning) {
					System.err.println("[WorldGen Block Replacer] WorldGen Block Replacer is configured not to use risky blocks, but you have specified a risky block to be used ("+i+"). Defaulting to Stone instead.");
					displayWarning = false;
				}
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
										if(biomeprecision) {
											doEvent = ArrayUtils.contains(biomefilter[i].split(","), "*");
											for (String biome : biomefilter[i].split(","))
												try {
													if (chunk.getBiome(new BlockPos(x, y, z), event.getWorld().getBiomeProvider()) == Biome.REGISTRY.getObject(new ResourceLocation(biome))) {
														doEvent = !doEvent;
														break;
													}
												} catch (Exception e) {
													if (!biome.equals("*")) {
														System.err.println("[WorldGen Block Replacer] WorldGen Block Replacer is improperly configured. The biome (" + biome + ") was not found.");
														displayWarning = false;
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
	}
}
