package org.squiddev.cctweaks.blocks.network;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import org.apache.commons.lang3.StringUtils;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNodeHost;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHost;
import org.squiddev.cctweaks.blocks.TileLazyNBT;
import org.squiddev.cctweaks.core.network.modem.MultiPeripheralModem;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

/**
 * A full block implementation of a modem
 */
public class TileNetworkedModem extends TileLazyNBT implements IPeripheralHost, IWorldNetworkNodeHost {
	public final MultiPeripheralModem modem = new MultiPeripheralModem() {
		@Override
		public IWorldPosition getPosition() {
			return TileNetworkedModem.this;
		}
	};

	@Override
	public void updateEntity() {
		if (worldObj.isRemote) return;

		if (modem.modem.pollChanged()) markForUpdate();

		modem.processQueue();
	}

	@Override
	public void onNeighborChanged() {
		if (modem.hasChanged()) {
			modem.getAttachedNetwork().invalidateNetwork();
			markForUpdate();
		}
	}

	@Override
	public boolean onActivated(EntityPlayer player, int side) {
		if (player.isSneaking()) return false;
		if (worldObj.isRemote) return true;

		Set<String> names = modem.getPeripheralNames();

		modem.toggleEnabled();

		Set<String> newNames = modem.getPeripheralNames();

		if (!Objects.equals(names, newNames)) {
			if (names != null) {
				player.addChatMessage(new ChatComponentTranslation("gui.computercraft:wired_modem.peripheral_disconnected", StringUtils.join(names, ", ")));
			}

			if (newNames != null) {
				player.addChatMessage(new ChatComponentTranslation("gui.computercraft:wired_modem.peripheral_connected", StringUtils.join(newNames, ", ")));
			}

			modem.getAttachedNetwork().invalidateNetwork();
			markForUpdate();
		}

		return true;
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);

		tag.setBoolean("modem_enabled", modem.isEnabled());
		tag.setIntArray("modem_id", modem.ids);
	}

	@Override
	public void readLazyNBT(NBTTagCompound tag) {
		modem.setState(tag.getBoolean("modem_enabled") ? MultiPeripheralModem.MODEM_PERIPHERAL : 0);

		int[] ids = tag.getIntArray("modem_id");
		if (ids != null && ids.length == 6) System.arraycopy(ids, 0, modem.ids, 0, 6);
	}

	@Override
	public Iterable<String> getFields() {
		return Arrays.asList("modem_enabled", "modem_id");
	}

	@Override
	public void destroy() {
		super.destroy();
		modem.destroy();
	}

	@Override
	protected void readDescription(NBTTagCompound tag) {
		modem.setState(tag.getByte("modem_state"));
		// Force a rerender
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	protected boolean writeDescription(NBTTagCompound tag) {
		tag.setByte("modem_state", modem.state);
		return true;
	}

	@Override
	public void markForUpdate() {
		if (!worldObj.isRemote) modem.refreshState();
		super.markForUpdate();
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
