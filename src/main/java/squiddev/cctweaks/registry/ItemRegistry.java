package squiddev.cctweaks.registry;

import squiddev.cctweaks.items.ItemBase;
import squiddev.cctweaks.items.ItemComputerUpgrade;
import squiddev.cctweaks.reference.Config;
import squiddev.cctweaks.turtle.TurtleToolHost;

public class ItemRegistry {

	public static ItemBase itemComputerUpgrade;
	public static TurtleToolHost toolHost;

	public static void init() {
		if (Config.enableItemComputerUpgrades) {
			itemComputerUpgrade = new ItemComputerUpgrade();
			itemComputerUpgrade.registerItem();
		}

		if(Config.enableTurtleToolHost) {
			toolHost = new TurtleToolHost();
			toolHost.registerItem();
		}
	}
}