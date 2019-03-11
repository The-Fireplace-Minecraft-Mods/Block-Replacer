package the_fireplace.wgblockreplacer;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.nbt.INBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Mod(WGBlockReplacer.MODID)
public class WGBlockReplacer {
	public static final String MODID = "wgblockreplacer";

	@CapabilityInject(BlockReplacedCapability.class)
	public static final Capability<BlockReplacedCapability> BLOCKS_REPLACED = null;
	private static final ResourceLocation blocks_replaced_res = new ResourceLocation(MODID, "blocks_replaced");

	public static Logger LOGGER = LogManager.getLogger(MODID);

	public WGBlockReplacer() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, cfg.SERVER_SPEC);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInit);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverConfig);
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void preInit(FMLCommonSetupEvent event){
		CapabilityManager.INSTANCE.register(BlockReplacedCapability.class, new BlockReplacedCapability.Storage(), BlockReplacedCapability.Default::new);
	}

	public void serverConfig(ModConfig.ModConfigEvent event) {
		if (event.getConfig().getType() == ModConfig.Type.SERVER)
			cfg.load();
	}

	public static boolean hasBeenReplaced(IChunk chunk) {
		//noinspection ConstantConditions
		BlockReplacedCapability cap = chunk instanceof ICapabilityProvider ? ((ICapabilityProvider) chunk).getCapability(BLOCKS_REPLACED).orElseThrow(() -> new IllegalStateException("Blocks Replaced Capability is not present for a chunk!")) : null;
		return cap != null && cap.getReplacedMarker() != null && cap.getReplacedMarker().equals(cfg.replacementChunkKey);
	}

	public static void setReplaced(IChunk chunk) {
		//noinspection ConstantConditions
		BlockReplacedCapability cap = chunk instanceof ICapabilityProvider ? ((ICapabilityProvider) chunk).getCapability(BLOCKS_REPLACED).orElseThrow(() -> new IllegalStateException("Blocks Replaced Capability is not present for a chunk!")) : null;
		if(cap != null)
			cap.setReplacedMarker(cfg.replacementChunkKey);
	}

	@SubscribeEvent
	public static void attachChunkCaps(AttachCapabilitiesEvent<Chunk> e){
		//noinspection ConstantConditions
		assert BLOCKS_REPLACED != null;
		e.addCapability(blocks_replaced_res, new ICapabilitySerializable() {
			BlockReplacedCapability inst = BLOCKS_REPLACED.getDefaultInstance();

			@Override
			public INBTBase serializeNBT() {
				return BLOCKS_REPLACED.getStorage().writeNBT(BLOCKS_REPLACED, inst, null);
			}

			@Override
			public void deserializeNBT(INBTBase nbt) {
				BLOCKS_REPLACED.getStorage().readNBT(BLOCKS_REPLACED, inst, null, nbt);
			}

			@Nonnull
			@Override
			public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
				//noinspection unchecked
				return capability == BLOCKS_REPLACED ? LazyOptional.of(() -> (T) inst) : LazyOptional.empty();
			}
		});
	}

	public static boolean isBlockRisky(Block block) {
		return block.getDefaultState().needsRandomTick() || !block.getDefaultState().isFullCube() || !block.isCollidable() || block.hasTileEntity(block.getDefaultState());
	}

	public static class cfg {
		public static final ServerConfig SERVER;
		public static final ForgeConfigSpec SERVER_SPEC;

		static {
			final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
			SERVER_SPEC = specPair.getRight();
			SERVER = specPair.getLeft();
		}

		public static List<String> replaceBlocks;
		public static List<String> replacements;
		public static boolean allowRiskyReplacements;
		public static List<String> dimensionFilters;
		public static List<Double> replacePercents;
		public static List<Boolean> multiplyChances;
		public static List<Integer> minYs;
		public static List<Integer> maxYs;
		public static List<String> biomeFilters;
		public static boolean biomeFilterPrecision;
		public static boolean preventLoadOnFailure;
		public static String replacementChunkKey;

		public static void load() {
			replaceBlocks = SERVER.replaceBlocks.get();
			replacements = SERVER.replacements.get();
			allowRiskyReplacements = SERVER.allowRiskyReplacements.get();
			dimensionFilters = SERVER.dimensionFilters.get();
			replacePercents = SERVER.replacePercents.get();
			multiplyChances = SERVER.multiplyChances.get();
			minYs = SERVER.minYs.get();
			maxYs = SERVER.maxYs.get();
			biomeFilters = SERVER.biomeFilter.get();
			biomeFilterPrecision = SERVER.biomeFilterPrecision.get();
			preventLoadOnFailure = SERVER.preventLoadOnFailure.get();
			replacementChunkKey = SERVER.replacementChunkKey.get();
		}

		public static class ServerConfig {
			public ForgeConfigSpec.ConfigValue<List<String>> replaceBlocks;
			public ForgeConfigSpec.ConfigValue<List<String>> replacements;
			public ForgeConfigSpec.BooleanValue allowRiskyReplacements;
			public ForgeConfigSpec.ConfigValue<List<String>> dimensionFilters;
			public ForgeConfigSpec.ConfigValue<List<Double>> replacePercents;
			public ForgeConfigSpec.ConfigValue<List<Boolean>> multiplyChances;
			public ForgeConfigSpec.ConfigValue<List<Integer>> minYs;
			public ForgeConfigSpec.ConfigValue<List<Integer>> maxYs;
			public ForgeConfigSpec.ConfigValue<List<String>> biomeFilter;
			public ForgeConfigSpec.BooleanValue biomeFilterPrecision;
			public ForgeConfigSpec.BooleanValue preventLoadOnFailure;
			public ForgeConfigSpec.ConfigValue<String> replacementChunkKey;

			ServerConfig(ForgeConfigSpec.Builder builder) {
				builder.push("general");
				replaceBlocks = builder
						.comment("The block ids to replace.")
						.translation("Replace Blocks")
						.define("replaceBlocks", Lists.newArrayList("minecraft:stone"));
				replacements = builder
						.comment("The block ids to replace the blocks with.")
						.translation("Replacements")
						.define("replacements", Lists.newArrayList("minecraft:stone"));
				allowRiskyReplacements = builder
						.comment("Enables using blocks that might crash/lag the game if used to replace other blocks. Enable at your own risk.")
						.translation("Allow Risky Replacements")
						.define("allowRiskyReplacements", false);
				dimensionFilters = builder
						.comment("This is the Dimension Filter list. If it contains *, it is a blacklist. Otherwise, it is a whitelist. Each item in the list is a list, separate the items in each sub-list with commas.")
						.translation("Dimension Filter")
						.define("dimensionFilters", Lists.newArrayList("*"));
				replacePercents = builder
						.comment("This defines what percentage of blocks get replaced. 0.0 = 0%. 1.0 = 100%.")
						.translation("Replace Percentages")
						.define("replacePercents", Lists.newArrayList(1.0D));
				multiplyChances = builder
						.comment("Multiplies the block replacement chance by the block's y-value.")
						.translation("Multiply Chances")
						.define("multiplyChances", Lists.newArrayList(false));
				minYs = builder
						.comment("The minimum Y values to replace the blocks at.")
						.translation("Minimum Replacement Y Values")
						.define("minYs", Lists.newArrayList(-1));
				maxYs = builder
						.comment("The maximum Y values to replace the blocks at.")
						.translation("Maximum Replacement Y Values")
						.define("maxYs", Lists.newArrayList(256));
				biomeFilter = builder
						.comment("This is the Biome Filter. If it contains *, it is a blacklist. Otherwise, it is a whitelist. Each item in the list is a list, separate the items in each sub-list with commas.")
						.translation("Biome Filter")
						.define("biomeFilters", Lists.newArrayList("*"));
				biomeFilterPrecision = builder
						.comment("Increase the precision of the biome filter. This may reduce performance.")
						.translation("Biome Filter Precision")
						.define("biomeFilterPrecision", true);
				preventLoadOnFailure = builder
						.comment("Prevent the world from loading (and by extension, generating) if the mod is improperly configured.")
						.translation("Prevent Loading On Failure")
						.define("preventLoadOnFailure", true);
				replacementChunkKey = builder
						.comment("Changing this will allow Block Replacer to run again on existing chunks. Useful for doing retrogen on world you've already run the mod on. Back up your world before changing this.")
						.translation("Replacement Chunk Key")
						.define("replacementChunkKey", "DEFAULT_REPLACE_KEY");
				builder.pop();
			}
		}
	}
}
