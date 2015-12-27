package org.squiddev.cctweaks.integration.peripheralspp;

import com.austinv11.peripheralsplusplus.hooks.ComputerCraftHooks;
import com.austinv11.peripheralsplusplus.hooks.ComputerCraftRegistry;
import com.austinv11.peripheralsplusplus.pocket.PocketPeripheralContainer;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.squiddev.cctweaks.core.utils.ComputerAccessor;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

	@Override
	public Map<Integer, IPeripheral> getUpgrades() {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag == null) return Collections.emptyMap();

		int upgrade = (int) tag.getShort("upgrade");

		// 1 is reserved for wireless modems
		if (upgrade == 1) return Collections.emptyMap();

		// If we can't find the computer then abort
		ServerComputer computer = getComputer();
		if (computer == null) return Collections.emptyMap();

		// If we can't find the peripheral then abort
		IPeripheral peripheral = ComputerCraftHooks.cachedPeripherals.get(computer.getID());
		if (peripheral == null) return Collections.emptyMap();

		// Single peripheral, that is fine
		if (!(ComputerCraftRegistry.pocketUpgrades.get(upgrade) instanceof PocketPeripheralContainer) || !tag.hasKey("upgrades")) {
			return Collections.singletonMap(upgrade, peripheral);
		}

		// If we can't find
		Map<Integer, IPeripheral> cached = ComputerCraftHooks.cachedExtraPeripherals.get(computer.getID());
		NBTTagList list = tag.getTagList("upgrades", Constants.NBT.TAG_FLOAT);
		if (cached == null || list == null || list.tagCount() == 0) {
			return Collections.singletonMap(upgrade, peripheral);
		}

		Map<Integer, IPeripheral> peripherals = new HashMap<Integer, IPeripheral>();
		peripherals.put(upgrade, peripheral);

		for (int i = 0; i < list.tagCount(); i++) {
			int id = (int) list.getDouble(i);
			IPeripheral single = cached.get(id);
			if (single != null) peripherals.put(id, single);
		}

		return peripherals;
	}
}
