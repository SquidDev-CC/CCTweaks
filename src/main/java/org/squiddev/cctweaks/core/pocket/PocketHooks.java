package org.squiddev.cctweaks.core.pocket;

import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.pocket.IPocketUpgrade;

import java.util.HashMap;
import java.util.Map;

/**
 * Hooks for pocket upgrades
 */
public class PocketHooks {
	private static final Map<Integer, PocketAccess> upgradeInstance = new HashMap<Integer, PocketAccess>();

	private static PocketAccess getAccess(ItemStack stack, ServerComputer computer) {
		if (!stack.hasTagCompound()) return null;

		NBTTagCompound tag = stack.getTagCompound();
		if (!tag.hasKey("upgrade")) return null;

		short id = tag.getShort("upgrade");
		if (id == 0 || id == 1) return null;

		return upgradeInstance.get(computer.getInstanceID());
	}

	public static void update(Entity entity, ItemStack stack, ServerComputer computer) {
		PocketAccess access = getAccess(stack, computer);
		if (access == null) return;

		access.stack = stack;
		access.entity = entity;
		access.upgrade.update(access, access.peripheral);
	}

	public static boolean rightClick(World world, Entity entity, ItemStack stack, ServerComputer computer) {
		PocketAccess access = getAccess(stack, computer);
		if (access == null) return false;

		access.stack = stack;
		access.entity = entity;
		return access.upgrade.onRightClick(world, access, access.peripheral);
	}

	public static void create(IInventory inventory, ItemStack stack, ServerComputer computer) {
		IPocketUpgrade upgrade = PocketRegistry.instance.getUpgrade(stack, null);
		if (upgrade == null) return;

		Entity entity = inventory instanceof InventoryPlayer ? ((InventoryPlayer) inventory).player : null;
		PocketAccess access = new PocketAccess(upgrade, entity, stack);
		computer.setPeripheral(2, access.peripheral);
		upgradeInstance.put(computer.getInstanceID(), access);
	}

	public static void destroy(ServerComputer computer) {
		upgradeInstance.remove(computer.getInstanceID());
	}
}
