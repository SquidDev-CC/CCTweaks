package org.squiddev.cctweaks.core.network.bridge;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.IPeripheralTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.IDataCard;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.cctweaks.core.network.NetworkedTile;
import org.squiddev.cctweaks.core.network.modem.BasicModem;

import java.util.Map;

/**
 * Bind networks together
 */
public class WirelessBridgeTile extends NetworkedTile implements IPeripheralTile {
	protected final NetworkBinding binding = new NetworkBinding(this);
	protected final BasicModem modem = new BasicModem() {
		@Override
		public IWorldPosition getPosition() {
			return WirelessBridgeTile.this;
		}

		@Override
		public Map<String, IPeripheral> getConnectedPeripherals() {
			return WirelessBridgeTile.this.getConnectedPeripherals();
		}
	};

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		binding.load(tag);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		binding.save(tag);
	}

	@Override
	public void postRemove() {
		binding.remove();
		modem.modem.destroy();
	}

	@Override
	public void setWorldObj(World world) {
		super.setWorldObj(world);
		binding.add();
	}

	@Override
	public boolean onActivated(EntityPlayer player, int side) {
		ItemStack stack = player.getHeldItem();
		return stack != null && stack.getItem() instanceof IDataCard && onActivated(stack, (IDataCard) stack.getItem(), player);
	}

	public boolean onActivated(ItemStack stack, IDataCard card, EntityPlayer player) {
		if (player.isSneaking()) {
			binding.save(stack, card);
			markDirty(); // Mark dirty to ensure the UUID is stored

			card.notifyPlayer(player, IDataCard.Messages.Stored);
			return true;
		} else if (binding.load(stack, card)) {
			card.notifyPlayer(player, IDataCard.Messages.Loaded);
			markDirty();
			return true;
		}

		return false;
	}

	@Override
	public void updateEntity() {
		if (worldObj.isRemote) return;

		modem.processQueue();
		if (!modem.peripheralsKnown) modem.findPeripherals();
	}

	@Override
	public Iterable<IWorldPosition> getExtraNodes() {
		return binding.getPositions();
	}

	@Override
	public void receivePacket(Packet packet, int distanceTravelled) {
		modem.receivePacket(packet, distanceTravelled);
	}

	@Override
	public void networkInvalidated() {
		modem.networkInvalidated();
	}

	@Override
	public Object lock() {
		return modem.lock();
	}

	@Override
	public PeripheralType getPeripheralType() {
		return PeripheralType.WiredModem;
	}

	@Override
	public IPeripheral getPeripheral(int side) {
		return modem.modem;
	}

	@Override
	public String getLabel() {
		return null;
	}

	@Override
	public int getDirection() {
		return 0;
	}

	@Override
	public void setDirection(int direction) {
	}
}
