package org.squiddev.cctweaks.core.pocket;

import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.WeakHashMap;

/**
 * Hooks for pocket upgrades
 */
public class PocketHooks {
	private static final WeakHashMap<ServerComputer, PocketAccess> upgradeInstance = new WeakHashMap<ServerComputer, PocketAccess>();

	public static PocketAccess getAccess(ServerComputer computer) {
		return upgradeInstance.get(computer);
	}

	public static void update(Entity entity, ItemStack stack, ServerComputer computer) {
		PocketAccess access = getAccess(computer);
		if (access == null) return;

		access.stack = stack;
		access.entity = entity;
		access.update();
	}

	public static boolean rightClick(World world, Entity entity, ItemStack stack, ServerComputer computer) {
		PocketAccess access = getAccess(computer);
		if (access == null) return false;

		access.stack = stack;
		access.entity = entity;
		return access.rightClick(world);
	}

	public static void create(IInventory inventory, ItemStack stack, ServerComputer computer) {
		Entity entity = inventory instanceof InventoryPlayer ? ((InventoryPlayer) inventory).player : null;

		PocketAccess access = new PocketAccess(entity, stack);
		upgradeInstance.put(computer, access);

		access.setUpgrade(PocketRegistry.instance.getUpgrade(stack, inventory), computer);
	}

	public static void destroy(ServerComputer computer) {
		upgradeInstance.remove(computer);
	}

	public static boolean hasPocketUpgrade(ItemStack stack) {
		return stack.hasTagCompound() && stack.getTagCompound().getInteger("upgrade") != 0;
	}
}
