package org.squiddev.cctweaks.integration.peripheralspp;

import com.austinv11.peripheralsplusplus.hooks.ComputerCraftRegistry;
import cpw.mods.fml.common.Optional;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.cctweaks.integration.ModIntegration;

/**
 * Adds pocket computer binding
 */
public class PeripheralsPlusPlusIntegration extends ModIntegration {
	public PeripheralsPlusPlusIntegration() {
		super("PeripheralsPlusPlus");
	}

	@Override
	@Optional.Method(modid = "PeripheralsPlusPlus")
	public void init() {
		super.init();
		try {
			if (Config.Network.WirelessBridge.pocketEnabled) {
				DebugLogger.debug("Registering PocketWirelessBinding");
				ComputerCraftRegistry.registerPocketUpgrade(new PocketWirelessBinding());
			}
		} catch (Exception e) {
			DebugLogger.error("Cannot register Peripherals++ upgrades", e);
		}
	}
}
