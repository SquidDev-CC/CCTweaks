package org.squiddev.cctweaks.core.patch.op;

import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.cctweaks.api.network.INetworkAccess;
import org.squiddev.cctweaks.api.network.INetworkedPeripheral;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.cctweaks.api.peripheral.IPeripheralProxy;
import org.squiddev.patcher.visitors.MergeVisitor;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * PeripheralProxy rewrite with INetworkedPeripheral support
 */
public abstract class PeripheralProxy_Patch implements INetworkedPeripheral, IPeripheralProxy {
	@MergeVisitor.Stub
	private final IPeripheral peripheral;

	@MergeVisitor.Stub
	public PeripheralProxy_Patch(IPeripheral peripheral) {
		this.peripheral = peripheral;
	}

	@Override
	public void attachToNetwork(@Nonnull INetworkAccess network, @Nonnull String name) {
		if (peripheral instanceof INetworkedPeripheral) {
			((INetworkedPeripheral) peripheral).attachToNetwork(network, name);
		}
	}

	@Override
	public void detachFromNetwork(@Nonnull INetworkAccess network, @Nonnull String name) {
		if (peripheral instanceof INetworkedPeripheral) {
			((INetworkedPeripheral) peripheral).detachFromNetwork(network, name);
		}
	}

	@Override
	public void networkInvalidated(@Nonnull INetworkAccess network, @Nonnull Map<String, IPeripheral> oldPeripherals, @Nonnull Map<String, IPeripheral> newPeripherals) {
		if (peripheral instanceof INetworkedPeripheral) {
			((INetworkedPeripheral) peripheral).networkInvalidated(network, oldPeripherals, newPeripherals);
		}
	}

	@Override
	public void receivePacket(@Nonnull INetworkAccess network, @Nonnull Packet packet, double distanceTravelled) {
		if (peripheral instanceof INetworkedPeripheral) {
			((INetworkedPeripheral) peripheral).receivePacket(network, packet, distanceTravelled);
		}
	}

	@Override
	public IPeripheral getBasePeripheral() {
		return peripheral;
	}
}
