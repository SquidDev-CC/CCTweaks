package squiddev.cctweaks;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import squiddev.cctweaks.reference.Config;
import squiddev.cctweaks.registry.ItemRegistry;

public class CCTweaksCreativeTab extends CreativeTabs {

	public CCTweaksCreativeTab() {
		super("tabCCTweaks");
	}

	@Override
	public Item getTabIconItem() {
		if (Config.enableItemComputerUpgrades) {
			return ItemRegistry.itemComputerUpgrade;
		}
		return Items.skull;
	}
}