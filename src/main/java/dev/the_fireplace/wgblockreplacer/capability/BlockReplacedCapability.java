package dev.the_fireplace.wgblockreplacer.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public interface BlockReplacedCapability {

    String getReplacedMarker();
    void setReplacedMarker(String faction);

    class Default implements BlockReplacedCapability {
        @Nullable
        private String replacingMarker;

        public Default(){
            replacingMarker = null;
        }

        @Override
        public void setReplacedMarker(String faction){
            replacingMarker = faction;
        }

        @Override
        @Nullable
        public String getReplacedMarker(){
            return replacingMarker;
        }
    }

    class Storage implements Capability.IStorage<BlockReplacedCapability> {
        @Override
        public NBTBase writeNBT(Capability<BlockReplacedCapability> capability, BlockReplacedCapability instance, EnumFacing side) {
            return new NBTTagString(instance.getReplacedMarker() != null ? instance.getReplacedMarker() : "");
        }

        @Override
        public void readNBT(Capability<BlockReplacedCapability> capability, BlockReplacedCapability instance, EnumFacing side, NBTBase nbt) {
            if (nbt instanceof NBTTagString && !((NBTTagString) nbt).getString().isEmpty()) {
                instance.setReplacedMarker(((NBTTagString) nbt).getString());
            }
        }
    }
}
