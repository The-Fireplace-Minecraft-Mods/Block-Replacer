package the_fireplace.wgblockreplacer.config;

import com.google.common.collect.Maps;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.Map;

public class BlockList extends GuiConfigEntries.SelectValueEntry {
	public static final Map<Object, String> entries = Maps.newHashMap();

	public BlockList(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
		super(owningScreen, owningEntryList, configElement, entries);
	}
	//TODO: Override rendering code and add a rendering of the highlighted block
}
