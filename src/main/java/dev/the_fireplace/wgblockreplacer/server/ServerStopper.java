package dev.the_fireplace.wgblockreplacer.server;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.lib.api.chat.injectables.TranslatorFactory;
import dev.the_fireplace.lib.api.chat.interfaces.Translator;
import dev.the_fireplace.wgblockreplacer.WGBRConstants;
import dev.the_fireplace.wgblockreplacer.domain.server.ServerShutdownForcer;
import net.minecraft.server.MinecraftServer;

import javax.inject.Inject;
import java.util.Collection;

@Implementation
public final class ServerStopper implements ServerShutdownForcer
{
    private final Translator translator;

    @Inject
    public ServerStopper(TranslatorFactory translatorFactory) {
        this.translator = translatorFactory.getTranslator(WGBRConstants.MODID);
    }

    @Override
    public void shutdown(MinecraftServer server, Collection<String> messages) {
        server.shutdown();
        throw new RuntimeException(translator.getTranslatedString("wgbr.shutdown", String.join(", ", messages)));
    }
}
