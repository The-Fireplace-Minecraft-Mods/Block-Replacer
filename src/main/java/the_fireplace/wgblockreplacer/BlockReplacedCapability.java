package the_fireplace.wgblockreplacer;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Direction;
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
        public String getReplacedMarker(){
            return replacingMarker;
        }
    }

    class Storage implements Capability.IStorage<BlockReplacedCapability> {
        @Override
        public INBT writeNBT(Capability<BlockReplacedCapability> capability, BlockReplacedCapability instance, Direction side) {
            return new StringNBT(instance.getReplacedMarker() != null ? instance.getReplacedMarker() : "");
        }

        @Override
        public void readNBT(Capability<BlockReplacedCapability> capability, BlockReplacedCapability instance, Direction side, INBT nbt) {
            if(nbt instanceof StringNBT && !nbt.getString().isEmpty())
                instance.setReplacedMarker(nbt.getString());
        }
    }
}