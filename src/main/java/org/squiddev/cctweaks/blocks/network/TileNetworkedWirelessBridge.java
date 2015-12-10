package org.squiddev.cctweaks.blocks.network;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import org.squiddev.cctweaks.api.IDataCard;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNodeHost;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHost;
import org.squiddev.cctweaks.blocks.TileLazyNBT;
import org.squiddev.cctweaks.core.network.NetworkHelpers;
import org.squiddev.cctweaks.core.network.bridge.NetworkBinding;
import org.squiddev.cctweaks.core.network.bridge.NetworkBindingWithModem;

import java.util.Arrays;
import java.util.UUID;

/**
 * Bind networks together
 */
public class TileNetworkedWirelessBridge extends TileLazyNBT implements IPeripheralHost, IWorldNetworkNodeHost {
	protected final NetworkBindingWithModem binding = new NetworkBindingWithModem(this);

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		binding.save(tag);
	}

	@Override
	public void readLazyNBT(NBTTagCompound tag) {
		binding.load(tag);
	}

	@Override
	public Iterable<String> getFields() {
		return Arrays.asList(NetworkBinding.LSB, NetworkBinding.MSB);
	}

	@Override
	public void create() {
		super.create();
		NetworkHelpers.scheduleConnect(binding);
	}

	@Override
	public void destroy() {
		super.destroy();
		binding.destroy();
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumFacing side) {
		ItemStack stack = player.getHeldItem();
		return stack != null && stack.getItem() instanceof IDataCard && onActivated(stack, (IDataCard) stack.getItem(), player);
	}

	public boolean onActivated(ItemStack stack, IDataCard card, EntityPlayer player) {
		if (worldObj.isRemote) return true;

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

	public UUID getBindingId() {
		return binding.getId();
	}

	public void setBindingId(UUID id) {
		binding.setId(id);
		markDirty();
	}

	@Override
	public IPeripheral getPeripheral(EnumFacing side) {
		return binding.getModem().modem;
	}

	@Override
	public IWorldNetworkNode getNode() {
		return binding;
	}
}
