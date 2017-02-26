package org.squiddev.cctweaks.core.patch;

import com.google.common.base.Objects;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import dan200.computercraft.shared.pocket.peripherals.PocketModemPeripheral;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.IComputerItemFactory;
import org.squiddev.cctweaks.core.pocket.PocketHooks;
import org.squiddev.cctweaks.core.pocket.PocketRegistry;
import org.squiddev.patcher.visitors.MergeVisitor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Set;

/**
 * Patches for pocket computers.
 *
 * @see org.squiddev.cctweaks.core.pocket.PocketHooks
 */
public class ItemPocketComputer_Patch extends ItemPocketComputer implements IComputerItemFactory {
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (!world.isRemote) {
			ServerComputer computer = createServerComputer(world, player.inventory, stack);
			if (computer != null) computer.turnOn();

			if (PocketHooks.rightClick(world, player, stack, computer)) return stack;

			ComputerCraft.openPocketComputerGUI(player);
		}

		return stack;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String baseName = getUnlocalizedName(stack);
		String adjective = PocketRegistry.instance.getUpgradeAdjective(stack, null);

		if (adjective == null) return StatCollector.translateToLocal(baseName + ".name");
		return StatCollector.translateToLocal(baseName + ".upgraded.name").replace("%s", StatCollector.translateToLocal(adjective));
	}

	public void onUpdate(ItemStack stack, World world, Entity entity, int slotNum, boolean selected) {
		if (!world.isRemote) {
			IInventory inventory = entity instanceof EntityPlayer ? ((EntityPlayer) entity).inventory : null;
			ServerComputer computer = createServerComputer(world, inventory, stack);
			if (computer != null) {
				computer.keepAlive();
				computer.setWorld(world);

				// Correctly sync pocket computer position
				computer.setPosition(entity.getPosition());

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

				IPeripheral peripheral = computer.getPeripheral(2);
				if (peripheral instanceof PocketModemPeripheral) {
					PocketModemPeripheral modem = (PocketModemPeripheral) peripheral;
					if (entity instanceof EntityLivingBase) {
						EntityLivingBase player = (EntityLivingBase) entity;
						modem.setLocation(world, player.posX, player.posY + player.getEyeHeight(), player.posZ);
					} else {
						modem.setLocation(world, entity.posX, entity.posY, entity.posZ);
					}
					boolean modemLight = modem.isActive();
					NBTTagCompound modemNBT = computer.getUserData();
					if (modemNBT.getBoolean("modemLight") != modemLight) {
						modemNBT.setBoolean("modemLight", modemLight);
						computer.updateUserData();
					}
				}
			}
		} else {
			createClientComputer(stack);
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

	@MergeVisitor.Stub
	private void setComputerID(ItemStack stack, int computerID) {
	}

	@MergeVisitor.Stub
	public int getComputerID(ItemStack stack) {
		return -1;
	}

	@MergeVisitor.Stub
	private ServerComputer createServerComputer(World world, IInventory inventory, ItemStack stack) {
		throw new RuntimeException("Not implemented");
	}
}
