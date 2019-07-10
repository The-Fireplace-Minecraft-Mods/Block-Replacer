package the_fireplace.wgblockreplacer.events;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import the_fireplace.wgblockreplacer.WGBlockReplacer;

import java.util.Objects;

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

	@SubscribeEvent
	public static void onKeyPress(ClientChatEvent event) {
		if(event.getOriginalMessage().equals("$meta")) {
			event.setCanceled(true);
			RayTraceResult look = Minecraft.getMinecraft().objectMouseOver;
			if(look.typeOfHit == RayTraceResult.Type.BLOCK) {
				IBlockState state = Minecraft.getMinecraft().world.getBlockState(look.getBlockPos());
				Minecraft.getMinecraft().player.sendMessage(new TextComponentString(Objects.requireNonNull(state.getBlock().getRegistryName()).toString() + " " + state.getBlock().getMetaFromState(state)));
			}
		}
	}
}
