package the_fireplace.wgblockreplacer;

import net.minecraft.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(WGBlockReplacer.MODID)
public class WGBlockReplacer {
	public static final String MODID = "wgblockreplacer";

	public static Logger LOGGER = LogManager.getLogger(MODID);

	public WGBlockReplacer() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, cfg.SERVER_SPEC);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverConfig);
	}

	public void serverConfig(ModConfig.ModConfigEvent event) {
		if (event.getConfig().getType() == ModConfig.Type.SERVER)
			cfg.load();
	}

	public static boolean isBlockRisky(Block block) {
		return !block.getDefaultState().needsRandomTick() || !block.getDefaultState().isFullCube() || !block.isCollidable() || block.hasTileEntity(block.getDefaultState());
	}

	public static class cfg {
		public static final ServerConfig SERVER;
		public static final ForgeConfigSpec SERVER_SPEC;

		static {
			final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
			SERVER_SPEC = specPair.getRight();
			SERVER = specPair.getLeft();
		}

		public static String[] replaceBlocks = {"minecraft:stone"};
		public static String[] replacements = {"minecraft:stone"};
		public static boolean allowRiskyReplacements = false;
		public static String[] dimensionFilter = {"*"};
		public static double[] replacePercents = {1.0D};
		public static boolean[] multiplyChances = {false};
		public static int[] minYs = {-1};
		public static int[] maxYs = {256};
		public static String[] biomeFilter = {"*"};
		public static boolean biomeFilterPrecision = true;
		public static boolean preventLoadOnFailure = true;

		public static void load() {
			replaceBlocks = SERVER.replaceBlocks.get();
			replacements = SERVER.replacements.get();
			allowRiskyReplacements = SERVER.allowRiskyReplacements.get();
			dimensionFilter = SERVER.dimensionFilter.get();
			replacePercents = SERVER.replacePercents.get();
			multiplyChances = SERVER.multiplyChances.get();
			minYs = SERVER.minYs.get();
			maxYs = SERVER.maxYs.get();
			biomeFilter = SERVER.biomeFilter.get();
			biomeFilterPrecision = SERVER.biomeFilterPrecision.get();
			preventLoadOnFailure = SERVER.preventLoadOnFailure.get();
		}

		public static class ServerConfig {
			public ForgeConfigSpec.ConfigValue<String[]> replaceBlocks;
			public ForgeConfigSpec.ConfigValue<String[]> replacements;
			public ForgeConfigSpec.BooleanValue allowRiskyReplacements;
			public ForgeConfigSpec.ConfigValue<String[]> dimensionFilter;
			public ForgeConfigSpec.ConfigValue<double[]> replacePercents;
			public ForgeConfigSpec.ConfigValue<boolean[]> multiplyChances;
			public ForgeConfigSpec.ConfigValue<int[]> minYs;
			public ForgeConfigSpec.ConfigValue<int[]> maxYs;
			public ForgeConfigSpec.ConfigValue<String[]> biomeFilter;
			public ForgeConfigSpec.BooleanValue biomeFilterPrecision;
			public ForgeConfigSpec.BooleanValue preventLoadOnFailure;

			ServerConfig(ForgeConfigSpec.Builder builder) {
				builder.push("general");
				replaceBlocks = builder
						.comment("The block ids to replace.")
						.translation("Replace Blocks")
						.define("replaceBlocks", new String[]{"minecraft:stone"});
				replacements = builder
						.comment("The block ids to replace the blocks with.")
						.translation("Replacements")
						.define("replacements", new String[]{"minecraft:stone"});
				allowRiskyReplacements = builder
						.comment("Enables using blocks that might crash/lag the game if used to replace other blocks. Enable at your own risk.")
						.translation("Allow Risky Replacements")
						.define("allowRiskyReplacements", false);
				dimensionFilter = builder
						.comment("This is the Dimension Filter. If it contains *, it is a blacklist. Otherwise, it is a whitelist.")
						.translation("Dimension Filter")
						.define("dimensionFilter", new String[]{"*"});
				replacePercents = builder
						.comment("This defines what percentage of blocks get replaced. 0.0 = 0%. 1.0 = 100%.")
						.translation("Replace Percentages")
						.define("replacePercents", new double[]{1.0D});
				multiplyChances = builder
						.comment("Multiplies the block removal chance by the block's y-value.")
						.translation("Multiply Chances")
						.define("multiplyChances", new boolean[]{false});
				minYs = builder
						.comment("The minimum Y values to replace the blocks at.")
						.translation("Minimum Replacement Y Values")
						.define("minYs", new int[]{-1});
				maxYs = builder
						.comment("The maximum Y values to replace the blocks at.")
						.translation("Maximum Replacement Y Values")
						.define("maxYs", new int[]{256});
				biomeFilter = builder
						.comment("This is the Biome Filter. If it contains *, it is a blacklist. Otherwise, it is a whitelist.")
						.translation("Biome Filter")
						.define("biomeFilter", new String[]{"*"});
				biomeFilterPrecision = builder
						.comment("Increase the precision of the biome filter. This may reduce performance.")
						.translation("Biome Filter Precision")
						.define("showBalanceOnJoin", true);
				preventLoadOnFailure = builder
						.comment("Prevent the world from loading (and by extension, generating) if the mod is improperly configured.")
						.translation("Prevent Loading On Failure")
						.define("preventLoadOnFailure", true);
				builder.pop();
			}
		}
	}
}
