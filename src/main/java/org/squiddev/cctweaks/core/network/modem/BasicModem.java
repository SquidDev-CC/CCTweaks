package org.squiddev.cctweaks.core.network.modem;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import dan200.computercraft.api.network.IPacketNetwork;
import dan200.computercraft.api.network.IPacketReceiver;
import dan200.computercraft.api.network.Packet;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.core.network.AbstractNode;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Basic wired modem that handles peripherals and computer interaction
 */
public abstract class BasicModem extends AbstractNode implements IPacketNetwork, IWorldNetworkNode {
	public static final byte MODEM_ON = 1;
	public static final byte MODEM_PERIPHERAL = 2;

	/**
	 * Set of receivers to use
	 */
	private final Set<IPacketReceiver> receivers = Sets.newHashSet();

	/**
	 * List of wrappers for peripherals on the remote network
	 */
	private final Map<String, PeripheralAccess> peripheralWrappersByName = new HashMap<String, PeripheralAccess>();

	private boolean peripheralEnabled = false;

	/**
	 * The modem to use
	 */
	public final BasicModemPeripheral<?> modem = createPeripheral();

	/**
	 * The state of the modem
	 */
	public byte state;

	@Override
	public void addReceiver(@Nonnull IPacketReceiver receiver) {
		synchronized (receivers) {
			receivers.add(receiver);
		}
	}

	@Override
	public void removeReceiver(@Nonnull IPacketReceiver receiver) {
		synchronized (receivers) {
			receivers.remove(receiver);
		}
	}

	@Override
	public void transmitSameDimension(@Nonnull Packet packet, double v) {
		INetworkController controller = getAttachedNetwork();
		if (controller != null) {
			controller.transmitPacket(this, packet);
		}
	}

	@Override
	public void transmitInterdimensional(@Nonnull dan200.computercraft.api.network.Packet packet) {
		INetworkController controller = getAttachedNetwork();
		if (controller != null) {
			controller.transmitPacket(this, packet);
		}
	}

	/**
	 * Attach a peripheral to the modem
	 *
	 * @param name       The peripheral name
	 * @param peripheral The peripheral to attach
	 */
	public void attachPeripheral(String name, IPeripheral peripheral) {
		synchronized (peripheralWrappersByName) {
			attachPeripheralUnsync(name, peripheral);
		}
	}

	private void attachPeripheralUnsync(String name, IPeripheral peripheral) {
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
		synchronized (peripheralWrappersByName) {
			detachPeripheralUnsync(name);
		}
	}

	/**
	 * Detach all peripherals
	 */
	public void detachPeripherals() {
		synchronized (peripheralWrappersByName) {
			List<String> names = Lists.newArrayList(peripheralWrappersByName.keySet());
			for (String name : names) {
				detachPeripheralUnsync(name);
			}
		}
	}

	private void detachPeripheralUnsync(String name) {
		PeripheralAccess wrapper = peripheralWrappersByName.remove(name);
		if (wrapper != null) wrapper.detach();
	}

	public PeripheralAccess getPeripheral(String name) {
		synchronized (peripheralWrappersByName) {
			return peripheralWrappersByName.get(name);
		}
	}

	@Override
	public void receivePacket(@Nonnull Packet packet, double distanceTravelled) {
		synchronized (receivers) {
			for (IPacketReceiver receiver : receivers) {
				receiver.receiveSameDimension(packet, distanceTravelled);
			}
		}
	}

	/**
	 * Set the state of the modem.
	 * This is purely cosmetic, use {@link #setPeripheralEnabled(boolean)} to set peripheral state.
	 *
	 * @param state The flags to set the state with
	 * @see #MODEM_ON
	 * @see #MODEM_PERIPHERAL
	 */
	public void setState(byte state) {
		this.state = state;
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
	protected BasicModemPeripheral<?> createPeripheral() {
		return new BasicModemPeripheral<BasicModem>(this);
	}

	@Override
	public boolean isWireless() {
		return false;
	}

	@Override
	public boolean canConnect(@Nonnull EnumFacing from) {
		return true;
	}

	@Override
	public void networkInvalidated(@Nonnull Map<String, IPeripheral> oldPeripherals, @Nonnull Map<String, IPeripheral> newPeripherals) {
		synchronized (peripheralWrappersByName) {
			for (String wrapper : oldPeripherals.keySet()) {
				detachPeripheralUnsync(wrapper);
			}

			if (modem != null && modem.getComputer() != null) {
				for (String name : newPeripherals.keySet()) {
					if (!peripheralWrappersByName.containsKey(name)) {
						attachPeripheralUnsync(name, newPeripherals.get(name));
					}
				}
			}
		}
	}

	public void destroy() {
		INetworkController controller = getAttachedNetwork();
		if (controller != null) controller.removeNode(this);
		modem.destroy();
	}

	/**
	 * If this modem is active and can connect to peripherals
	 */
	protected boolean isPeripheralEnabled() {
		return peripheralEnabled;
	}

	public void setPeripheralEnabled(boolean peripheralEnabled) {
		if (peripheralEnabled == this.peripheralEnabled) return;

		this.peripheralEnabled = peripheralEnabled;

		INetworkController controller = getAttachedNetwork();
		if (controller != null) controller.invalidateNode(this);
	}

	@Override
	public String toString() {
		BlockPos position = getPosition().getPosition();
		return "Modem: " + super.toString() + String.format(" (%s, %s, %s)", position.getX(), position.getY(), position.getZ());
	}
}
