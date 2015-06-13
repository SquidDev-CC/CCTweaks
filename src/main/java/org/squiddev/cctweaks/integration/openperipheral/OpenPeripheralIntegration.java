package org.squiddev.cctweaks.integration.openperipheral;

import openperipheral.api.ApiAccess;
import openperipheral.api.adapter.IPeripheralAdapterRegistry;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.cctweaks.integration.ModIntegration;

/**
 * OpenPeripheral integration
 */
public class OpenPeripheralIntegration extends ModIntegration {
	public OpenPeripheralIntegration() {
		super("OpenPeripheral");
	}

	@Override
	public void postInit() {
		if (ApiAccess.isApiPresent(IPeripheralAdapterRegistry.class)) {
			DebugLogger.debug("Registering custom AdapterNetworkedInventory");
			ApiAccess.getApi(IPeripheralAdapterRegistry.class).register(new AdapterNetworkedInventory());
		} else {
			DebugLogger.error("Cannot register AdapterNetworkedInventory");
		}
	}
}
