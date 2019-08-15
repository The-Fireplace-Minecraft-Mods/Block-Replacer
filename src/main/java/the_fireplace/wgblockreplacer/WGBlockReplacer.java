package the_fireplace.wgblockreplacer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;
import the_fireplace.wgblockreplacer.proxy.Common;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod(modid = WGBlockReplacer.MODID, name = WGBlockReplacer.MODNAME, guiFactory = "the_fireplace.wgblockreplacer.config.WGBRGuiFactory", canBeDeactivated = true, acceptedMinecraftVersions = "[1.12,1.13)", acceptableRemoteVersions = "*")
public class WGBlockReplacer {
	public static final String MODID = "wgblockreplacer";
	public static final String MODNAME = "WorldGen Block Replacer";

	@CapabilityInject(BlockReplacedCapability.class)
	public static final Capability<BlockReplacedCapability> BLOCKS_REPLACED = null;
	private static final ResourceLocation blocks_replaced_res = new ResourceLocation(MODID, "blocks_replaced");

	@SidedProxy(clientSide = "the_fireplace.wgblockreplacer.proxy.Client", serverSide = "the_fireplace.wgblockreplacer.proxy.Common")
	public static Common proxy;

	public static Logger LOGGER;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		LOGGER = event.getModLog();
		CapabilityManager.INSTANCE.register(BlockReplacedCapability.class, new BlockReplacedCapability.Storage(), BlockReplacedCapability.Default::new);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		if(event.getSide().isClient())
			proxy.initBlockList();
	}

	public static boolean hasBeenReplaced(Chunk chunk) {
		//noinspection ConstantConditions
		BlockReplacedCapability cap = chunk instanceof ICapabilityProvider ? ((ICapabilityProvider) chunk).getCapability(BLOCKS_REPLACED, null) : null;
		return cap != null && cap.getReplacedMarker() != null && cap.getReplacedMarker().equals(ConfigValues.replacementChunkKey);
	}

	public static void setReplaced(Chunk chunk) {
		//noinspection ConstantConditions
		BlockReplacedCapability cap = chunk instanceof ICapabilityProvider ? ((ICapabilityProvider) chunk).getCapability(BLOCKS_REPLACED, null) : null;
		if(cap != null)
			cap.setReplacedMarker(ConfigValues.replacementChunkKey);
	}

	@SubscribeEvent
	public void attachChunkCaps(AttachCapabilitiesEvent<Chunk> e){
		//noinspection ConstantConditions
		assert BLOCKS_REPLACED != null;
		e.addCapability(blocks_replaced_res, new ICapabilitySerializable() {
			BlockReplacedCapability inst = BLOCKS_REPLACED.getDefaultInstance();

			@Override
			public NBTBase serializeNBT() {
				return BLOCKS_REPLACED.getStorage().writeNBT(BLOCKS_REPLACED, inst, null);
			}

			@Override
			public void deserializeNBT(NBTBase nbt) {
				BLOCKS_REPLACED.getStorage().readNBT(BLOCKS_REPLACED, inst, null, nbt);
			}

			@Override
			public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
				return capability == BLOCKS_REPLACED;
			}

			@Nonnull
			@Override
			public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
				//noinspection unchecked
				return capability == BLOCKS_REPLACED ? (T) inst : null;
			}
		});
	}

	public static boolean isBlockRisky(Block block) {
		return !(block instanceof BlockAir) && (!block.getDefaultState().isOpaqueCube() || !block.getDefaultState().isFullCube() || !block.isCollidable() || block.hasTileEntity(block.getDefaultState()));
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
		@Config.Comment("Changing this will allow Block Replacer to run again on existing chunks. Useful for doing retrogen on world you've already run the mod on. Back up your world before changing this.")
		public static String replacementChunkKey = "DEFAULT_REPLACE_KEY";
		@Config.Comment("The server's locale")
		public static String locale = "en_us";
	}
}
