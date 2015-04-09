package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.BlockCable;
import dan200.computercraft.shared.peripheral.modem.IReceiver;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.NetworkRegistry;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.cctweaks.core.asm.patch.Visitors;

import java.util.*;

@SuppressWarnings("all")
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

	@Override
	public void addReceiver(IReceiver receiver) {
		synchronized (m_receivers) {
			int channel = receiver.getChannel();
			Set<IReceiver> receivers = this.m_receivers.get(channel);
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
			Set<IReceiver> receivers = this.m_receivers.get(channel);
			if (receivers != null) {
				receivers.remove(receiver);
			}
		}
	}

	private void attachPeripheral(String periphName, IPeripheral peripheral) {
		if (!m_peripheralWrappersByName.containsKey(periphName)) {
			RemotePeripheralWrapper wrapper = new RemotePeripheralWrapper(peripheral, m_modem.getComputer(), periphName);
			m_peripheralWrappersByName.put(periphName, wrapper);
			wrapper.attach();
		}
	}

	private void detachPeripheral(String periphName) {
		if (m_peripheralWrappersByName.containsKey(periphName)) {
			RemotePeripheralWrapper wrapper = m_peripheralWrappersByName.get(periphName);
			m_peripheralWrappersByName.remove(periphName);
			wrapper.detach();
		}
	}

	@Override
	public void networkChanged() {
		if (!this.worldObj.isRemote) {
			if (!this.m_destroyed) {
				searchNetwork(new ICableVisitor() {
					public void visit(INetworkNode node, int distance) {
						synchronized (node.lock()) {
							node.invalidateNetwork();
						}
					}
				});
			} else {
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
			}
		}
	}

	private void dispatchPacket(final Packet packet) {
		searchNetwork(new ICableVisitor() {
			public void visit(INetworkNode node, int distance) {
				node.receivePacket(packet, distance);
			}
		});
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
				searchNetwork(new ICableVisitor() {
					public void visit(INetworkNode node, int distance) {
						if (node != origin) {
							IPeripheral peripheral = node.getConnectedPeripheral();
							String periphName = node.getConnectedPeripheralName();
							if ((peripheral != null) && (periphName != null)) {
								newPeripheralsByName.put(periphName, peripheral);
							}
						}
					}
				});
			}

			Iterator it = m_peripheralsByName.keySet().iterator();
			while (it.hasNext()) {
				String periphName = (String) it.next();
				if (!newPeripheralsByName.containsKey(periphName)) {
					detachPeripheral(periphName);
					it.remove();
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

	private static void enqueue(Queue<SearchLoc> queue, World world, int x, int y, int z, int distanceTravelled) {
		if (y >= 0 && y < world.getHeight() && BlockCable.isCable(world, x, y, z)) {
			queue.offer(new SearchLoc(world, x, y, z, distanceTravelled));
		}
	}

	private static void visitBlock(Queue<SearchLoc> queue, Set<TileEntity> visited, SearchLoc location, ICableVisitor visitor) {
		if (location.distanceTravelled >= 256) {
			return;
		}

		TileEntity tile = location.world.getTileEntity(location.x, location.y, location.z);
		INetworkNode node;
		if (tile != null && (node = NetworkRegistry.getNode(tile)) != null) {
			if (node.canVisit() && visited.add(tile)) {
				visitor.visit(node, location.distanceTravelled + 1);

				enqueue(queue, location.world, location.x, location.y + 1, location.z, location.distanceTravelled + 1);
				enqueue(queue, location.world, location.x, location.y - 1, location.z, location.distanceTravelled + 1);
				enqueue(queue, location.world, location.x, location.y, location.z + 1, location.distanceTravelled + 1);
				enqueue(queue, location.world, location.x, location.y, location.z - 1, location.distanceTravelled + 1);
				enqueue(queue, location.world, location.x + 1, location.y, location.z, location.distanceTravelled + 1);
				enqueue(queue, location.world, location.x - 1, location.y, location.z, location.distanceTravelled + 1);
			}
		}
	}

	private void searchNetwork(ICableVisitor visitor) {
		Queue<SearchLoc> queue = new LinkedList<SearchLoc>();
		Set<TileEntity> visited = new HashSet<TileEntity>();
		enqueue(queue, worldObj, xCoord, yCoord, zCoord, 1);

		while (queue.peek() != null) {
			visitBlock(queue, visited, queue.remove(), visitor);
		}
	}

	@Visitors.Rewrite
	private static class SearchLoc {
		public final World world;
		public final int x;
		public final int y;
		public final int z;
		public final int distanceTravelled;

		public SearchLoc(World world, int x, int y, int z, int distanceTravelled) {
			this.world = world;
			this.x = x;
			this.y = y;
			this.z = z;
			this.distanceTravelled = distanceTravelled;
		}
	}

	@Visitors.Rewrite
	private interface ICableVisitor {
		void visit(INetworkNode node, int distance);
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
