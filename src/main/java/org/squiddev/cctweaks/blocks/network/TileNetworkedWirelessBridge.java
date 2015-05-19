package org.squiddev.cctweaks.blocks.network;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
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
	protected final NetworkBinding binding = new NetworkBinding(this);
	protected final BasicModem modem = new BasicModem() {
		@Override
		public IWorldPosition getPosition() {
			return TileNetworkedWirelessBridge.this;
		}

		@Override
		public Map<String, IPeripheral> getConnectedPeripherals() {
			return Collections.emptyMap();
		}

		@Override
		public Set<INetworkNode> getConnectedNodes() {
			// TODO: Fix me! Bindings should return nodes, not positions.
			return binding.getPositions();
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
		modem.destroy();
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
