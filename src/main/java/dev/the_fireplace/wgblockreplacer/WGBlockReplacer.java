package dev.the_fireplace.wgblockreplacer;

import dev.the_fireplace.wgblockreplacer.api.config.ConfigAccess;
import dev.the_fireplace.wgblockreplacer.api.config.ValidationFailureHandler;
import dev.the_fireplace.wgblockreplacer.capability.BlockReplacedCapability;
import dev.the_fireplace.wgblockreplacer.capability.ChunkReplacedCapabilityHandler;
import dev.the_fireplace.wgblockreplacer.proxy.Common;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = WGBlockReplacer.MODID, name = WGBlockReplacer.MODNAME, guiFactory = "dev.the_fireplace.wgblockreplacer.config.WGBRGuiFactory", canBeDeactivated = true, acceptedMinecraftVersions = "[1.12,1.13)", acceptableRemoteVersions = "*")
public final class WGBlockReplacer {
	public static final String MODID = "wgblockreplacer";
	public static final String MODNAME = "WorldGen Block Replacer";

	@SidedProxy(clientSide = "dev.the_fireplace.wgblockreplacer.proxy.Client", serverSide = "dev.the_fireplace.wgblockreplacer.proxy.Common")
	public static Common proxy;

	private static Logger LOGGER = FMLLog.getLogger();

	public static Logger getLogger() {
		return LOGGER;
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		LOGGER = event.getModLog();
		CapabilityManager.INSTANCE.register(BlockReplacedCapability.class, new BlockReplacedCapability.Storage(), BlockReplacedCapability.Default::new);
		//noinspection deprecation
		MinecraftForge.EVENT_BUS.register(ChunkReplacedCapabilityHandler.INSTANCE);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		if (event.getSide().isClient()) {
			proxy.initBlockList();
		}
	}

	@EventHandler
	public void onServerStart(FMLServerStartedEvent event) {
		ValidationFailureHandler.getInstance().revalidate();
	}

	@SuppressWarnings("WeakerAccess")
	@Config(modid = MODID)
	public static final class ConfigValues implements ConfigAccess {
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
		public static int[] replacewithmeta = {-1};
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
		@Config.Comment("Prevent the world from loading if the mod is improperly configured. This is to prevent terrain from generating without the intended configuration.")
		public static boolean preventLoadOnFailure = true;
		@Config.Comment("Runs the replacement this many ticks after the chunk is generated. Use this if you're having issues with some blocks not being replaced. I strongly recommend that you do not set this above 10. 2-5 should be plenty.")
		public static int lateReplacement = 0;
		@Config.Comment("Changing this will allow Block Replacer to run again on existing chunks. Useful for doing retrogen on world you've already run the mod on. Back up your world before changing this.")
		public static String replacementChunkKey = "DEFAULT_REPLACE_KEY";
		@Config.Comment("The server's locale")
		public static String locale = "en_us";

		@Deprecated
		@Config.Ignore
		public static final ConfigAccess INSTANCE = new ConfigValues();
		@Deprecated
		public ConfigValues() {}

		@Override
		public int[] getReplaceBlockMetas() {
			return replaceblockmeta;
		}

		@Override
		public String[] getReplaceWithBlockIds() {
			return replacewith;
		}

		@Override
		public int[] getReplaceWithMetas() {
			return replacewithmeta;
		}

		@Override
		public boolean allowRiskyBlocks() {
			return riskyblocks;
		}

		@Override
		public String[] getDimensionLists() {
			return dimension_list;
		}

		@Override
		public double[] getReplaceChances() {
			return replacepercent;
		}

		@Override
		public boolean[] getMultiplyChances() {
			return multiplychance;
		}

		@Override
		public int[] getMinYs() {
			return miny;
		}

		@Override
		public int[] getMaxYs() {
			return maxy;
		}

		@Override
		public String[] getBiomeFilterLists() {
			return biomefilter;
		}

		@Override
		public boolean useBiomePrecision() {
			return biomeprecision;
		}

		@Override
		public boolean preventLoadOnFailure() {
			return preventLoadOnFailure;
		}

		@Override
		public int getLateReplacementTicks() {
			return lateReplacement;
		}

		@Override
		public String getReplacementChunkKey() {
			return replacementChunkKey;
		}

		@Override
		public String[] getReplaceBlockIds() {
			return replaceblock;
		}
	}
}
