package org.squiddev.cctweaks.core.network;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.modem.INetwork;
import dan200.computercraft.shared.peripheral.modem.IReceiver;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.NetworkHelpers;
import org.squiddev.cctweaks.api.network.NetworkVisitor;
import org.squiddev.cctweaks.api.network.Packet;

import java.util.*;

/**
 * Basic wired modem that handles peripherals and
 * computer interaction
 */
public abstract class BasicModem implements INetwork, INetworkNode {
	public static final byte MODEM_ON = 1;
	public static final byte MODEM_PERIPHERAL = 2;

	private final Object lock = new Object();

	/**
	 * Set of receivers to use
	 */
	protected final SetMultimap<Integer, IReceiver> receivers = MultimapBuilder.hashKeys().hashSetValues().build();

	/**
	 * Set of messages to transmit across the network
	 */
	protected final Queue<Packet> transmitQueue = new LinkedList<Packet>();

	/**
	 * List of peripherals on the remote network
	 *
	 * @see #peripheralWrappersByName
	 */
	protected final Map<String, IPeripheral> peripheralsByName = new HashMap<String, IPeripheral>();

	/**
	 * List of wrappers for peripherals on the remote network
	 *
	 * @see #peripheralsByName
	 */
	protected final Map<String, PeripheralAccess> peripheralWrappersByName = new HashMap<String, PeripheralAccess>();

	/**
	 * If this modem is active and can connect to peripherals
	 */
	protected boolean peripheralEnabled = false;

	/**
	 * The modem to use
	 */
	public final WiredModem modem = new WiredModem(this);

	/**
	 * The state of the modem
	 */
	public byte state;

	/**
	 * If this modem has gone looking for peripherals yet
	 */
	public boolean peripheralsKnown = false;

	@Override
	public void addReceiver(IReceiver receiver) {
		synchronized (receivers) {
			receivers.put(receiver.getChannel(), receiver);
		}
	}

	@Override
	public void removeReceiver(IReceiver receiver) {
		synchronized (receivers) {
			receivers.remove(receiver.getChannel(), receiver);
		}
	}

	@Override
	public void transmit(int channel, int replyChannel, Object payload, double range, double xPos, double yPos, double zPos, Object senderObject) {
		synchronized (transmitQueue) {
			transmitQueue.offer(new Packet(channel, replyChannel, payload, senderObject));
		}
	}

	/**
	 * Process the transmit queue
	 *
	 * @param start TE to broadcast from
	 */
	public void processQueue(TileEntity start) {
		synchronized (transmitQueue) {
			Packet packet;
			while ((packet = transmitQueue.poll()) != null) {
				NetworkHelpers.sendPacket(start.getWorldObj(), start.xCoord, start.yCoord, start.zCoord, packet);
			}
		}
	}

	/**
	 * Attach a peripheral to the modem
	 *
	 * @param name       The peripheral name
	 * @param peripheral The peripheral to attach
	 */
	public void attachPeripheral(String name, IPeripheral peripheral) {
		if (peripheralWrappersByName.containsKey(name)) {
			PeripheralAccess wrapper = new PeripheralAccess(peripheral, modem.getComputer(), name);
			peripheralWrappersByName.put(name, wrapper);
			wrapper.attach();
		}
	}

	public void detachPeriperal(String name) {
		PeripheralAccess wrapper = peripheralWrappersByName.remove(name);
		if (wrapper != null) wrapper.detach();
	}

	/**
	 * Find peripherals on the network
	 *
	 * @param start TE to search from
	 */
	public void findPeripherals(final TileEntity start) {
		synchronized (peripheralsByName) {
			final Map<String, IPeripheral> newPeripherals = new HashMap<String, IPeripheral>();
			new NetworkVisitor() {
				public void visitNode(INetworkNode node, int distance) {
					if (node != start) {
						Map<String, IPeripheral> nodePeripherals = node.getConnectedPeripherals();
						if (nodePeripherals != null) newPeripherals.putAll(nodePeripherals);
					}
				}
			}.visitNetwork(start);

			Map<String, IPeripheral> currentPeripherals = peripheralsByName;
			boolean attached = modem != null && modem.getComputer() != null;

			Iterator<String> it = currentPeripherals.keySet().iterator();
			while (it.hasNext()) {
				String name = it.next();
				if (!newPeripherals.containsKey(name)) {
					it.remove();
					detachPeriperal(name);
				}

			}

			for (String name : newPeripherals.keySet()) {
				if (!currentPeripherals.containsKey(name)) {
					IPeripheral peripheral = newPeripherals.get(name);
					if (peripheral != null) {
						currentPeripherals.put(name, peripheral);

						if (attached) attachPeripheral(name, peripheral);
					}
				}
			}

			peripheralsKnown = true;
		}
	}

	@Override
	public void receivePacket(Packet packet, int distanceTravelled) {
		synchronized (receivers) {
			for (IReceiver receiver : receivers.get(packet.channel)) {
				receiver.receive(packet.replyChannel, packet.payload, distanceTravelled, packet.senderObject);
			}
		}
	}

	public void setState(byte state) {
		this.state = state;
		peripheralEnabled = (state & MODEM_PERIPHERAL) == MODEM_PERIPHERAL;
	}

	public void refreshState() {
		state = (byte) ((modem.isActive() ? MODEM_ON : 0) | (peripheralEnabled ? MODEM_PERIPHERAL : 0));
	}

	/**
	 * Toggles if the modem is enabled
	 *
	 * @return If it can connect to peripherals
	 */
	public boolean toggleEnabled() {
		if (peripheralEnabled) {
			peripheralEnabled = false;
		} else {
			peripheralEnabled = true;
			Map<String, IPeripheral> peripherals = getConnectedPeripherals();
			if (peripherals == null || peripherals.size() == 0) {
				peripheralEnabled = false;
			}
		}

		refreshState();
		return peripheralEnabled;
	}

	/**
	 * Toggles if the modem is enabled
	 *
	 * @return If it can connect to peripherals
	 */
	public boolean isEnabled() {
		return peripheralEnabled;
	}

	/**
	 * Check if the modem is a active
	 *
	 * @return If the modem is active
	 */
	public boolean isActive() {
		return modem != null && modem.isActive();
	}

	@Override
	public boolean isWireless() {
		return false;
	}

	@Override
	public boolean canBeVisited(ForgeDirection from) {
		return true;
	}

	@Override
	public boolean canVisitTo(ForgeDirection to) {
		return true;
	}

	@Override
	public void invalidateNetwork() {
		peripheralsKnown = false;
	}

	@Override
	public void networkChanged() {
	}

	@Override
	public Iterable<NetworkVisitor.SearchLoc> getExtraNodes() {
		return null;
	}

	@Override
	public Object lock() {
		return lock;
	}
}
