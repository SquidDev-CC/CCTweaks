package squiddev.cctweaks.registry;

import squiddev.cctweaks.items.ItemBase;
import squiddev.cctweaks.items.ItemComputerUpgrade;
import squiddev.cctweaks.reference.Config;

public class ItemRegistry {

	public static ItemBase itemComputerUpgrade;

	public static void init() {
		if (Config.enableItemComputerUpgrades) {
			itemComputerUpgrade = new ItemComputerUpgrade();
			itemComputerUpgrade.registerItem();
		}
	}
}