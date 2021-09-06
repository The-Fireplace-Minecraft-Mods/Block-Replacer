package dev.the_fireplace.wgblockreplacer.proxy;

import net.minecraft.util.text.translation.I18n;

/**
 * @author The_Fireplace
 */
public class Common {
	public String translateToLocal(String key, Object... args) {
		return I18n.translateToLocalFormatted(key, args);
	}

	public void initBlockList(){}
}
