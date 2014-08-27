package squiddev.cctweaks;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import squiddev.cctweaks.reference.Config;

public class CCTweaksCreativeTab extends CreativeTabs {

	public CCTweaksCreativeTab() {
		super("tabCCTweaks");
	}

	@Override
	public int getTabIconItemIndex() {
		if (Config.enableItemComputerUpgrades) {
			return Config.itemIdComputerUpgrade;
		}
		return Item.skull.itemID;
	}

}