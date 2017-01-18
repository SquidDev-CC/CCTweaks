package org.squiddev.cctweaks.blocks.debug;

import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.apache.commons.lang3.StringUtils;
import org.squiddev.cctweaks.api.network.INetworkAccess;
import org.squiddev.cctweaks.api.network.INetworkedPeripheral;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.cctweaks.core.network.NetworkAccessDelegate;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Map;

/**
 * A networked peripheral that logs events to the console
 */
public class TileDebugNetworkedPeripheral extends TileDebugPeripheral {
	@Override
	protected IPeripheral createPeripheral(int side) {
		return new NetworkedPeripheral(side);
	}

	public class NetworkedPeripheral extends SidedPeripheral implements INetworkedPeripheral {
		private final NetworkAccessDelegate access = new NetworkAccessDelegate();

		public NetworkedPeripheral(int side) {
			super(side);
		}

		@Override
		public String getType() {
			return "networked";
		}

		@Override
		public String[] getMethodNames() {
			String[] names = super.getMethodNames();
			String[] newNames = Arrays.copyOf(names, names.length + 1);
			newNames[names.length] = "getPeripherals";
			return newNames;
		}

		@Override
		public Object[] callMethod(IComputerAccess computer, ILuaContext context, int function, Object[] arguments) throws LuaException, InterruptedException {
			int parentFunctions = super.getMethodNames().length;

			switch (function - parentFunctions) {
				case 0: {
					Map<String, String> out = Maps.newHashMap();
					for (Map.Entry<String, IPeripheral> peripheral : access.getPeripheralsOnNetwork().entrySet()) {
						out.put(peripheral.getKey(), peripheral.getValue().toString());
					}
					return new Object[]{out};
				}
				default:
					return super.callMethod(computer, context, function, arguments);
			}
		}

		@Override
		public void attachToNetwork(@Nonnull INetworkAccess network, @Nonnull String name) {
			DebugLogger.debug("Attaching to network " + network + " with name " + name);
			access.add(network);
		}

		@Override
		public void detachFromNetwork(@Nonnull INetworkAccess network, @Nonnull String name) {
			DebugLogger.debug("Detaching from network " + network + " with name " + name);
			access.remove(network);
		}

		@Override
		public void networkInvalidated(@Nonnull INetworkAccess network, @Nonnull Map<String, IPeripheral> oldPeripherals, @Nonnull Map<String, IPeripheral> newPeripherals) {
			DebugLogger.debug(
				"Node invalidated at %s, %s, %s\n - Removed: %s\n - Added:   %s",
				pos.getX(), pos.getY(), pos.getZ(),
				StringUtils.join(oldPeripherals.keySet(), ", "),
				StringUtils.join(newPeripherals.keySet(), ", ")
			);
		}

		@Override
		public void receivePacket(@Nonnull INetworkAccess network, @Nonnull Packet packet, double distanceTravelled) {
			DebugLogger.debug("Received packet from " + distanceTravelled + " blocks away");
		}
	}
}
