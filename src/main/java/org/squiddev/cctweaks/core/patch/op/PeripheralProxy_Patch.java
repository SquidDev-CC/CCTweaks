package org.squiddev.cctweaks.core.patch.op;

import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.cctweaks.api.network.INetworkAccess;
import org.squiddev.cctweaks.api.network.INetworkedPeripheral;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.cctweaks.api.peripheral.IPeripheralProxy;
import org.squiddev.cctweaks.core.asm.patch.MergeVisitor;

import java.util.Map;

/**
 * PeripheralProxy rewrite with INetworkedPeripheral support
 */
public class PeripheralProxy_Patch implements INetworkedPeripheral, IPeripheralProxy {
	@MergeVisitor.Stub
	private final IPeripheral peripheral;

	@MergeVisitor.Stub
	public PeripheralProxy_Patch(IPeripheral peripheral) {
		this.peripheral = peripheral;
	}

	@Override
	public void attachToNetwork(INetworkAccess network, String name) {
		if (peripheral instanceof INetworkedPeripheral) {
			((INetworkedPeripheral) peripheral).attachToNetwork(network, name);
		}
	}

	@Override
	public void detachFromNetwork(INetworkAccess network, String name) {
		if (peripheral instanceof INetworkedPeripheral) {
			((INetworkedPeripheral) peripheral).detachFromNetwork(network, name);
		}
	}

	@Override
	public void networkInvalidated(INetworkAccess network, Map<String, IPeripheral> oldPeripherals) {
		if (peripheral instanceof INetworkedPeripheral) {
			((INetworkedPeripheral) peripheral).networkInvalidated(network, oldPeripherals);
		}
	}

	@Override
	public void receivePacket(INetworkAccess network, Packet packet, double distanceTravelled) {
		if (peripheral instanceof INetworkedPeripheral) {
			((INetworkedPeripheral) peripheral).receivePacket(network, packet, distanceTravelled);
		}
	}

	@Override
	public IPeripheral getBasePeripheral() {
		return peripheral;
	}
}
