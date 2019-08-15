package the_fireplace.wgblockreplacer.translation;

class I18n {
    private static final BRLanguageMap localizedName = BRLanguageMap.getInstance();
    private static final BRLanguageMap fallbackTranslator = new BRLanguageMap("en_us");

    static String translateToLocalFormatted(String key, Object... format) {
        return canTranslate(key) ? localizedName.translateKeyFormat(key, format) : translateToFallback(key, format);
    }

    private static String translateToFallback(String key, Object... format) {
        return fallbackTranslator.translateKeyFormat(key, format);
    }

    private static boolean canTranslate(String key) {
        return localizedName.isKeyTranslated(key);
    }
}
