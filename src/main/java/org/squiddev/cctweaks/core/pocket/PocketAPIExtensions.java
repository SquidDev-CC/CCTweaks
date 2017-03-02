package org.squiddev.cctweaks.core.pocket;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.pocket.apis.PocketAPI;
import dan200.computercraft.shared.util.InventoryUtil;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.squiddev.cctweaks.api.pocket.IPocketUpgrade;
import org.squiddev.cctweaks.pocket.PocketModem;

public class PocketAPIExtensions extends PocketAPI {
	private final ServerComputer computer;

	public PocketAPIExtensions(ServerComputer computer) {
		this.computer = computer;
	}

	@Override
	public void shutdown() {
		PocketHooks.destroy(computer);
	}

	@Override
	public String[] getMethodNames() {
		return new String[]{"equip", "unequip"};
	}

	@Override
	public Object[] callMethod(ILuaContext context, int method, Object[] arguments) throws LuaException {
		switch (method) {
			case 0: {
				PocketAccess access = PocketHooks.getAccess(computer);
				if (access == null || !(access.getEntity() instanceof EntityPlayer)) {
					throw new LuaException("Cannot find player");
				}

				ItemStack pocketStack = access.stack;
				EntityPlayer player = (EntityPlayer) access.getEntity();
				InventoryPlayer inventory = player.inventory;

				IPocketUpgrade previousUpgrade = access.getUpgrade();
				IPocketUpgrade newUpgrade = null;

				int size = inventory.getSizeInventory(), held = inventory.currentItem;
				for (int i = 0; i < size; i++) {
					ItemStack invStack = inventory.getStackInSlot((i + held) % size);
					if (!invStack.isEmpty()) {
						if (InventoryUtil.areItemsStackable(invStack, PocketModem.INSTANCE.getCraftingItem())) {
							newUpgrade = PocketModem.INSTANCE;
						} else {
							newUpgrade = PocketRegistry.instance.getFromItemStack(invStack);
						}
						if (newUpgrade != null && newUpgrade != previousUpgrade) {
							// Consume an item from this stack and exit the loop
							invStack = invStack.copy();
							invStack.grow(-1);
							inventory.setInventorySlotContents((i + held) % size, invStack.isEmpty() ? ItemStack.EMPTY : invStack);

							break;
						}
					}
				}

				if (newUpgrade == null) throw new LuaException("Cannot find a valid upgrade");

				// Remove the current upgrade
				if (previousUpgrade != null) {
					ItemStack stack = previousUpgrade.getCraftingItem();
					if (stack != null && !stack.isEmpty()) {
						stack = InventoryUtil.storeItems(stack, inventory, 0, 36, inventory.currentItem);
						if (stack != null && !stack.isEmpty()) {
							WorldUtil.dropItemStack(stack, player.getEntityWorld(), player.posX, player.posY, player.posZ);
						}
					}
				}

				// Set the new upgrade
				PocketRegistry.instance.setToItemStack(pocketStack, newUpgrade);
				access.setUpgrade(newUpgrade, computer);

				return null;
			}

			case 1:
				PocketAccess access = PocketHooks.getAccess(computer);
				if (access == null || !(access.getEntity() instanceof EntityPlayer)) {
					throw new LuaException("Cannot find player");
				}

				ItemStack pocketStack = access.stack;
				EntityPlayer player = (EntityPlayer) access.getEntity();
				InventoryPlayer inventory = player.inventory;

				IPocketUpgrade previousUpgrade = access.getUpgrade();

				if (previousUpgrade == null) throw new LuaException("Nothing to unequip");

				PocketRegistry.instance.setToItemStack(pocketStack, null);
				access.setUpgrade(null, computer);

				ItemStack stack = previousUpgrade.getCraftingItem();
				if (stack != null && !stack.isEmpty()) {
					stack = InventoryUtil.storeItems(stack, inventory, 0, 36, inventory.currentItem);
					if (stack != null && !stack.isEmpty()) {
						WorldUtil.dropItemStack(stack, player.getEntityWorld(), player.posX, player.posY, player.posZ);
					}
				}

				return null;
			default:
				return null;
		}
	}
}
