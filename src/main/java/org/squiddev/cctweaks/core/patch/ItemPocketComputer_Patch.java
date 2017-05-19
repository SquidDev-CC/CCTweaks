package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import org.squiddev.cctweaks.api.IComputerItemFactory;
import org.squiddev.cctweaks.api.computer.ICustomRomItem;
import org.squiddev.patcher.visitors.MergeVisitor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Patches for pocket computers.
 *
 * Also converts CCTweaks upgrades to the new system.
 *
 * @see IComputerItemFactory
 * @see ICustomRomItem
 */
public class ItemPocketComputer_Patch extends ItemPocketComputer implements IComputerItemFactory, ICustomRomItem {
	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		if (advanced) {
			int id = getComputerID(stack);
			if (id >= 0) tooltip.add("(Computer ID: " + id + ")");
		}

		if (hasCustomRom(stack)) {
			int id = getCustomRom(stack);
			if (advanced && id >= 0) {
				tooltip.add("Has custom ROM (disk ID: " + id + ")");
			} else {
				tooltip.add("Has custom ROM");
			}
		}
	}

	public IPocketUpgrade getUpgrade(ItemStack stack) {
		NBTTagCompound compound = stack.getTagCompound();
		if (compound != null) {
			if (compound.hasKey("upgrade_name", Constants.NBT.TAG_STRING)) {
				IPocketUpgrade upgrade = ComputerCraft.getPocketUpgrade(compound.getString("upgrade_name"));
				if (upgrade != null) return upgrade;
			}

			if (compound.hasKey("upgrade", Constants.NBT.TAG_STRING)) {
				String name = compound.getString("upgrade");
				return ComputerCraft.getPocketUpgrade(name);
			} else if (compound.hasKey("upgrade", Constants.NBT.TAG_ANY_NUMERIC)) {
				int id = compound.getInteger("upgrade");
				if (id == 1) {
					return ComputerCraft.getPocketUpgrade("computercraft:wireless_modem");
				}
			}
		}

		return null;
	}

	@Nonnull
	@Override
	public ItemStack createComputer(int id, @Nullable String label, @Nonnull ComputerFamily family) {
		return create(id, label, -1, family, null);
	}

	@Nonnull
	@Override
	public Set<ComputerFamily> getSupportedFamilies() {
		return EnumSet.of(ComputerFamily.Normal, ComputerFamily.Advanced);
	}

	@Nonnull
	@Override
	public ComputerFamily getDefaultFamily() {
		return ComputerFamily.Normal;
	}

	@Override
	public boolean hasCustomRom(@Nonnull ItemStack stack) {
		return stack.hasTagCompound() && stack.getTagCompound().hasKey("rom_id", 99);
	}

	@Override
	public int getCustomRom(@Nonnull ItemStack stack) {
		return stack.getTagCompound().getInteger("rom_id");
	}

	@Override
	public void clearCustomRom(@Nonnull ItemStack stack) {
		if (stack.hasTagCompound()) {
			NBTTagCompound tag = stack.getTagCompound();
			tag.removeTag("rom_id");
			tag.removeTag("instanceID");
			tag.removeTag("sessionID");
		}
	}

	@Override
	public void setCustomRom(@Nonnull ItemStack stack, int id) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag == null) stack.setTagCompound(tag = new NBTTagCompound());

		tag.setInteger("rom_id", id);
		tag.removeTag("instanceID");
		tag.removeTag("sessionID");
	}

	@MergeVisitor.Stub
	private void setComputerID(ItemStack stack, int id) {
	}

	@MergeVisitor.Stub
	private void setInstanceID(ItemStack stack, int id) {
	}

	@MergeVisitor.Stub
	private void setSessionID(ItemStack stack, int id) {
	}

	@MergeVisitor.Stub
	public int getComputerID(ItemStack stack) {
		return -1;
	}

	@MergeVisitor.Stub
	public int getInstanceID(ItemStack stack) {
		return -1;
	}

	@MergeVisitor.Stub
	public int getSessionID(ItemStack stack) {
		return -1;
	}
}
