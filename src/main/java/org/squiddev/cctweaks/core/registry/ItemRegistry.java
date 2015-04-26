package org.squiddev.cctweaks.core.registry;

import org.squiddev.cctweaks.core.blocks.WirelessBridge;
import org.squiddev.cctweaks.core.items.ItemBase;
import org.squiddev.cctweaks.core.items.ItemComputerUpgrade;
import org.squiddev.cctweaks.core.items.ItemDebugger;
import org.squiddev.cctweaks.core.items.ItemNetworkBinder;
import org.squiddev.cctweaks.core.reference.Config;

public class ItemRegistry {
	public static ItemBase itemComputerUpgrade;
	public static ItemBase itemDebugger;

	public static void init() {
		if (Config.config.enableComputerUpgrades) {
			ItemBase item = itemComputerUpgrade = new ItemComputerUpgrade();
			item.registerItem();
		}

		if (Config.config.enableDebugWand) {
			itemDebugger = new ItemDebugger();
			itemDebugger.registerItem();
		}

		new ItemNetworkBinder().registerItem();
		new WirelessBridge().registerBlock();
	}
}
