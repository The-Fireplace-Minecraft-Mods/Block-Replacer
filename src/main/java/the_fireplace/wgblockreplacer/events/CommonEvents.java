package the_fireplace.wgblockreplacer.events;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import the_fireplace.wgblockreplacer.WGBlockReplacer;
import the_fireplace.wgblockreplacer.api.config.ConfigAccess;
import the_fireplace.wgblockreplacer.api.config.ConfigValidator;
import the_fireplace.wgblockreplacer.api.world.ChunkReplacementData;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = WGBlockReplacer.MODID)
public final class CommonEvents {
	private static final Random rand = new Random();

	@SuppressWarnings("Duplicates")
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onEvent(ChunkEvent.Load event) {
	    if (event.getWorld().isRemote
            || !ConfigValidator.getInstance().isValid()
            || ChunkReplacementData.getInstance().isReplaced(event.getChunk())
        ) {
            return;
        }
        if (ConfigAccess.getInstance().getLateReplacementTicks() > 0) {
            queueReplacement(event.getWorld(), event.getChunk());
        } else {
            doReplacement(event.getWorld(), event.getChunk());
        }
	}

    @SubscribeEvent
    public static void onWorldTick(TickEvent.ServerTickEvent event) {
	    for (Map.Entry<Pair<World, Chunk>, Integer> p: TIMERS.entrySet()) {
	        if (p.getValue() <= 0) {
                TIMERS.remove(p.getKey());
	            doReplacement(p.getKey().getLeft(), p.getKey().getRight());
            } else {
                TIMERS.put(p.getKey(), p.getValue() - 1);
            }
        }
    }

	private static final Map<Pair<World, Chunk>, Integer> TIMERS = new ConcurrentHashMap<>();

    private static void queueReplacement(World world, Chunk chunk) {
	    Pair<World, Chunk> p = Pair.of(world, chunk);
	    TIMERS.putIfAbsent(p, ConfigAccess.getInstance().getLateReplacementTicks());
    }

    private static void doReplacement(World world, Chunk chunk) {
        for (int i = 0; i < ConfigAccess.getInstance().getReplaceBlockIds().length; i++) {
            if (!canReplaceInDimension(world, ConfigAccess.getInstance().getDimensionLists()[i])) {
                continue;
            }

            if (!ConfigAccess.getInstance().useBiomePrecision()) {
                Biome approximateBiome = chunk.getBiome(new BlockPos(chunk.getPos().getXStart(), 64, chunk.getPos().getZStart()), world.getBiomeProvider());
                if (!canReplaceInBiome(approximateBiome, ConfigAccess.getInstance().getBiomeFilterLists()[i]))
                    continue;
            }

            Block toBlock = Block.getBlockFromName(ConfigAccess.getInstance().getReplaceWithBlockIds()[i]);
            Block fromBlock = Block.getBlockFromName(ConfigAccess.getInstance().getReplaceBlockIds()[i]);
            int fromMeta = ConfigAccess.getInstance().getReplaceBlockMetas()[i];
            int toMeta = ConfigAccess.getInstance().getReplaceWithMetas()[i];
            if (toBlock == fromBlock && fromMeta == toMeta) {
                continue;
            }

            assert fromBlock != null;
            assert toBlock != null;

            IBlockState fromState;
            if (fromMeta == -1)
                fromState = fromBlock.getDefaultState();
            else
                fromState = fromBlock.getStateFromMeta(fromMeta);

            IBlockState toState;
            if (toMeta == -1)
                toState = toBlock.getDefaultState();
            else
                toState = toBlock.getStateFromMeta(toMeta);

            int storageYIndex = 0;
            for (ExtendedBlockStorage storage : chunk.getBlockStorageArray()) {
                if (storage != null) {
                    for (int storageX = 0; storageX < 16; storageX++) {
                        for (int storageY = 0; storageY < 16; storageY++) {
                            for (int storageZ = 0; storageZ < 16; storageZ++) {
                                if (storage.get(storageX, storageY, storageZ).equals(fromState)) {
                                    int worldY = storageYIndex * 16 + storageY;
                                    if (ConfigAccess.getInstance().getMinYs()[i] <= worldY
                                        && ConfigAccess.getInstance().getMaxYs()[i] >= worldY
                                        && rand.nextDouble() * (ConfigAccess.getInstance().getMultiplyChances()[i] ? worldY : 1) <= ConfigAccess.getInstance().getReplaceChances()[i]
                                    ) {
                                        if (ConfigAccess.getInstance().useBiomePrecision()) {
                                            Biome biome = chunk.getBiome(new BlockPos(storageX, storageY, storageZ), world.getBiomeProvider());
                                            if (!canReplaceInBiome(biome, ConfigAccess.getInstance().getBiomeFilterLists()[i])) {
                                                continue;
                                            }
                                        }
                                        storage.set(storageX, storageY, storageZ, toState);
                                    }
                                }
                            }
                        }
                    }
                }
                storageYIndex++;
            }
            chunk.markDirty();
        }
        ChunkReplacementData.getInstance().markAsReplaced(chunk);
    }

    private static boolean canReplaceInBiome(Biome biome1, String biomeList) {
        boolean doEvent = ArrayUtils.contains(biomeList.split(","), "*");
        for (String biome : biomeList.split(",")) {
            if ("*".equals(biome)) {
                continue;
            }
            if (biome1 == Biome.REGISTRY.getObject(new ResourceLocation(biome))) {
                doEvent = !doEvent;
                break;
            }
        }

        return doEvent;
    }

    private static boolean canReplaceInDimension(World world, String s) {
        boolean doEvent = ArrayUtils.contains(s.split(","), "*");
        for (String dim : s.split(",")) {
            try {
                if (world.provider.getDimension() == Integer.parseInt(dim)) {
                    doEvent = !doEvent;
                    break;
                }
            } catch (NumberFormatException e) {
                if (!dim.equals("*") && world.provider.getDimensionType().getName().equalsIgnoreCase(dim)) {
                    doEvent = !doEvent;
                    break;
                }
            }
        }
        return doEvent;
    }
}
