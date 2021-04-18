package the_fireplace.wgblockreplacer.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import the_fireplace.wgblockreplacer.WGBlockReplacer;
import the_fireplace.wgblockreplacer.api.config.ConfigAccess;
import the_fireplace.wgblockreplacer.api.world.ChunkReplacementData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ChunkReplacedCapabilityHandler implements ChunkReplacementData {
    @Deprecated
    public static final ChunkReplacementData INSTANCE = new ChunkReplacedCapabilityHandler();
    private ChunkReplacedCapabilityHandler() {}

    private static final ResourceLocation BLOCKS_REPLACED_CAPABILITY_ID = new ResourceLocation(WGBlockReplacer.MODID, "blocks_replaced");

    @CapabilityInject(BlockReplacedCapability.class)
    public static Capability<BlockReplacedCapability> BLOCKS_REPLACED = null;

    @Override
    public boolean isReplaced(Chunk chunk) {
        //noinspection ConstantConditions
        BlockReplacedCapability cap = chunk instanceof ICapabilityProvider ? ((ICapabilityProvider) chunk).getCapability(BLOCKS_REPLACED, null) : null;
        return cap != null && cap.getReplacedMarker() != null && cap.getReplacedMarker().equals(ConfigAccess.getInstance().getReplacementChunkKey());
    }

    @Override
    public void markAsReplaced(Chunk chunk) {
        //noinspection ConstantConditions
        BlockReplacedCapability cap = chunk instanceof ICapabilityProvider ? ((ICapabilityProvider) chunk).getCapability(BLOCKS_REPLACED, null) : null;
        if (cap != null) {
            cap.setReplacedMarker(ConfigAccess.getInstance().getReplacementChunkKey());
        }
    }

    @SubscribeEvent
    public void attachChunkCapability(AttachCapabilitiesEvent<Chunk> e) {
        assert BLOCKS_REPLACED != null;
        e.addCapability(BLOCKS_REPLACED_CAPABILITY_ID, new ICapabilitySerializable<NBTBase>() {
            final BlockReplacedCapability inst = BLOCKS_REPLACED.getDefaultInstance();

            @Nullable
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

            @Nullable
            @Override
            public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
                //noinspection unchecked
                return capability == BLOCKS_REPLACED ? (T) inst : null;
            }
        });
    }
}
