package dev.the_fireplace.wgblockreplacer.server;

import dev.the_fireplace.wgblockreplacer.api.server.ServerShutdownForcer;
import dev.the_fireplace.wgblockreplacer.translation.SimpleTranslationUtil;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Collection;

public final class ServerStopper implements ServerShutdownForcer {
    @Deprecated
    public static final ServerShutdownForcer INSTANCE = new ServerStopper();
    private ServerStopper() {}

    @Override
    public void shutdown(Collection<String> messages) {
        FMLCommonHandler.instance().getMinecraftServerInstance().stopServer();
        throw new RuntimeException(SimpleTranslationUtil.getStringTranslation("wgbr.shutdown", String.join(", ", messages)));
    }
}
