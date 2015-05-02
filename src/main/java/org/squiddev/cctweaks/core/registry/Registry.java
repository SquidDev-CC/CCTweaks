package org.squiddev.cctweaks.core.registry;

import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.blocks.BaseBlock;
import org.squiddev.cctweaks.core.network.bridge.WirelessBridgeBlock;
import org.squiddev.cctweaks.core.items.BaseItem;
import org.squiddev.cctweaks.core.items.ComputerUpgradeItem;
import org.squiddev.cctweaks.core.items.DebuggerItem;
import org.squiddev.cctweaks.core.items.DataCardItem;

import java.util.HashSet;
import java.util.Set;

/**
 * The proxy class
 */
public class Registry implements IRegisterable {
	public static BaseItem itemComputerUpgrade;
	public static BaseItem itemDebugger;
	public static BaseItem itemNetworkBinder;

	public static BaseBlock blockWirelessBridge;

	private final Set<IRegisterable> registers = new HashSet<IRegisterable>();

	protected void setup() {
		if (Config.config.enableComputerUpgrades) registers.add(itemComputerUpgrade = new ComputerUpgradeItem());
		if (Config.config.enableDebugWand) registers.add(itemDebugger = new DebuggerItem());

		registers.add(itemNetworkBinder = new DataCardItem());
		registers.add(blockWirelessBridge = new WirelessBridgeBlock());

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
