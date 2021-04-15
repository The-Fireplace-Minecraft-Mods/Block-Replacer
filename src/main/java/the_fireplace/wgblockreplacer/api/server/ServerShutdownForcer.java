package the_fireplace.wgblockreplacer.api.server;

import java.util.Collection;

public interface ServerShutdownForcer {
    static ServerShutdownForcer getInstance() {
        return null;
    }
    void shutdown(Collection<String> messages);
}
