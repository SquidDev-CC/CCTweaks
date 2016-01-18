package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import org.squiddev.cctweaks.core.asm.PocketUpgrades;
import org.squiddev.cctweaks.core.pocket.PocketHooks;
import org.squiddev.cctweaks.core.pocket.PocketRegistry;
import org.squiddev.patcher.visitors.MergeVisitor;

/**
 * Patches for pocket computers.
 *
 * @see org.squiddev.cctweaks.core.pocket.PocketHooks
 * @see PocketUpgrades
 */
public class ItemPocketComputer_Patch extends ItemPocketComputer {
	@MergeVisitor.Stub
	private ServerComputer createServerComputer(World world, IInventory inventory, ItemStack stack) {
		return null;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (!world.isRemote) {
			ServerComputer computer = this.createServerComputer(world, player.inventory, stack);
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
		return adjective == null ? StatCollector.translateToLocal(baseName + ".name") : StatCollector.translateToLocalFormatted(baseName + ".upgraded.name", adjective);
	}
}
