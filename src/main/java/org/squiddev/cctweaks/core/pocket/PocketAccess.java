package org.squiddev.cctweaks.core.pocket;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.squiddev.cctweaks.api.pocket.IPocketAccess;
import org.squiddev.cctweaks.api.pocket.IPocketUpgrade;
import org.squiddev.cctweaks.core.utils.ComputerAccessor;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Set;

public final class PocketAccess implements IPocketAccess {
	protected IPocketUpgrade upgrade;
	protected Entity entity;
	protected ItemStack stack;
	protected IPeripheral peripheral;

	public PocketAccess(IPocketUpgrade upgrade, Entity entity, ItemStack stack) {
		this.upgrade = upgrade;
		this.entity = entity;
		this.stack = stack;

		this.peripheral = upgrade.createPeripheral(this);
	}

	@Override
	public Entity getEntity() {
		return entity;
	}

	private ServerComputer getComputer() {
		Entity entity = getEntity();
		if (entity == null) return null;

		InventoryPlayer inventory = entity instanceof EntityPlayer ? ((EntityPlayer) entity).inventory : null;
		try {
			Object computer = ComputerAccessor.pocketServerComputer.invoke(ComputerCraft.Items.pocketComputer, entity.worldObj, inventory, stack);
			return computer == null ? null : (ServerComputer) computer;
		} catch (InvocationTargetException e) {
			DebugLogger.error("Cannot find computer", e);
			return null;
		} catch (IllegalAccessException e) {
			DebugLogger.error("Cannot find computer", e);
			return null;
		}
	}

	@Override
	public boolean getModemLight() {
		ServerComputer computer = getComputer();
		return computer != null && computer.getUserData().getBoolean("modemLight");
	}

	@Override
	public void setModemLight(boolean value) {
		ServerComputer computer = getComputer();
		if (computer != null) {
			NBTTagCompound userData = computer.getUserData();
			if (userData.getBoolean("modemLight") != value) {
				computer.getUserData().setBoolean("modemLight", value);
				computer.updateUserData();
			}
		}
	}

	@Override
	public NBTTagCompound getUpgradeNBTData() {
		NBTTagCompound tag;
		if (stack.hasTagCompound()) {
			tag = stack.getTagCompound();
		} else {
			tag = new NBTTagCompound();
			stack.setTagCompound(tag);
		}

		if (tag.hasKey("upgrade_info", 10)) {
			return tag.getCompoundTag("upgrade_info");
		} else {
			NBTTagCompound sub = new NBTTagCompound();

			tag.setTag("upgrade_info", sub);
			updateUpgradeNBTData();

			return sub;
		}
	}

	@Override
	public void updateUpgradeNBTData() {
		InventoryPlayer inventory = entity instanceof EntityPlayer ? ((EntityPlayer) entity).inventory : null;
		if (inventory != null) {
			inventory.markDirty();
		}
	}

	@Override
	public Set<ResourceLocation> getUpgrades() {
		return Collections.singleton(upgrade.getUpgradeID());
	}
}
