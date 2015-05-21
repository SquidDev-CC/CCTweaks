package org.squiddev.cctweaks.blocks.network;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.cctweaks.blocks.TileBase;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Abstract TE to prevent having to override every item
 */
public abstract class TileNetworked extends TileBase implements IWorldNetworkNode {
	private INetworkController networkController;

	@Override
	public boolean canConnect(ForgeDirection from) {
		return true;
	}

	@Override
	public Map<String, IPeripheral> getConnectedPeripherals() {
		return null;
	}

	@Override
	public void receivePacket(Packet packet, double distanceTravelled) {
	}

	@Override
	public void networkInvalidated(Map<String, IPeripheral> oldPeripherals) {
	}

	@Override
	public Set<INetworkNode> getConnectedNodes() {
		return Collections.emptySet();
	}

	@Override
	public void detachFromNetwork() {
		networkController = null;
	}

	@Override
	public void attachToNetwork(INetworkController networkController) {
		this.networkController = networkController;
	}

	@Override
	public INetworkController getAttachedNetwork() {
		return networkController;
	}

	@Override
	public IWorldPosition getPosition() {
		return this;
	}

	public boolean onActivated(EntityPlayer player, int side) {
		return false;
	}

	public void onNeighborChanged() {
	}
}
