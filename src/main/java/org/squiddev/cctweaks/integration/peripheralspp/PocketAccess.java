package org.squiddev.cctweaks.integration.peripheralspp;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.cctweaks.core.utils.ComputerAccessor;
import org.squiddev.cctweaks.core.utils.DebugLogger;

/**
 * Concrete implementation of https://github.com/austinv11/PeripheralsPlusPlus/issues/97
 */
public class PocketAccess implements IPocketAccess {
	protected Entity entity;
	protected ItemStack stack;

	public PocketAccess(Entity entity, ItemStack stack) {
		this.entity = entity;
		this.stack = stack;
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
		} catch (ReflectiveOperationException e) {
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
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}

		// TODO: Localise to child tag
		return stack.getTagCompound();
	}

	@Override
	public void updateUpgradeNBTData() {
		InventoryPlayer inventory = entity instanceof EntityPlayer ? ((EntityPlayer) entity).inventory : null;
		if (inventory != null) {
			inventory.markDirty();
		}
	}
}
