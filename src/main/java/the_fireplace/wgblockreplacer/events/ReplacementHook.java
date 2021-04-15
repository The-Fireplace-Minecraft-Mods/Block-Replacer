package the_fireplace.wgblockreplacer.events;

import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class ReplacementHook {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEvent(ChunkEvent.Load event) {

    }
}
