package the_fireplace.wgblockreplacer.config;

import net.minecraftforge.common.MinecraftForge;
import the_fireplace.wgblockreplacer.WGBlockReplacer;
import the_fireplace.wgblockreplacer.api.config.ConfigAccess;
import the_fireplace.wgblockreplacer.api.config.ConfigValidator;
import the_fireplace.wgblockreplacer.api.config.ValidationFailureHandler;
import the_fireplace.wgblockreplacer.api.server.ServerShutdownForcer;
import the_fireplace.wgblockreplacer.events.ReplacementHook;
import the_fireplace.wgblockreplacer.translation.SimpleTranslationUtil;

import java.util.Collection;

public final class ValidationFailureHandlerImpl implements ValidationFailureHandler {
    @Deprecated
    public static final ValidationFailureHandler INSTANCE = new ValidationFailureHandlerImpl();

    private final ReplacementHook replacementHook = new ReplacementHook();

    private ValidationFailureHandlerImpl() {}

    @Override
    public void revalidate() {
        ConfigValidator validator = ConfigValidator.getInstance();
        Collection<String> validationErrors = validator.getValidationErrors();

        if (!validator.calculateValidity() && ConfigAccess.getInstance().preventLoadOnFailure()) {
            WGBlockReplacer.getLogger().error(SimpleTranslationUtil.getStringTranslation("wgbr.improperly_configured"));
            ServerShutdownForcer.getInstance().shutdown(validationErrors);
            return;
        }

        if (!validationErrors.isEmpty()) {
            for (String errorMessage: validationErrors) {
                if (ConfigAccess.getInstance().preventLoadOnFailure()) {
                    WGBlockReplacer.getLogger().warn(errorMessage);
                } else {
                    WGBlockReplacer.getLogger().error(errorMessage);
                }
            }

            if (!ConfigAccess.getInstance().preventLoadOnFailure()) {
                MinecraftForge.EVENT_BUS.unregister(replacementHook);
                return;
            }
        }

        MinecraftForge.EVENT_BUS.register(replacementHook);
    }
}
