package the_fireplace.wgblockreplacer.events;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import the_fireplace.wgblockreplacer.WGBlockReplacer;

/**
 * @author The_Fireplace
 */
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = WGBlockReplacer.MODID)
public class ClientEvents {
	@SubscribeEvent
	public static void configChanged(ConfigChangedEvent event) {
		if (event.getModID().equals(WGBlockReplacer.MODID)) {
			ConfigManager.sync(WGBlockReplacer.MODID, Config.Type.INSTANCE);
			WGBlockReplacer.proxy.initBlockList();
		}
	}
}
