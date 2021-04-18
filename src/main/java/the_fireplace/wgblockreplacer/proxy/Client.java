package the_fireplace.wgblockreplacer.proxy;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import the_fireplace.wgblockreplacer.api.config.BlockRiskAssessor;
import the_fireplace.wgblockreplacer.api.config.ConfigAccess;
import the_fireplace.wgblockreplacer.config.BlockList;

@SideOnly(Side.CLIENT)
public final class Client extends Common {
	@Override
	public String translateToLocal(String key, Object... args) {
		return I18n.format(key, args);
	}

	@Override
	public void initBlockList() {
		BlockList.entries.clear();
		IForgeRegistry<Block> registry = ForgeRegistries.BLOCKS;
		Object[] reg = registry.getKeys().toArray();
		for (Object element : reg) {
			String id = element.toString();
			Block block = Block.getBlockFromName(id);
			if (block == null) {
				continue;
			}
			String name = translateToLocal(block.getTranslationKey() + ".name");
			if (!name.contains("tile.") && !name.contains(".name")) {
				if (ConfigAccess.getInstance().allowRiskyBlocks() || !BlockRiskAssessor.getInstance().isRisky(block)) {
					BlockList.entries.put(id, name);
				}
			}
		}
	}
}
