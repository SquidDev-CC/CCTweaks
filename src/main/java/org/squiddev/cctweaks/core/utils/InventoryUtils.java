/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Open Mods
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.squiddev.cctweaks.core.utils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Various inventory code taken from OpenModsLib because I am lazy
 */
public class InventoryUtils {
	public static boolean areItemAndTagEqual(final ItemStack stackA, ItemStack stackB) {
		return stackA.isItemEqual(stackB) && ItemStack.areItemStackTagsEqual(stackA, stackB);
	}

	public static boolean areMergeCandidates(ItemStack source, ItemStack target) {
		return areItemAndTagEqual(source, target) && target.stackSize < target.getMaxStackSize();
	}

	public static ItemStack copyAndChange(ItemStack stack, int newSize) {
		ItemStack copy = stack.copy();
		copy.stackSize = newSize;
		return copy;
	}

	public static void removeFromSlot(IInventory inventory, int slot, int amount) {
		ItemStack sourceStack = inventory.getStackInSlot(slot);
		sourceStack.stackSize -= amount;
		if (sourceStack.stackSize == 0) {
			inventory.setInventorySlotContents(slot, null);
		} else {
			// Paranoia? Always!
			inventory.setInventorySlotContents(slot, sourceStack);
		}
	}

	private static IInventory doubleChestFix(TileEntity te) {
		final World world = te.getWorld();
		final BlockPos position = te.getPos();

		final Block block = te.getBlockType();

		if (block == Blocks.chest || block == Blocks.trapped_chest) {
			for (EnumFacing facing : EnumFacing.VALUES) {
				BlockPos offset = position.offset(facing);
				if (world.getBlockState(offset).getBlock() == block) {
					return new InventoryLargeChest("Large chest", (ILockableContainer) world.getTileEntity(offset), (ILockableContainer) te);
				}
			}
		}

		return (te instanceof IInventory) ? (IInventory) te : null;
	}

	public static IInventory getInventory(IInventory inventory) {
		if (inventory instanceof TileEntityChest) return doubleChestFix((TileEntity) inventory);
		return inventory;
	}

	public static boolean insertItemIntoInventory(IInventory inventory, ItemStack stack, EnumFacing side, int intoSlot) {
		if (stack == null) return false;
		final Set<Integer> attemptSlots = Sets.newTreeSet();

		// if it's a sided inventory, get all the accessible slots
		final boolean isSidedInventory = side != null && inventory instanceof ISidedInventory;

		if (isSidedInventory) {
			int[] accessibleSlots = ((ISidedInventory) inventory).getSlotsForFace(side);
			for (int slot : accessibleSlots) {
				attemptSlots.add(slot);
			}
		} else {
			// if it's just a standard inventory, get all slots
			for (int a = 0; a < inventory.getSizeInventory(); a++) {
				attemptSlots.add(a);
			}
		}

		if (intoSlot > -1) attemptSlots.retainAll(ImmutableSet.of(intoSlot));

		if (attemptSlots.isEmpty()) return false;

		boolean result = false;
		for (Integer slot : attemptSlots) {
			if (stack.stackSize <= 0) break;
			if (isSidedInventory && !((ISidedInventory) inventory).canInsertItem(slot, stack, side)) continue;
			result |= tryInsertStack(inventory, slot, stack);
		}

		return result;
	}

	/**
	 * Move an item from the fromInventory, into the target. The target can be
	 * an inventory or pipe.
	 * Double checks are automagically wrapped. If you're not bothered what slot
	 * you insert into, pass -1 for intoSlot. If you're passing false for
	 * doMove, it'll create a dummy inventory and its calculations on that
	 * instead
	 *
	 * @param fromInventory The inventory the item is coming from
	 * @param fromSlot      The slot the item is coming from
	 * @param fromDirection The direction to remove from. Pass {@code null} if not applicable.
	 * @param toInventory   The inventory you want the item to be put into
	 * @param toSlot        The target slot. Pass -1 for any slot
	 * @param toDirection   The direction to insert into. Pass {@code null} if not applicable
	 * @param maxAmount     The maximum amount you wish to pass
	 * @return The amount of items moved
	 */
	public static int moveItemInto(
		IInventory fromInventory, int fromSlot, EnumFacing fromDirection,
		IInventory toInventory, int toSlot, EnumFacing toDirection,
		int maxAmount
	) {
		ItemStack sourceStack = fromInventory.getStackInSlot(fromSlot);
		if (sourceStack == null || maxAmount <= 0) return 0;

		// Check if this is an ISidedInventory.
		final boolean isSidedInventory = fromInventory instanceof ISidedInventory && fromDirection != null;
		if (isSidedInventory && !((ISidedInventory) fromInventory).canExtractItem(fromSlot, sourceStack, fromDirection)) {
			return 0;
		}

		final int amountToMove = Math.min(sourceStack.stackSize, maxAmount);
		ItemStack insertedStack = copyAndChange(sourceStack, amountToMove);

		// try insert the item into the target inventory. This will reduce the stackSize of our stack
		insertItemIntoInventory(toInventory, insertedStack, toDirection, toSlot);
		int inserted = amountToMove - insertedStack.stackSize;

		removeFromSlot(fromInventory, fromSlot, inserted);

		return inserted;
	}

	/**
	 * Try to merge the supplied stack into the supplied slot in the target inventory
	 *
	 * @param targetInventory Although it doesn't return anything, it'll REDUCE the stack
	 *                        size of the stack that you pass in
	 * @param slot            The slot to insert into
	 * @param stack           The stack to insert
	 */
	public static boolean tryInsertStack(IInventory targetInventory, int slot, ItemStack stack) {
		if (targetInventory.isItemValidForSlot(slot, stack)) {
			ItemStack targetStack = targetInventory.getStackInSlot(slot);
			if (targetStack == null) {
				int limit = targetInventory.getInventoryStackLimit();
				if (limit < stack.stackSize) {
					targetInventory.setInventorySlotContents(slot, stack.splitStack(limit));
				} else {
					targetInventory.setInventorySlotContents(slot, stack.copy());
					stack.stackSize = 0;
				}
				return true;
			} else {
				if (targetInventory.isItemValidForSlot(slot, stack) &&
					areMergeCandidates(stack, targetStack)) {
					int space = targetStack.getMaxStackSize() - targetStack.stackSize;
					int mergeAmount = Math.min(space, stack.stackSize);
					ItemStack copy = targetStack.copy();
					copy.stackSize += mergeAmount;
					targetInventory.setInventorySlotContents(slot, copy);
					stack.stackSize -= mergeAmount;
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Extract n items
	 *
	 * @param inventory The inventory to extract from
	 * @param item      The item to extract
	 * @param count     The number to extract
	 * @return Number of items remaining
	 */
	public static int extractItems(IInventory inventory, Item item, final int count) {
		List<Integer> items = new ArrayList<Integer>();

		int remaining = count;

		int invSize = inventory.getSizeInventory();
		for (int i = 0; i < invSize; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack != null && stack.getItem() == item && stack.stackSize > 0) {
				items.add(i);

				int size = stack.stackSize;
				if (size > remaining) {
					remaining = 0;
				} else {
					remaining -= stack.stackSize;
				}

				if (remaining <= 0) break;
			}
		}

		if (remaining > 0) return remaining;

		remaining = count;
		for (int i : items) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack.stackSize <= remaining) {
				remaining -= stack.stackSize;
				inventory.setInventorySlotContents(i, null);
			} else {
				stack.stackSize -= remaining;
				inventory.setInventorySlotContents(i, stack);
				remaining = 0;
			}
		}

		inventory.markDirty();

		return 0;
	}

}
