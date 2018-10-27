package the_fireplace.wgblockreplacer;

import net.minecraft.block.Block;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import the_fireplace.wgblockreplacer.proxy.Common;

@Mod(modid = WGBlockReplacer.MODID, name = WGBlockReplacer.MODNAME, guiFactory = "the_fireplace.wgblockreplacer.config.WGBRGuiFactory", canBeDeactivated = true, acceptedMinecraftVersions = "[1.12,1.13)", acceptableRemoteVersions = "*")
public class WGBlockReplacer {
	public static final String MODID = "wgblockreplacer";
	public static final String MODNAME = "WorldGen Block Replacer";

	@SidedProxy(clientSide = "the_fireplace.wgblockreplacer.proxy.Client", serverSide = "the_fireplace.wgblockreplacer.proxy.Common")
	public static Common proxy;

	@EventHandler
	@SuppressWarnings("unchecked")
	public void postInit(FMLPostInitializationEvent event) {
		if(event.getSide().isClient())
			proxy.initBlockList();
	}

	public static boolean isBlockRisky(Block block) {
		return !block.getDefaultState().isOpaqueCube() || !block.getDefaultState().isFullCube() || !block.isCollidable() || block.hasTileEntity(block.getDefaultState());
	}

	@Config(modid = MODID)
	public static class ConfigValues{
		@Config.Comment("The block id to replace.")
		@Config.LangKey("replaceblock")
		public static String[] replaceblock = {"minecraft:stone"};
		@Config.Comment(("The block meta to replace. Use -1 for the block's default state."))
		@Config.LangKey("replaceblockmeta")
		public static int[] replaceblockmeta = {-1};
		@Config.Comment("The block id to replace the block with.")
		@Config.LangKey("replacewith")
		public static String[] replacewith = {"minecraft:stone"};
		@Config.Comment("The block meta for the replacement block. Use -1 for the block's default state.")
		@Config.LangKey("replacewithmeta")
		public static int replacewithmeta[] = {-1};
		@Config.Comment("Enables using blocks that might crash/lag the game if used to replace other blocks. Enable at your own risk.")
		@Config.LangKey("riskyblocks")
		public static boolean riskyblocks = false;
		@Config.Comment("This is the Dimension Black/Whitelist. If it contains *, it is a blacklist. Otherwise, it is a whitelist.")
		@Config.LangKey("dimension_list")
		public static String[] dimension_list = {"*"};
		@Config.Comment("What percentage of the blocks get replaced. 0.0D = 0%, 1.0D = 100%")
		@Config.RangeDouble(min = 0.0D, max=1.0D)
		@Config.LangKey("replacepercent")
		public static double[] replacepercent = {1.0D};
		@Config.Comment("Multiplies the block removal chance by the block's y-value.")
		@Config.LangKey("multiplychance")
		public static boolean[] multiplychance = {false};
		@Config.Comment("The lowest Y value the block should be replaced at")
		@Config.RangeInt(min=-1,max=256)
		@Config.LangKey("miny")
		public static int[] miny = {-1};
		@Config.Comment("The highest Y value the block should be replaced at")
		@Config.RangeInt(min=-1,max=256)
		@Config.LangKey("maxy")
		public static int[] maxy = {256};
		@Config.Comment("This is the Biome Black/Whitelist. If it contains *, it is a blacklist. Otherwise, it is a whitelist.")
		@Config.LangKey("biomefilter")
		public static String[] biomefilter = {"*"};
		@Config.Comment("Increase the precision of the biome filter. This may reduce performance.")
		@Config.LangKey("biomeprecision")
		public static boolean biomeprecision = true;
	}
}
