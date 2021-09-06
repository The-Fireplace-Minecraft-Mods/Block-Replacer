package dev.the_fireplace.wgblockreplacer.translation;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class SimpleTranslationUtil {
    /**
     * Gets the translation for the given key and arguments and returns the formatted string.
     */
    public static String getStringTranslation(String translationKey, Object... args) {
        return getTranslation(translationKey, args).getUnformattedText();
    }

    public static String getRawTranslationString(String translationKey) {
        return I18n.translateToLocalFormatted(translationKey);
    }

    /**
     * Returns the TextComponentTranslation if the target is able to translate it, or the translated TextComponentString otherwise.
     */
    public static ITextComponent getTranslation(String translationKey, Object... args) {
        return new TextComponentString(I18n.translateToLocalFormatted(translationKey, args));
    }
}
