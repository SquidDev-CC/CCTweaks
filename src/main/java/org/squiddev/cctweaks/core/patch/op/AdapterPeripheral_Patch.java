package org.squiddev.cctweaks.core.patch.op;

import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.cctweaks.api.network.INetworkAccess;
import org.squiddev.cctweaks.api.network.INetworkedPeripheral;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.cctweaks.api.peripheral.IPeripheralTargeted;
import org.squiddev.cctweaks.core.network.NetworkAccessDelegate;
import org.squiddev.patcher.visitors.MergeVisitor;

import java.util.Map;

/**
 * Before changing anything check with
 * <ul>
 * <li>{@link org.squiddev.cctweaks.core.asm.PatchOpenPeripheralAdapter}</li>
 * <li>{@link org.squiddev.cctweaks.core.patch.op.AdapterPeripheral_Patch}</li>
 * </ul>
 * to make sure it doesn't break anything
 */
public abstract class AdapterPeripheral_Patch implements IPeripheralTargeted, INetworkedPeripheral {
	@MergeVisitor.Stub
	protected final Object target;

	protected NetworkAccessDelegate network;

	@MergeVisitor.Stub
	public AdapterPeripheral_Patch(Object target) {
		this.target = target;
	}

	@Override
	public Object getTarget() {
		return target;
	}

	public NetworkAccessDelegate getNetworkAccess() {
		if (network == null) return network = new NetworkAccessDelegate();
		return network;
	}

	@Override
	public void attachToNetwork(INetworkAccess network, String name) {
		getNetworkAccess().add(network);
	}

	@Override
	public void detachFromNetwork(INetworkAccess network, String name) {
		getNetworkAccess().remove(network);
	}

	@Override
	public void networkInvalidated(INetworkAccess network, Map<String, IPeripheral> oldPeripherals, Map<String, IPeripheral> newPeripherals) {
	}

	@Override
	public void receivePacket(INetworkAccess network, Packet packet, double distanceTravelled) {
	}
}
