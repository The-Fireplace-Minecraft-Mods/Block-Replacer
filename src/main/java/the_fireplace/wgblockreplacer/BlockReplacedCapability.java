package the_fireplace.wgblockreplacer;

import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public interface BlockReplacedCapability {

    String getReplacedMarker();
    void setReplacedMarker(String faction);

    class Default implements BlockReplacedCapability {
        private String replacingMarker;

        public Default(){
            replacingMarker = null;
        }

        @Override
        public void setReplacedMarker(String faction){
            replacingMarker = faction;
        }

        @Override
        public String getReplacedMarker(){
            return replacingMarker;
        }
    }

    class Storage implements Capability.IStorage<BlockReplacedCapability> {
        @Override
        public INBTBase writeNBT(Capability<BlockReplacedCapability> capability, BlockReplacedCapability instance, EnumFacing side) {
            return new NBTTagString(instance.getReplacedMarker() != null ? instance.getReplacedMarker() : "");
        }

        @Override
        public void readNBT(Capability<BlockReplacedCapability> capability, BlockReplacedCapability instance, EnumFacing side, INBTBase nbt) {
            if(nbt instanceof NBTTagString && !nbt.getString().isEmpty())
                instance.setReplacedMarker(nbt.getString());
        }
    }
}