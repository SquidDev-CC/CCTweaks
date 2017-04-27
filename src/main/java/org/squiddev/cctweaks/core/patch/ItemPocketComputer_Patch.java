package org.squiddev.cctweaks.core.patch;

import com.google.common.base.Objects;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.IComputerItemFactory;
import org.squiddev.cctweaks.api.computer.ICustomRomItem;
import org.squiddev.cctweaks.api.computer.IExtendedServerComputer;
import org.squiddev.cctweaks.core.pocket.PocketAPIExtensions;
import org.squiddev.cctweaks.core.pocket.PocketHooks;
import org.squiddev.cctweaks.core.pocket.PocketRegistry;
import org.squiddev.cctweaks.core.pocket.PocketServerComputer;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.cctweaks.core.utils.Helpers;
import org.squiddev.patcher.visitors.MergeVisitor;
import org.squiddev.unborked.ProxyServer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Patches for pocket computers.
 *
 * @see org.squiddev.cctweaks.core.pocket.PocketHooks
 */
public class ItemPocketComputer_Patch extends ItemPocketComputer implements IComputerItemFactory, ICustomRomItem {
	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (!world.isRemote) {
			ServerComputer computer = this.createServerComputer(world, player.inventory, stack);
			if (computer != null) computer.turnOn();

			if (!PocketHooks.rightClick(world, player, stack, computer)) {
				ProxyServer.openPocketComputerGUI(player, hand);
			}
		}

		return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
	}

	@Nonnull
	@Override
	public String getItemStackDisplayName(@Nonnull ItemStack stack) {
		String baseName = getUnlocalizedName(stack);
		String adjective = PocketRegistry.instance.getUpgradeAdjective(stack, null);

		if (adjective == null) return Helpers.translateToLocal(baseName + ".name");
		return Helpers.translateToLocal(baseName + ".upgraded.name").replace("%s", Helpers.translateToLocal(adjective));
	}

	public void onUpdate(ItemStack stack, World world, Entity entity, int slotNum, boolean selected) {
		if (!world.isRemote) {
			IInventory inventory = entity instanceof EntityPlayer ? ((EntityPlayer) entity).inventory : null;
			ServerComputer computer = createServerComputer(world, inventory, stack);
			if (computer != null) {
				computer.keepAlive();
				computer.setWorld(world);

				// Correctly sync pocket computer position & entity
				computer.setPosition(entity.getPosition());
				((PocketServerComputer) computer).setOwner(entity);

				int id = computer.getID();
				if (id != getComputerID(stack)) {
					setComputerID(stack, id);
					if (inventory != null) inventory.markDirty();
				}

				String label = computer.getLabel();
				if (!Objects.equal(label, getLabel(stack))) {
					setLabel(stack, label);
					if (inventory != null) inventory.markDirty();
				}

				// Update pocket hooks
				PocketHooks.update(entity, stack, computer);
			}
		} else {
			createClientComputer(stack);
		}
	}

	private ServerComputer createServerComputer(World world, IInventory inventory, ItemStack stack) {
		if (world.isRemote) {
			return null;
		}
		int instanceID = getInstanceID(stack);
		int sessionID = getSessionID(stack);
		int correctSessionID = ComputerCraft.serverComputerRegistry.getSessionID();
		ServerComputer computer;
		if (instanceID >= 0 && sessionID == correctSessionID && ComputerCraft.serverComputerRegistry.contains(instanceID)) {
			computer = ComputerCraft.serverComputerRegistry.get(instanceID);
		} else {
			if (instanceID < 0 || sessionID != correctSessionID) {
				instanceID = ComputerCraft.serverComputerRegistry.getUnusedInstanceID();
				setInstanceID(stack, instanceID);
				setSessionID(stack, correctSessionID);
			}

			int computerID = getComputerID(stack);
			if (computerID < 0) {
				computerID = ComputerCraft.createUniqueNumberedSaveDir(world, "computer");
				setComputerID(stack, computerID);
			}

			computer = new PocketServerComputer(world, computerID, getLabel(stack), instanceID, getFamily(stack), 26, 20);
			if (hasCustomRom(stack)) {
				DebugLogger.debug("Setting custom ROM from " + stack);
				((IExtendedServerComputer) computer).setCustomRom(getCustomRom(stack));
			}

			computer.addAPI(new PocketAPIExtensions(computer));
			PocketHooks.create(inventory, stack, computer);
			ComputerCraft.serverComputerRegistry.add(instanceID, computer);

			if (inventory != null) inventory.markDirty();
		}

		computer.setWorld(world);
		return computer;
	}

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

	@Nonnull
	@Override
	public ItemStack createComputer(int id, @Nullable String label, @Nonnull ComputerFamily family) {
		return create(id, label, family, false);
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
