package org.squiddev.cctweaks.core.registry;

import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.blocks.BlockBase;
import org.squiddev.cctweaks.core.blocks.WirelessBridge;
import org.squiddev.cctweaks.core.items.ItemBase;
import org.squiddev.cctweaks.core.items.ItemComputerUpgrade;
import org.squiddev.cctweaks.core.items.ItemDebugger;
import org.squiddev.cctweaks.core.items.ItemNetworkBinder;

import java.util.HashSet;
import java.util.Set;

/**
 * The proxy class
 */
public class Registry implements IRegisterable {
	public static ItemBase itemComputerUpgrade;
	public static ItemBase itemDebugger;
	public static ItemBase itemNetworkBinder;

	public static BlockBase blockWirelessBridge;

	private final Set<IRegisterable> registers = new HashSet<IRegisterable>();

	protected void setup() {
		if (Config.config.enableComputerUpgrades) registers.add(itemComputerUpgrade = new ItemComputerUpgrade());
		if (Config.config.enableDebugWand) registers.add(itemDebugger = new ItemDebugger());

		registers.add(itemNetworkBinder = new ItemNetworkBinder());
		registers.add(blockWirelessBridge = new WirelessBridge());

		registers.add(new RefuelRegisters());
	}

	@Override
	public void preInit() {
		setup();

		for (IRegisterable register : registers) {
			register.preInit();
		}
	}

	@Override
	public void init() {
		for (IRegisterable register : registers) {
			register.init();
		}
	}
}
