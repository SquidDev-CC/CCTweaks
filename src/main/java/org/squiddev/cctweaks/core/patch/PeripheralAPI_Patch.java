package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.core.apis.IAPIEnvironment;
import dan200.computercraft.core.apis.PeripheralAPI;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.filesystem.FileSystem;
import org.squiddev.cctweaks.api.network.INetworkAccess;
import org.squiddev.cctweaks.api.network.INetworkedPeripheral;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.patcher.visitors.MergeVisitor;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * - Implement {@link INetworkAccess} on the peripheral wrapper and attach/detach networked peripherals.
 * - Remove/move synchronisation in a couple of places to solve problems with multi-threading.
 */
public class PeripheralAPI_Patch extends PeripheralAPI {
	@MergeVisitor.Stub
	private IAPIEnvironment m_environment;
	@MergeVisitor.Stub
	private PeripheralWrapper[] m_peripherals;
	@MergeVisitor.Stub
	private FileSystem m_fileSystem;

	@MergeVisitor.Stub
	public PeripheralAPI_Patch(IAPIEnvironment _environment) {
		super(_environment);
	}


	private abstract class PeripheralWrapper implements INetworkAccess, IComputerAccess {
		@MergeVisitor.Stub
		private final String m_side;
		@MergeVisitor.Stub
		private final IPeripheral m_peripheral;
		@MergeVisitor.Stub
		private boolean m_attached;
		@MergeVisitor.Stub
		private Set<String> m_mounts;

		@MergeVisitor.Stub
		public PeripheralWrapper(IPeripheral peripheral, String side) {
			m_side = side;
			m_peripheral = peripheral;
		}

		@MergeVisitor.Stub
		@MergeVisitor.Rename(from = {"attach"})
		public void nativeAttach() {
		}

		public synchronized void attach() {
			nativeAttach();
			if (m_peripheral instanceof INetworkedPeripheral) {
				((INetworkedPeripheral) m_peripheral).attachToNetwork(this, m_side);
			}
		}

		public void detach() {
			if (m_peripheral instanceof INetworkedPeripheral) {
				((INetworkedPeripheral) m_peripheral).detachFromNetwork(this, m_side);
			}

			m_peripheral.detach(this);

			synchronized (this) {
				m_attached = false;
				for (String mount : m_mounts) {
					m_fileSystem.unmount(mount);
				}
				m_mounts.clear();
			}
		}

		@Nonnull
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
		public boolean transmitPacket(@Nonnull Packet packet) {
			return false;
		}

		public int getID() {
			if (!m_attached) {
				throw new RuntimeException("You are not attached to this Computer");
			} else {
				return m_environment.getComputerID();
			}
		}

		public void queueEvent(String event, Object[] arguments) {
			if (!m_attached) {
				throw new RuntimeException("You are not attached to this Computer");
			} else {
				m_environment.queueEvent(event, arguments);
			}
		}

		public String getAttachmentName() {
			if (!m_attached) {
				throw new RuntimeException("You are not attached to this Computer");
			} else {
				return m_side;
			}
		}
	}
}
