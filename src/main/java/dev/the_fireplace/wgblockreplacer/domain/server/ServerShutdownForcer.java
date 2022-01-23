package dev.the_fireplace.wgblockreplacer.domain.server;

import net.minecraft.server.MinecraftServer;

import java.util.Collection;

public interface ServerShutdownForcer
{
    void shutdown(MinecraftServer server, Collection<String> messages);
}
