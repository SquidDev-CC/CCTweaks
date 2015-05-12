package org.squiddev.cctweaks.core.patch.op;

import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.cctweaks.api.network.INetworkAccess;
import org.squiddev.cctweaks.api.network.INetworkedPeripheral;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.cctweaks.api.peripheral.IPeripheralProxy;
import org.squiddev.cctweaks.core.asm.patch.MergeVisitor;

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
	public void networkInvalidated(INetworkAccess network) {
		if (peripheral instanceof INetworkedPeripheral) {
			((INetworkedPeripheral) peripheral).networkInvalidated(network);
		}
	}

	@Override
	public void receivePacket(Packet packet, int distanceTravelled) {
		if (peripheral instanceof INetworkedPeripheral) {
			((INetworkedPeripheral) peripheral).receivePacket(packet, distanceTravelled);
		}
	}

	@Override
	public IPeripheral getBasePeripheral() {
		return peripheral;
	}
}
