package org.squiddev.cctweaks.core.network.mock;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.network.Packet;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.util.EnumFacing;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.core.network.AbstractWorldNode;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

/**
 * A network node that counts how many times an event occurred
 */
public class CountingNetworkNode extends AbstractWorldNode implements IPeripheral {
	protected final boolean[] canVisit;
	protected final IWorldPosition position;
	protected int invalidated = 0;
	protected double distance = -1;

	private boolean hasPeripherals = false;

	public CountingNetworkNode(IWorldPosition position, boolean[] canVisit) {
		this.canVisit = canVisit;
		this.position = position;
	}

	protected CountingNetworkNode(IWorldPosition position) {
		this(position, new boolean[]{true, true, true, true, true});
	}

	@Override
	public void networkInvalidated(@Nonnull Map<String, IPeripheral> oldPeripherals, @Nonnull Map<String, IPeripheral> newPeripherals) {
		invalidated++;
	}

	@Override
	public void receivePacket(@Nonnull Packet packet, double distanceTravelled) {
		super.receivePacket(packet, distanceTravelled);
		distance = distanceTravelled;
	}

	@Override
	public boolean canConnect(@Nonnull EnumFacing direction) {
		return direction.getAxis() != EnumFacing.Axis.Y && canVisit[direction.ordinal() - 2];
	}

	@Nonnull
	@Override
	public IWorldPosition getPosition() {
		return position;
	}

	public int invalidated() {
		return invalidated;
	}

	public double distance() {
		return distance;
	}

	@Nonnull
	@Override
	public Map<String, IPeripheral> getConnectedPeripherals() {
		if (hasPeripherals) {
			return Collections.singletonMap("peripheral", (IPeripheral) this);
		} else {
			return Collections.emptyMap();
		}
	}

	public void reset() {
		invalidated = 0;
	}

	public void doInvalidate() {
		hasPeripherals = !hasPeripherals;
		getAttachedNetwork().invalidateNetwork();
	}

	@Override
	public String getType() {
		return "debug";
	}

	@Override
	public String[] getMethodNames() {
		return new String[0];
	}

	@Override
	public Object[] callMethod(IComputerAccess iComputerAccess, ILuaContext iLuaContext, int i, Object[] objects) throws LuaException, InterruptedException {
		return new Object[0];
	}

	@Override
	public void attach(IComputerAccess iComputerAccess) {
	}

	@Override
	public void detach(IComputerAccess iComputerAccess) {
	}

	@Override
	public boolean equals(IPeripheral other) {
		return this == other;
	}
}
