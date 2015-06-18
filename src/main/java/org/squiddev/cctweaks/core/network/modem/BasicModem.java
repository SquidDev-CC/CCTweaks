package org.squiddev.cctweaks.core.network.modem;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.modem.INetwork;
import dan200.computercraft.shared.peripheral.modem.IReceiver;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.*;
import org.squiddev.cctweaks.core.network.AbstractNode;

import java.util.*;

/**
 * Basic wired modem that handles peripherals and
 * computer interaction
 */
public abstract class BasicModem extends AbstractNode implements INetwork, IWorldNetworkNode, INetworkAccess {
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

	private boolean peripheralEnabled = false;

	/**
	 * The modem to use
	 */
	public final BasicModemPeripheral modem = createPeripheral();

	/**
	 * The state of the modem
	 */
	public byte state;

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
	public void receivePacket(Packet packet, double distanceTravelled) {
		synchronized (receivers) {
			for (IReceiver receiver : receivers.get(packet.channel)) {
				receiver.receive(packet.replyChannel, packet.payload, distanceTravelled, packet.senderObject);
			}
		}

		for (Map.Entry<String, IPeripheral> entry : this.getConnectedPeripherals().entrySet()) {
			IPeripheral value = entry.getValue();

			if (value instanceof INetworkedPeripheral) {
				((INetworkedPeripheral) value).receivePacket(this, packet, distanceTravelled);
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
		setPeripheralEnabled((state & MODEM_PERIPHERAL) == MODEM_PERIPHERAL);
	}

	/**
	 * Recalculate the state of the modem
	 *
	 * @see #MODEM_ON
	 * @see #MODEM_PERIPHERAL
	 */
	public void refreshState() {
		state = (byte) ((modem.isActive() ? MODEM_ON : 0) | (isPeripheralEnabled() ? MODEM_PERIPHERAL : 0));
	}

	/**
	 * Toggles if the modem is enabled
	 *
	 * @return If it can connect to peripherals
	 */
	public boolean toggleEnabled() {
		if (isPeripheralEnabled()) {
			setPeripheralEnabled(false);
		} else {
			setPeripheralEnabled(true);
			updateEnabled();
		}

		refreshState();
		return isPeripheralEnabled();
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
			setPeripheralEnabled(false);
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
		return isPeripheralEnabled();
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
		// Clone to prevent modification errors
		Set<String> peripheralNames = new HashSet<String>(peripheralWrappersByName.keySet());
		for (String wrapper : peripheralNames) {
			if (!networkController.getPeripheralsOnNetwork().containsKey(wrapper)) {
				// Wrapper removed
				detachPeripheral(wrapper);
			}
		}

		if (modem != null && modem.getComputer() != null) {
			for (String name : networkController.getPeripheralsOnNetwork().keySet()) {
				if (!peripheralWrappersByName.containsKey(name)) {
					IPeripheral peripheral = networkController.getPeripheralsOnNetwork().get(name);
					if (peripheral != null) {
						attachPeripheral(name, peripheral);
					}
				}
			}
		}

		for (Map.Entry<String, IPeripheral> entry : getConnectedPeripherals().entrySet()) {
			IPeripheral value = entry.getValue();

			if (value instanceof INetworkedPeripheral) {
				((INetworkedPeripheral) value).networkInvalidated(this, oldPeripherals);
			}
		}
	}

	@Override
	public void detachFromNetwork() {
		for (Map.Entry<String, IPeripheral> entry : getConnectedPeripherals().entrySet()) {
			String key = entry.getKey();
			IPeripheral value = entry.getValue();

			if (value instanceof INetworkedPeripheral) {
				((INetworkedPeripheral) value).detachFromNetwork(this, key);
			}
		}
		super.detachFromNetwork();
	}

	@Override
	public void attachToNetwork(INetworkController networkController) {
		super.attachToNetwork(networkController);
		for (Map.Entry<String, IPeripheral> entry : this.getConnectedPeripherals().entrySet()) {
			String key = entry.getKey();
			IPeripheral value = entry.getValue();

			if (value instanceof INetworkedPeripheral) {
				((INetworkedPeripheral) value).attachToNetwork(this, key);
			}
		}
	}

	public void destroy() {
		if (networkController != null) networkController.removeNode(this);
		modem.destroy();
	}

	@Override
	public Map<String, IPeripheral> getPeripheralsOnNetwork() {
		return networkController.getPeripheralsOnNetwork();
	}

	@Override
	public void invalidateNetwork() {
		networkController.invalidateNetwork();
	}

	@Override
	public boolean transmitPacket(Packet packet) {
		networkController.transmitPacket(this, packet);
		return true;
	}

	/**
	 * If this modem is active and can connect to peripherals
	 */
	protected boolean isPeripheralEnabled() {
		return peripheralEnabled;
	}

	protected void setPeripheralEnabled(boolean peripheralEnabled) {
		if (peripheralEnabled == this.peripheralEnabled) {
			return;
		}

		Map<String, IPeripheral> oldPeripherals = getConnectedPeripherals();

		this.peripheralEnabled = peripheralEnabled;

		Map<String, IPeripheral> newPeripherals = getConnectedPeripherals();

		for (Map.Entry<String, IPeripheral> entry : oldPeripherals.entrySet()) {
			String key = entry.getKey();
			IPeripheral value = entry.getValue();

			if (!newPeripherals.containsKey(key) && value instanceof INetworkedPeripheral) {
				((INetworkedPeripheral) value).detachFromNetwork(this, key);
			}
		}

		for (Map.Entry<String, IPeripheral> entry : newPeripherals.entrySet()) {
			String key = entry.getKey();
			IPeripheral value = entry.getValue();

			if (!oldPeripherals.containsKey(key) && value instanceof INetworkedPeripheral) {
				((INetworkedPeripheral) value).attachToNetwork(this, key);
			}
		}

		if (getAttachedNetwork() != null) getAttachedNetwork().invalidateNetwork();
	}

	@Override
	public String toString() {
		IWorldPosition position = getPosition();
		return "Modem: " + super.toString() + String.format(" (%s, %s, %s)", position.getX(), position.getY(), position.getZ());
	}
}
