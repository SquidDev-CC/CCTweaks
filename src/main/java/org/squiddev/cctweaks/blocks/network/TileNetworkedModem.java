package org.squiddev.cctweaks.blocks.network;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import org.apache.commons.lang3.StringUtils;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNodeHost;
import org.squiddev.cctweaks.api.network.NetworkAPI;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHost;
import org.squiddev.cctweaks.blocks.TileLazyNBT;
import org.squiddev.cctweaks.core.network.modem.BasicModem;
import org.squiddev.cctweaks.core.network.modem.BasicModemPeripheral;
import org.squiddev.cctweaks.core.network.modem.ControllableModemPeripheral;
import org.squiddev.cctweaks.core.network.modem.MultiPeripheralModem;
import org.squiddev.cctweaks.core.utils.Helpers;

import java.util.Arrays;
import java.util.Set;

/**
 * A full block implementation of a modem
 */
public class TileNetworkedModem extends TileLazyNBT implements IPeripheralHost, IWorldNetworkNodeHost, ITickable {
	public final MultiPeripheralModem modem = new MultiPeripheralModem() {
		@Override
		public IWorldPosition getPosition() {
			return TileNetworkedModem.this;
		}

		@Override
		protected BasicModemPeripheral<?> createPeripheral() {
			return new ControllableModemPeripheral<BasicModem>(this);
		}
	};

	@Override
	public void create() {
		NetworkAPI.helpers().scheduleJoin(modem);
		super.create();
	}

	@Override
	public void update() {
		if (worldObj.isRemote) return;

		if (modem.modem.pollChanged()) markForUpdate();
	}

	@Override
	public void onNeighborChanged() {
		if (modem.hasChanged()) {
			modem.getAttachedNetwork().invalidateNode(modem);
			markForUpdate();
		}
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumFacing side) {
		if (player.isSneaking()) return false;
		if (worldObj.isRemote) return true;

		Set<String> names = modem.getPeripheralNames();

		modem.toggleEnabled();

		Set<String> newNames = modem.getPeripheralNames();

		if (!Helpers.equals(names, newNames)) {
			if (names != null && !names.isEmpty()) {
				player.addChatMessage(new ChatComponentTranslation("gui.computercraft:wired_modem.peripheral_disconnected", StringUtils.join(names, ", ")));
			}

			if (newNames != null && !newNames.isEmpty()) {
				player.addChatMessage(new ChatComponentTranslation("gui.computercraft:wired_modem.peripheral_connected", StringUtils.join(newNames, ", ")));
			}

			modem.getAttachedNetwork().invalidateNode(modem);
			markForUpdate();
		}

		return true;
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);

		tag.setBoolean("modem_enabled", modem.isEnabled());
		tag.setIntArray("modem_id", modem.peripherals.ids);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		int[] ids = tag.getIntArray("modem_id");
		if (ids != null && ids.length == 6) System.arraycopy(ids, 0, modem.peripherals.ids, 0, 6);
	}

	@Override
	public void readLazyNBT(NBTTagCompound tag) {
		modem.setPeripheralEnabled(tag.getBoolean("modem_enabled"));
	}

	@Override
	public Iterable<String> getFields() {
		return Arrays.asList("modem_enabled", "modem_id");
	}

	@Override
	protected void readDescription(NBTTagCompound tag) {
		modem.setState(tag.getByte("modem_state"));
		// Force a rerender
		worldObj.markBlockForUpdate(pos);
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
	public void destroy() {
		super.destroy();
		modem.destroy();
	}

	@Override
	public IPeripheral getPeripheral(EnumFacing side) {
		return modem.modem;
	}

	@Override
	public IWorldNetworkNode getNode() {
		return modem;
	}
}
