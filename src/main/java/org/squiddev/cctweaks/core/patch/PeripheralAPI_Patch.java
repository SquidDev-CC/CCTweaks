package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.core.apis.IAPIEnvironment;
import dan200.computercraft.core.apis.PeripheralAPI;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.filesystem.FileSystem;
import org.squiddev.cctweaks.api.lua.ArgumentDelegator;
import org.squiddev.cctweaks.api.lua.IArguments;
import org.squiddev.cctweaks.api.lua.IBinaryHandler;
import org.squiddev.cctweaks.api.lua.ILuaObjectWithArguments;
import org.squiddev.cctweaks.api.network.INetworkAccess;
import org.squiddev.cctweaks.api.network.INetworkedPeripheral;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.cctweaks.core.lua.LuaConverter;
import org.squiddev.patcher.visitors.MergeVisitor;

import java.util.HashMap;
import java.util.Map;

public class PeripheralAPI_Patch extends PeripheralAPI implements ILuaObjectWithArguments {
	@MergeVisitor.Stub
	private FileSystem m_fileSystem;
	@MergeVisitor.Stub
	private PeripheralWrapper[] m_peripherals;

	@MergeVisitor.Stub
	public PeripheralAPI_Patch(IAPIEnvironment _environment) {
		super(_environment);
	}

	private int parseSide(Object[] args) throws LuaException {
		if (args.length < 1 || args[0] == null || !(args[0] instanceof byte[])) {
			throw new LuaException("Expected string");
		}

		String side = new String((byte[]) args[0]);
		for (int n = 0; n < Computer.s_sideNames.length; n++) {
			if (side.equals(Computer.s_sideNames[n])) return n;
		}
		return -1;
	}

	private int parseSide(IArguments arguments) throws LuaException {
		String side = arguments.getString(0);
		for (int n = 0; n < Computer.s_sideNames.length; n++) {
			if (side.equals(Computer.s_sideNames[n])) return n;
		}
		return -1;
	}

	@Override
	public Object[] callMethod(ILuaContext context, int method, IArguments args) throws LuaException, InterruptedException {
		if (method == 3) {
			if (args.size() >= 2 && args.getArgument(1, false) != null && args.getArgument(1, false) instanceof String) {
				String methodName = args.getString(1);
				int side = this.parseSide(args);
				if (side >= 0) {
					PeripheralWrapper p = null;
					synchronized (m_peripherals) {
						p = m_peripherals[side];
					}

					if (p != null) {
						return p.call(context, methodName, args);
					}
				}

				throw new LuaException("No peripheral attached");
			}

			throw new LuaException("Expected string, string");
		}
		return new Object[0];
	}

	private abstract class PeripheralWrapper implements INetworkAccess, IComputerAccess {
		@MergeVisitor.Stub
		private final String m_side;
		@MergeVisitor.Stub
		private final IPeripheral m_peripheral;
		@MergeVisitor.Stub
		private Map<String, Integer> m_methodMap;

		@MergeVisitor.Stub
		public PeripheralWrapper(IPeripheral peripheral, String side) {
			m_side = side;
			m_peripheral = peripheral;
		}

		//region Networking
		@MergeVisitor.Stub
		@MergeVisitor.Rename(from = {"attach"})
		public void nativeAttach() {
		}

		public synchronized void attach() {
			nativeAttach();
			if (this.m_peripheral instanceof INetworkedPeripheral) {
				((INetworkedPeripheral) this.m_peripheral).attachToNetwork(this, this.m_side);
			}
		}

		@MergeVisitor.Stub
		@MergeVisitor.Rename(from = {"detach"})
		public void nativeDetach() {
		}

		public synchronized void detach() {
			if (this.m_peripheral instanceof INetworkedPeripheral) {
				((INetworkedPeripheral) this.m_peripheral).detachFromNetwork(this, this.m_side);
			}
			nativeDetach();
		}

		@Override
		public Map<String, IPeripheral> getPeripheralsOnNetwork() {
			Map<String, IPeripheral> peripheralMap = new HashMap<String, IPeripheral>();
			for (int i = 0; i < 6; ++i) {
				if (m_peripherals[i] != null) {
					peripheralMap.put(Computer.s_sideNames[i], m_peripherals[i].m_peripheral);
				}
			}
			return peripheralMap;
		}

		@Override
		public void invalidateNetwork() {
		}

		@Override
		public boolean transmitPacket(Packet packet) {
			return false;
		}
		//endregion

		//region Binary support
		public Object[] call(ILuaContext context, String methodName, IArguments arguments) throws InterruptedException, LuaException {
			int method = -1;

			synchronized (this) {
				if (m_methodMap.containsKey(methodName)) {
					method = m_methodMap.get(methodName);
				}
			}

			if (method >= 0) {
				return ArgumentDelegator.delegatePeripheral(m_peripheral, this, context, method, arguments);
			}

			throw new LuaException("No such method " + methodName);
		}

		public Object[] call(ILuaContext context, String methodName, Object[] arguments) throws InterruptedException, LuaException {
			int method = -1;

			synchronized (this) {
				if (m_methodMap.containsKey(methodName)) {
					method = m_methodMap.get(methodName);
				}
			}

			if (method >= 0) {
				if (!(m_peripheral instanceof IBinaryHandler)) LuaConverter.toStrings(arguments);
				return m_peripheral.callMethod(this, context, method, arguments);
			}

			throw new LuaException("No such method " + methodName);
		}
		//endregion
	}
}
