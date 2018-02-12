package the_fireplace.wgblockreplacer.events;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Level;
import the_fireplace.wgblockreplacer.WGBlockReplacer;

import java.util.Random;

@Mod.EventBusSubscriber(modid = WGBlockReplacer.MODID)
public class CommonEvents {
	private static final Random rand = new Random();
	private static boolean displayWarning = true;

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onEvent(PopulateChunkEvent.Pre event) {
		boolean doEvent = ArrayUtils.contains(WGBlockReplacer.ConfigValues.dimension_list, "*");
		for (String dim : WGBlockReplacer.ConfigValues.dimension_list)
				try{
					if(event.getWorld().provider.getDimension() == Integer.parseInt(dim)) {
						doEvent = !doEvent;
						break;
					}
				}catch(NumberFormatException e){
					if(!dim.equals("*") && event.getWorld().provider.getDimensionType().getName().toLowerCase().equals(dim.toLowerCase())){
						doEvent = !doEvent;
						break;
					}
				}
		if(!doEvent)
			return;

		Chunk chunk = event.getWorld().getChunkFromChunkCoords(event.getChunkX(), event.getChunkZ());

		Block toBlock = Block.getBlockFromName(WGBlockReplacer.ConfigValues.replacewith);
		Block fromBlock = Block.getBlockFromName(WGBlockReplacer.ConfigValues.replaceblock);
		if(toBlock == fromBlock)
			return;
		if(fromBlock == null) {
			if(displayWarning) {
				FMLLog.log(Level.ERROR, "[WorldGen Block Replacer] WorldGen Block Replacer is improperly configured. The block to replace was not found.");
				displayWarning = false;
			}
			return;
		}
		if(toBlock == null) {
			if(displayWarning) {
				FMLLog.log(Level.ERROR, "[WorldGen Block Replacer] WorldGen Block Replacer is improperly configured. The block to replace with was not found.");
				displayWarning = false;
			}
			return;
		}
		if(!WGBlockReplacer.ConfigValues.riskyblocks && WGBlockReplacer.isBlockRisky(toBlock)) {
			if(displayWarning) {
				FMLLog.log(Level.ERROR, "[WorldGen Block Replacer] WorldGen Block Replacer is configured not to use risky blocks, but you have specified a risky block to be used. Defaulting to Stone instead.");
				displayWarning = false;
			}
			toBlock = Blocks.STONE;
		}

		int chunkNum = 0;
		for (ExtendedBlockStorage storage : chunk.getBlockStorageArray()) {
			if (storage != null) {
				for (int x = 0; x < 16; x++) {
					for (int y = 0; y < 16; y++) {
						for (int z = 0; z < 16; z++) {
							if (storage.get(x, y, z).equals(fromBlock.getDefaultState())) {
								if(rand.nextDouble()*(WGBlockReplacer.ConfigValues.multiplychance?(chunkNum*16+y):1) <= WGBlockReplacer.ConfigValues.replacepercent)
									storage.set(x, y, z, toBlock.getDefaultState());
							}
						}
					}
				}
			}
			chunkNum++;
		}
		chunk.setModified(true);
	}
}
