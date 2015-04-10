package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.BlockCable;
import dan200.computercraft.shared.peripheral.modem.IReceiver;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.NetworkRegistry;
import org.squiddev.cctweaks.api.network.NetworkVisitor;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.cctweaks.core.asm.patch.Visitors;

import java.util.*;

@SuppressWarnings("all")
@Visitors.Rename(from = "dan200/computercraft/shared/peripheral/modem/TileCable$Packet", to = "org/squiddev/cctweaks/api/network/Packet")
public class TileCable_Patch extends TileCable implements INetworkNode {
	@Visitors.Stub
	private Map<Integer, Set<IReceiver>> m_receivers;
	@Visitors.Stub
	private Map<String, IPeripheral> m_peripheralsByName;
	@Visitors.Stub
	private Map<String, RemotePeripheralWrapper> m_peripheralWrappersByName;
	@Visitors.Stub
	private boolean m_peripheralsKnown;
	@Visitors.Stub
	private boolean m_destroyed;
	@Visitors.Stub
	private Queue<Packet> m_transmitQueue;

	@Override
	public void addReceiver(IReceiver receiver) {
		synchronized (m_receivers) {
			int channel = receiver.getChannel();
			Set<IReceiver> receivers = m_receivers.get(channel);
			if (receivers == null) {
				receivers = new HashSet<IReceiver>();
				m_receivers.put(channel, receivers);
			}
			receivers.add(receiver);
		}
	}

	@Override
	public void removeReceiver(IReceiver receiver) {
		synchronized (m_receivers) {
			int channel = receiver.getChannel();
			Set<IReceiver> receivers = m_receivers.get(channel);
			if (receivers != null) {
				receivers.remove(receiver);
			}
		}
	}

	@Override
	public void transmit(int channel, int replyChannel, Object payload, double range, double xPos, double yPos, double zPos, Object senderObject) {
		synchronized (m_transmitQueue) {
			m_transmitQueue.offer(new Packet(channel, replyChannel, payload, senderObject));
		}
	}

	private void attachPeripheral(String name, IPeripheral peripheral) {
		if (!m_peripheralWrappersByName.containsKey(name)) {
			RemotePeripheralWrapper wrapper = new RemotePeripheralWrapper(peripheral, m_modem.getComputer(), name);
			m_peripheralWrappersByName.put(name, wrapper);
			wrapper.attach();
		}
	}

	private void detachPeripheral(String name) {
		if (m_peripheralWrappersByName.containsKey(name)) {
			RemotePeripheralWrapper wrapper = m_peripheralWrappersByName.get(name);
			m_peripheralWrappersByName.remove(name);
			wrapper.detach();
		}
	}

	@Override
	public void networkChanged() {
		if (!worldObj.isRemote) {
			if (m_destroyed) {
				for (int dir = 0; dir < 6; dir++) {
					int x = xCoord + Facing.offsetsXForSide[dir];
					int y = yCoord + Facing.offsetsYForSide[dir];
					int z = zCoord + Facing.offsetsZForSide[dir];
					if (y >= 0 && y < worldObj.getHeight() && BlockCable.isCable(worldObj, x, y, z)) {
						TileEntity tile = worldObj.getTileEntity(x, y, z);
						INetworkNode node;
						if (tile != null && (node = NetworkRegistry.getNode(tile)) != null) {
							node.networkChanged();
						}
					}
				}
			} else {
				new NetworkVisitor() {
					@Visitors.Rewrite
					boolean ANNOTATION;

					public void visitNode(INetworkNode node, int distance) {
						synchronized (node.lock()) {
							node.invalidateNetwork();
						}
					}
				}.visitNetwork(this);
			}
		}
	}

	private void dispatchPacket(final Packet packet) {
		new NetworkVisitor() {
			@Visitors.Rewrite
			boolean ANNOTATION;

			public void visitNode(INetworkNode node, int distance) {
				node.receivePacket(packet, distance);
			}
		}.visitNetwork(this);
	}

	@Override
	public boolean canVisit() {
		return !m_destroyed;
	}

	@Visitors.Stub
	public IPeripheral getConnectedPeripheral() {
		return null;
	}

	@Override
	public void receivePacket(Packet packet, int distanceTravelled) {
		synchronized (m_receivers) {
			Set<IReceiver> receivers = m_receivers.get(packet.channel);
			if (receivers != null) {
				for (IReceiver receiver : receivers) {
					receiver.receive(packet.replyChannel, packet.payload, distanceTravelled, packet.senderObject);
				}
			}
		}
	}

	@Override
	public void invalidateNetwork() {
		m_peripheralsKnown = false;
	}

	@Override
	public Object lock() {
		return m_peripheralsByName;
	}

	private void findPeripherals() {
		final TileCable_Patch origin = this;
		synchronized (m_peripheralsByName) {
			final Map<String, IPeripheral> newPeripheralsByName = new HashMap<String, IPeripheral>();
			if (getPeripheralType() == PeripheralType.WiredModemWithCable) {
				new NetworkVisitor() {
					@Visitors.Rewrite
					boolean ANNOTATION;

					public void visitNode(INetworkNode node, int distance) {
						if (node != origin) {
							IPeripheral peripheral = node.getConnectedPeripheral();
							String periphName = node.getConnectedPeripheralName();
							if (peripheral != null && periphName != null) {
								newPeripheralsByName.put(periphName, peripheral);
							}
						}
					}
				}.visitNetwork(this);
			}

			Iterator it = m_peripheralsByName.keySet().iterator();
			while (it.hasNext()) {
				String periphName = (String) it.next();
				if (!newPeripheralsByName.containsKey(periphName)) {
					it.remove();
					detachPeripheral(periphName);
				}

			}

			for (String periphName : newPeripheralsByName.keySet()) {
				if (!m_peripheralsByName.containsKey(periphName)) {
					IPeripheral peripheral = newPeripheralsByName.get(periphName);
					if (peripheral != null) {
						m_peripheralsByName.put(periphName, peripheral);
						if (isAttached()) {
							attachPeripheral(periphName, peripheral);
						}
					}
				}
			}
		}
	}

	@Visitors.Stub
	private static class RemotePeripheralWrapper {
		public RemotePeripheralWrapper(IPeripheral peripheral, IComputerAccess computer, String name) {

		}

		public void attach() {
		}

		public void detach() {
		}
	}
}
