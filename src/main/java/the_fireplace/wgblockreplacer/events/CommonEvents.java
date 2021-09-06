package the_fireplace.wgblockreplacer.events;

import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import the_fireplace.wgblockreplacer.WGBlockReplacer;
import the_fireplace.wgblockreplacer.api.config.ConfigAccess;
import the_fireplace.wgblockreplacer.api.config.ConfigValidator;
import the_fireplace.wgblockreplacer.api.world.ChunkReplacementData;
import the_fireplace.wgblockreplacer.logic.ReplacementQueue;
import the_fireplace.wgblockreplacer.logic.Replacer;

@Mod.EventBusSubscriber(modid = WGBlockReplacer.MODID)
public final class CommonEvents {

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onEvent(ChunkEvent.Load event) {
	    if (event.getWorld().isRemote
            || !ConfigValidator.getInstance().isValid()
            || ChunkReplacementData.getInstance().isReplaced(event.getChunk())
        ) {
            return;
        }
        if (ConfigAccess.getInstance().getLateReplacementTicks() > 0) {
            ReplacementQueue.add(event.getWorld(), event.getChunk());
        } else {
            Replacer.doReplacement(event.getWorld(), event.getChunk());
        }
	}

    @SubscribeEvent
    public static void onWorldTick(TickEvent.ServerTickEvent event) {
        ReplacementQueue.tick();
    }
}
