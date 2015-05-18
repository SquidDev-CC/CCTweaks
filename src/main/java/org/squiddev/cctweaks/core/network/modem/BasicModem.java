package org.squiddev.cctweaks.core.network.modem;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.modem.INetwork;
import dan200.computercraft.shared.peripheral.modem.IReceiver;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.Packet;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Basic wired modem that handles peripherals and
 * computer interaction
 */
public abstract class BasicModem implements INetwork, IWorldNetworkNode {
	public static final byte MODEM_ON = 1;
	public static final byte MODEM_PERIPHERAL = 2;

	/**
	 * Set of receivers to use
	 */
	protected final SetMultimap<Integer, IReceiver> receivers = MultimapBuilder.hashKeys().hashSetValues().build();

	/**
	 * Set of messages to transmit across the network
	 */
	protected final Queue<Packet> transmitQueue = new LinkedList<Packet>();

	/**
	 * List of wrappers for peripherals on the remote network
	 */
	public final Map<String, PeripheralAccess> peripheralWrappersByName = new HashMap<String, PeripheralAccess>();

	/**
	 * If this modem is active and can connect to peripherals
	 */
	protected boolean peripheralEnabled = false;

	/**
	 * The modem to use
	 */
	public final BasicModemPeripheral modem = createPeripheral();

	/**
	 * The state of the modem
	 */
	public byte state;

	/**
	 * The network this modem is attached to.
	 */
	INetworkController networkController;

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
	 */
	public void processQueue() {
		synchronized (transmitQueue) {
			Packet packet;
			IWorldPosition position = getPosition();
			while ((packet = transmitQueue.poll()) != null) {
				networkController.transmitPacket(this, packet);
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
		if (!peripheralWrappersByName.containsKey(name)) {
			PeripheralAccess wrapper = new PeripheralAccess(peripheral, modem.getComputer(), name);
			peripheralWrappersByName.put(name, wrapper);
			wrapper.attach();
		}
	}

	/**
	 * Detach a peripheral from the modem
	 *
	 * @param name The peripheral name
	 */
	public void detachPeripheral(String name) {
		PeripheralAccess wrapper = peripheralWrappersByName.remove(name);
		if (wrapper != null) wrapper.detach();
	}

	@Override
	public void receivePacket(INetworkController network, Packet packet, double distanceTravelled) {
		synchronized (receivers) {
			for (IReceiver receiver : receivers.get(packet.channel)) {
				receiver.receive(packet.replyChannel, packet.payload, distanceTravelled, packet.senderObject);
			}
		}
	}

	/**
	 * Set the state of the modem
	 *
	 * @param state The flags to set the state with
	 * @see #MODEM_ON
	 * @see #MODEM_PERIPHERAL
	 */
	public void setState(byte state) {
		this.state = state;
		peripheralEnabled = (state & MODEM_PERIPHERAL) == MODEM_PERIPHERAL;
	}

	/**
	 * Recalculate the state of the modem
	 *
	 * @see #MODEM_ON
	 * @see #MODEM_PERIPHERAL
	 */
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
			updateEnabled();
		}

		refreshState();
		return peripheralEnabled;
	}

	/**
	 * Scans for peripherals and disabled if none found
	 *
	 * @return If the connection state changed
	 */
	public boolean updateEnabled() {
		if (!peripheralEnabled) return false;

		Map<String, IPeripheral> peripherals = getConnectedPeripherals();
		if (peripherals == null || peripherals.size() == 0) {
			peripheralEnabled = false;
			return true;
		}

		return false;
	}

	/**
	 * Check if the modem is enabled
	 *
	 * @return If it can connect to peripherals
	 */
	public boolean isEnabled() {
		return peripheralEnabled;
	}

	/**
	 * Check if the modem is active
	 *
	 * @return If the modem has a channel open
	 */
	public boolean isActive() {
		return modem != null && modem.isActive();
	}

	/**
	 * Create a new peripheral for this modem
	 *
	 * @return The created peripheral
	 */
	protected BasicModemPeripheral createPeripheral() {
		return new BasicModemPeripheral<BasicModem>(this);
	}

	@Override
	public boolean isWireless() {
		return false;
	}

	@Override
	public boolean canConnect(ForgeDirection from) {
		return true;
	}

	@Override
	public void networkInvalidated(Map<String, IPeripheral> oldPeripherals) {
		for (Map.Entry<String, PeripheralAccess> wrapper : peripheralWrappersByName.entrySet()) {
			if (!networkController.getPeripheralsOnNetwork().containsKey(wrapper.getKey())) {
				// Wrapper removed
				detachPeripheral(wrapper.getKey());
			}
		}
	}

	@Override
	public void detachFromNetwork() {
		this.networkController = null;
	}

	@Override
	public void attachToNetwork(INetworkController networkController) {
		this.networkController = networkController;
	}

	@Override
	public INetworkController getAttachedNetwork() {
		return networkController;
	}

	public void destroy() {
		networkController.removeNode(this);
		modem.destroy();
	}
}
