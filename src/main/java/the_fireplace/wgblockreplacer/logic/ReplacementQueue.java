package the_fireplace.wgblockreplacer.logic;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.apache.commons.lang3.tuple.Pair;
import the_fireplace.wgblockreplacer.api.config.ConfigAccess;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ReplacementQueue {
    private static final Map<Pair<World, Chunk>, Integer> TIMERS = new ConcurrentHashMap<>();

    public static void tick() {
        for (Map.Entry<Pair<World, Chunk>, Integer> p : TIMERS.entrySet()) {
            if (p.getValue() <= 0) {
                TIMERS.remove(p.getKey());
                Replacer.doReplacement(p.getKey().getLeft(), p.getKey().getRight());
            } else {
                TIMERS.put(p.getKey(), p.getValue() - 1);
            }
        }
    }

    public static void add(World world, Chunk chunk) {
        Pair<World, Chunk> p = Pair.of(world, chunk);
        TIMERS.putIfAbsent(p, ConfigAccess.getInstance().getLateReplacementTicks());
    }
}
