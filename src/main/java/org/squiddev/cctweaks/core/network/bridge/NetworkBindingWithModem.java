package org.squiddev.cctweaks.core.network.bridge;

import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHidden;
import org.squiddev.cctweaks.core.network.modem.BasicModem;
import org.squiddev.cctweaks.core.network.modem.BasicModemPeripheral;

import java.util.Set;

/**
 * A network binding that comes with an attached modem.
 * This modem is hidden from other network components - it can
 * only be used from a computer.
 *
 * This is so there can be a direct computer -> binding connection
 * but users do not get confused that bindings normally peripherals.
 */
public class NetworkBindingWithModem extends NetworkBinding {
	public NetworkBindingWithModem(IWorldPosition position) {
		super(position);
	}

	protected BindingModem modem = createModem();

	protected class BindingModem extends BasicModem {
		@Override
		public IWorldPosition getPosition() {
			return position;
		}

		@Override
		protected BasicModemPeripheral createPeripheral() {
			return new BindingModemPeripheral(this);
		}
	}

	protected class BindingModemPeripheral extends BasicModemPeripheral<BindingModem> implements IPeripheralHidden {
		public BindingModemPeripheral(BindingModem modem) {
			super(modem);
		}
	}

	public BindingModem createModem() {
		return new BindingModem();
	}

	@Override
	public Set<INetworkNode> getConnectedNodes() {
		Set<INetworkNode> nodes = super.getConnectedNodes();
		nodes.add(modem);
		return nodes;
	}

	@Override
	public void connect() {
		super.connect();
		getAttachedNetwork().formConnection(this, modem);
	}

	@Override
	public void destroy() {
		super.destroy();
		modem.destroy();
	}

	public BasicModem getModem() {
		return modem;
	}
}
