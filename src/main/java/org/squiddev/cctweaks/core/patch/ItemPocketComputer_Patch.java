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
import org.squiddev.patcher.visitors.MergeVisitor;

import javax.annotation.Nonnull;

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

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(@Nonnull ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
		if (!world.isRemote) {
			ServerComputer computer = this.createServerComputer(world, player.inventory, stack);
			if (computer != null) computer.turnOn();

			if (PocketHooks.rightClick(world, player, stack, computer)) {
				return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
			}

			ComputerCraft.openPocketComputerGUI(player);
		}

		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}

	@Nonnull
	@Override
	public String getItemStackDisplayName(@Nonnull ItemStack stack) {
		String baseName = getUnlocalizedName(stack);
		String adjective = PocketRegistry.instance.getUpgradeAdjective(stack, null);

		if (adjective == null) return I18n.translateToLocal(baseName + ".name");
		return I18n.translateToLocal(baseName + ".upgraded.name").replace("%s", I18n.translateToLocal(adjective));
	}
}
