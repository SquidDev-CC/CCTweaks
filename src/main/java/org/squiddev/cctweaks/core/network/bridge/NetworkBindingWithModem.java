package org.squiddev.cctweaks.core.network.bridge;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHidden;
import org.squiddev.cctweaks.core.network.modem.BasicModem;
import org.squiddev.cctweaks.core.network.modem.BasicModemPeripheral;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * A network binding that comes with an attached modem.
 * This modem is hidden from other network components - it can
 * only be used from a computer.
 *
 * This is so there can be a direct computer -> binding connection
 * but users do not get confused that bindings normally peripherals.
 */
public abstract class NetworkBindingWithModem extends NetworkBinding {
	public NetworkBindingWithModem(IWorldPosition position) {
		super(position);
	}

	protected final BindingModem modem = createModem();

	protected class BindingModem extends BasicModem {
		@Nonnull
		@Override
		public IWorldPosition getPosition() {
			return NetworkBindingWithModem.this.getPosition();
		}

		@Override
		protected BasicModemPeripheral<?> createPeripheral() {
			return new BindingModemPeripheral(this);
		}
	}

	protected class BindingModemPeripheral extends BasicModemPeripheral<BindingModem> implements IPeripheralHidden {
		private final String[] methodNames;
		private final int methodOffset;
		private final NetworkBindingPeripheral bindingPeripheral = new NetworkBindingPeripheral(NetworkBindingWithModem.this);

		public BindingModemPeripheral(BindingModem modem) {

			super(modem);
			String[] methods = super.getMethodNames();
			String[] otherMethods = bindingPeripheral.getMethodNames();
			String[] newMethods = new String[methods.length + otherMethods.length];
			System.arraycopy(methods, 0, newMethods, 0, methods.length);
			System.arraycopy(otherMethods, 0, newMethods, methods.length, otherMethods.length);

			methodNames = newMethods;
			methodOffset = methods.length;
		}

		@Override
		public String[] getMethodNames() {
			return methodNames;
		}

		@Override
		public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
			int offset = method - methodOffset;
			if (offset >= 0) {
				return bindingPeripheral.callMethod(computer, context, offset, arguments);
			}

			return super.callMethod(computer, context, method, arguments);
		}

		@Override
		public IPeripheral getNetworkPeripheral() {
			return bindingPeripheral;
		}

		@Override
		protected boolean isInterdimensional() {
			return true;
		}
	}

	protected BindingModem createModem() {
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

	public abstract void markDirty();
}
