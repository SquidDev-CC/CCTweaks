package org.squiddev.cctweaks.core.rom;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.items.ComputerItemFactory;
import dan200.computercraft.shared.media.items.ItemDiskLegacy;
import dan200.computercraft.shared.util.ImpostorShapelessRecipe;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.computer.ICustomRomItem;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.registry.Module;
import org.squiddev.cctweaks.items.ItemBase;

import javax.annotation.Nonnull;

/**
 * Create a crafting recipe which can set the ROM of any computer item.
 */
public class CraftingSetRom extends Module implements IRecipe {
	private static final ComputerFamily[] FAMILIES = new ComputerFamily[]{
		ComputerFamily.Normal,
		ComputerFamily.Advanced,
	};

	@Override
	public void init() {
		RecipeSorter.register(CCTweaks.RESOURCE_DOMAIN + ":custom_rom", CraftingSetRom.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
		GameRegistry.addRecipe(this);

		ItemStack disk = new ItemStack(ComputerCraft.Items.diskExpanded);

		// Create some custom recipes. We don't bother with turtles, pocket computers or anything else: we just
		// want to show it is possible, not spam the recipe list.
		for (ComputerFamily family : FAMILIES) {
			ItemStack withoutRom = ComputerItemFactory.create(-1, null, family);

			ItemStack withRom = ComputerItemFactory.create(-1, null, family);
			ItemBase.getTag(withRom).setInteger("rom_id", -1);

			GameRegistry.addRecipe(new ImpostorShapelessRecipe(withRom, new ItemStack[]{withoutRom, disk}));
			GameRegistry.addRecipe(new ImpostorShapelessRecipe(disk, new ItemStack[]{withRom}));
			GameRegistry.addRecipe(new ImpostorShapelessRecipe(disk, new ItemStack[]{withRom, disk}));
		}
	}

	@Override
	public boolean matches(InventoryCrafting inv, World world) {
		return Config.Computer.CustomRom.enabled && Config.Computer.CustomRom.crafting && getCraftingResult(inv) != null;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		ICustomRomItem customRom = null;
		ItemStack romStack = null;

		ItemDiskLegacy disk = null;
		ItemStack diskStack = null;

		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack == null) continue;

			// We don't want to craft multiple items at once.
			if (stack.stackSize > 1) return null;

			Item item = stack.getItem();
			if (item instanceof ItemDiskLegacy) {
				if (disk != null) return null;
				disk = (ItemDiskLegacy) item;
				diskStack = stack;

				// Ensure this disk exists
				if (disk.getDiskID(stack) < 0) return null;
			} else if (item instanceof ICustomRomItem) {
				if (customRom != null) return null;
				customRom = (ICustomRomItem) item;
				romStack = stack;
			} else {
				return null;
			}
		}

		if (customRom == null) return null;

		if (customRom.hasCustomRom(romStack)) {
			// Crafting with a disk will result in a disk with the old ROM
			return newDisk(customRom.getCustomRom(romStack));
		} else {
			if (diskStack == null) return null;

			// Crafting without a disk will remove the ROM
			ItemStack result = romStack.copy();
			customRom.setCustomRom(result, disk.getDiskID(diskStack));
			return result;
		}
	}

	@Override
	public int getRecipeSize() {
		return 2;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return null;
	}

	@Nonnull
	@Override
	public ItemStack[] getRemainingItems(InventoryCrafting inv) {
		ItemStack[] out = new ItemStack[inv.getSizeInventory()];

		int romSlot = -1;
		ICustomRomItem customRom = null;
		ItemStack romStack = null;

		ItemDiskLegacy disk = null;
		ItemStack diskStack = null;

		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack == null) continue;

			Item item = stack.getItem();
			if (item instanceof ItemDiskLegacy) {
				disk = (ItemDiskLegacy) item;
				diskStack = stack;

				// Ensure this disk exists
			} else if (item instanceof ICustomRomItem) {
				romSlot = i;
				customRom = (ICustomRomItem) item;
				romStack = stack;
			}
		}

		// Should never be hit but helps static analysis
		if (customRom == null) return out;

		if (customRom.hasCustomRom(romStack)) {
			if (diskStack != null) {
				// Crafting with a disk will set the current computer's ROM
				ItemStack result = out[romSlot] = romStack.copy();
				customRom.setCustomRom(result, disk.getDiskID(diskStack));
			} else {
				// Crafting with a disk will clear the current computer's ROM
				ItemStack result = out[romSlot] = romStack.copy();
				customRom.clearCustomRom(result);
			}
		}

		return out;
	}

	private static ItemStack newDisk(int id) {
		ItemStack result = new ItemStack(ComputerCraft.Items.diskExpanded);
		ItemBase.getTag(result).setInteger("diskID", id);
		return result;
	}
}
