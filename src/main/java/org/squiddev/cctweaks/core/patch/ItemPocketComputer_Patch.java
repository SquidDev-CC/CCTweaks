package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import org.squiddev.cctweaks.core.asm.PocketUpgrades;
import org.squiddev.cctweaks.core.pocket.PocketHooks;
import org.squiddev.cctweaks.core.pocket.PocketRegistry;
import org.squiddev.cctweaks.core.utils.Helpers;
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
		throw new RuntimeException("Not implemented");
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
		if (!world.isRemote) {
			ServerComputer computer = this.createServerComputer(world, player.inventory, stack);
			if (computer != null) computer.turnOn();

			if (PocketHooks.rightClick(world, player, stack, computer)) {
				return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
			}

			ComputerCraft.openPocketComputerGUI(player);
		}

		return ActionResult.newResult(EnumActionResult.PASS, stack);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String baseName = getUnlocalizedName(stack);
		String adjective = PocketRegistry.instance.getUpgradeAdjective(stack, null);

		if (adjective == null) return Helpers.translateToLocal(baseName + ".name");
		return Helpers.translateToLocal(baseName + ".upgraded.name").replace("%s", I18n.translateToLocal(adjective));
	}
}
