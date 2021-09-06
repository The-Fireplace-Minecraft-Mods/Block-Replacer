package dev.the_fireplace.wgblockreplacer.events;

import dev.the_fireplace.wgblockreplacer.WGBlockReplacer;
import dev.the_fireplace.wgblockreplacer.api.config.ConfigAccess;
import dev.the_fireplace.wgblockreplacer.api.config.ConfigValidator;
import dev.the_fireplace.wgblockreplacer.api.world.ChunkReplacementData;
import dev.the_fireplace.wgblockreplacer.logic.ReplacementQueue;
import dev.the_fireplace.wgblockreplacer.logic.Replacer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = WGBlockReplacer.MODID)
public final class CommonEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void retrogenChunk(ChunkEvent.Load event) {
        if (event.getWorld().isRemote
            || !ConfigValidator.getInstance().isValid()
        ) {
            return;
        }
        Chunk chunk = event.getChunk();
        if (ChunkReplacementData.getInstance().needsRetrogen(chunk)) {
            replaceChunk(chunk, event.getWorld());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void genChunk(PopulateChunkEvent.Post event) {
        if (event.getWorld().isRemote || !ConfigValidator.getInstance().isValid()) {
            return;
        }
        Chunk chunk = event.getWorld().getChunk(event.getChunkX(), event.getChunkZ());
        if (ChunkReplacementData.getInstance().needsReplacement(chunk)) {
            replaceChunk(chunk, event.getWorld());
        }
    }

    private static void replaceChunk(Chunk chunk, World world) {
        if (ConfigAccess.getInstance().getLateReplacementTicks() > 0) {
            ReplacementQueue.add(world, chunk);
        } else {
            Replacer.doReplacement(world, chunk);
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.ServerTickEvent event) {
        ReplacementQueue.tick();
    }
}
