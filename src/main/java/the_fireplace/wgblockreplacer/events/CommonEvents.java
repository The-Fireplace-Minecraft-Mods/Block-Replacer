package the_fireplace.wgblockreplacer.events;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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
import static the_fireplace.wgblockreplacer.WGBlockReplacer.ConfigValues.*;

import java.util.Random;

@Mod.EventBusSubscriber(modid = WGBlockReplacer.MODID)
public class CommonEvents {
	private static final Random rand = new Random();
	private static boolean displayWarning = true;

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onEvent(PopulateChunkEvent.Pre event) {
		boolean doEvent = ArrayUtils.contains(dimension_list, "*");
		for (String dim : dimension_list)
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

		Block toBlock = Block.getBlockFromName(replacewith);
		Block fromBlock = Block.getBlockFromName(replaceblock);
		if(toBlock == fromBlock && replaceblockmeta == replacewithmeta)
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
		if(displayWarning && replaceblockmeta < -1 || replaceblockmeta > 15) {
			FMLLog.log(Level.ERROR, "[WorldGen Block Replacer] WorldGen Block Replacer might be improperly configured. The block meta is out of the standard range.");
			displayWarning = false;
		}
		if(displayWarning && replacewithmeta < -1 || replacewithmeta > 15) {
			FMLLog.log(Level.ERROR, "[WorldGen Block Replacer] WorldGen Block Replacer might be improperly configured. The replacement block meta is out of the standard range.");
			displayWarning = false;
		}
		if(!riskyblocks && WGBlockReplacer.isBlockRisky(toBlock)) {
			if(displayWarning) {
				FMLLog.log(Level.ERROR, "[WorldGen Block Replacer] WorldGen Block Replacer is configured not to use risky blocks, but you have specified a risky block to be used. Defaulting to Stone instead.");
				displayWarning = false;
			}
			toBlock = Blocks.STONE;
		}

		IBlockState fromState;
		if(replaceblockmeta == -1)
			fromState = fromBlock.getDefaultState();
		else
			fromState = fromBlock.getStateFromMeta(replaceblockmeta);

		IBlockState toState;
		if(replacewithmeta == -1)
			toState = toBlock.getDefaultState();
		else
			toState = toBlock.getStateFromMeta(replacewithmeta);

		int chunkNum = 0;
		for (ExtendedBlockStorage storage : chunk.getBlockStorageArray()) {
			if (storage != null) {
				for (int x = 0; x < 16; x++) {
					for (int y = 0; y < 16; y++) {
						for (int z = 0; z < 16; z++) {
							if (storage.get(x, y, z).equals(fromState)) {
								if(rand.nextDouble()*(multiplychance?(chunkNum*16+y):1) <= replacepercent)
									storage.set(x, y, z, toState);
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
