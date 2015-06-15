package org.squiddev.cctweaks.blocks.network;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.cctweaks.api.IDataCard;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNodeHost;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHost;
import org.squiddev.cctweaks.blocks.TileBase;
import org.squiddev.cctweaks.core.network.bridge.NetworkBinding;
import org.squiddev.cctweaks.core.network.modem.BasicModem;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Bind networks together
 */
public class TileNetworkedWirelessBridge extends TileBase implements IPeripheralHost, IWorldNetworkNodeHost {
	protected final BasicModem modem = new BasicModem() {
		@Override
		public IWorldPosition getPosition() {
			return TileNetworkedWirelessBridge.this;
		}

		@Override
		public Map<String, IPeripheral> getConnectedPeripherals() {
			return Collections.emptyMap();
		}

		// TODO: Get this working
		public Set<INetworkNode> getConnectedNodes() {
			return Collections.<INetworkNode>unmodifiableSet(binding.getNodes());
		}
	};
	protected final NetworkBinding binding = new NetworkBinding(modem);

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
	public void create() {
		super.create();
		binding.add();
	}

	@Override
	public void destroy() {
		super.destroy();
		binding.remove();
		modem.destroy();
	}

	@Override
	public boolean onActivated(EntityPlayer player, int side) {
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

	@Override
	public void updateEntity() {
		if (worldObj.isRemote) return;

		modem.processQueue();
	}

	@Override
	public IPeripheral getPeripheral(int side) {
		return modem.modem;
	}

	@Override
	public IWorldNetworkNode getNode() {
		return modem;
	}
}
