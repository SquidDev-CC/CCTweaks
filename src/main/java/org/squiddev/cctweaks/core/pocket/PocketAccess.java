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
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.pocket.IPocketAccess;
import org.squiddev.cctweaks.api.pocket.IPocketUpgrade;
import org.squiddev.cctweaks.core.utils.ComputerAccessor;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;

public final class PocketAccess implements IPocketAccess {
	private IPocketUpgrade upgrade;
	private IPeripheral peripheral;
	Entity entity;
	ItemStack stack;

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
			Object computer = ComputerAccessor.pocketServerComputer.invoke(ComputerCraft.Items.pocketComputer, entity.getEntityWorld(), inventory, stack);
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

	@Nonnull
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
	public void invalidatePeripheral() {
		ServerComputer computer = getComputer();
		if (computer == null) return;

		invalidatePeripheral(computer);
	}

	@Nonnull
	@Override
	public Map<ResourceLocation, IPeripheral> getUpgrades() {
		if (upgrade == null) {
			return Collections.emptyMap();
		} else {
			return Collections.singletonMap(upgrade.getUpgradeID(), peripheral);
		}
	}

	public IPocketUpgrade getUpgrade() {
		return upgrade;
	}

	private void invalidatePeripheral(ServerComputer computer) {
		peripheral = upgrade == null ? null : upgrade.createPeripheral(this);
		computer.setPeripheral(2, peripheral);
	}

	synchronized void setUpgrade(IPocketUpgrade upgrade, ServerComputer computer) {
		// Clear the old upgrade NBT
		if (stack.hasTagCompound()) {
			NBTTagCompound tag = stack.getTagCompound();
			if (tag.hasKey("upgrade_info", 10)) {
				tag.removeTag("upgrade_info");
				updateUpgradeNBTData();
			}
		}

		this.upgrade = upgrade;
		invalidatePeripheral(computer);
	}

	synchronized void update() {
		if (upgrade != null) upgrade.update(this, peripheral);
	}

	synchronized boolean rightClick(World world) {
		return upgrade != null && upgrade.onRightClick(world, this, peripheral);
	}
}
