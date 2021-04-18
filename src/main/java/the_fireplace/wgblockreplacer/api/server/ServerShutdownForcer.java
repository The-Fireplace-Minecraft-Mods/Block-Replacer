package the_fireplace.wgblockreplacer.api.server;

import the_fireplace.wgblockreplacer.server.ServerStopper;

import java.util.Collection;

public interface ServerShutdownForcer {
    static ServerShutdownForcer getInstance() {
        //noinspection deprecation
        return ServerStopper.INSTANCE;
    }
    void shutdown(Collection<String> messages);
}
