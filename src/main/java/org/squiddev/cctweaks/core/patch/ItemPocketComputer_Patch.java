package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.IComputerItemFactory;
import org.squiddev.cctweaks.core.asm.PocketUpgrades;
import org.squiddev.cctweaks.core.pocket.PocketHooks;
import org.squiddev.cctweaks.core.pocket.PocketRegistry;
import org.squiddev.cctweaks.core.utils.Helpers;
import org.squiddev.patcher.visitors.MergeVisitor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Set;

/**
 * Patches for pocket computers.
 *
 * @see org.squiddev.cctweaks.core.pocket.PocketHooks
 * @see PocketUpgrades
 */
public class ItemPocketComputer_Patch extends ItemPocketComputer implements IComputerItemFactory {
	@MergeVisitor.Stub
	private ServerComputer createServerComputer(World world, IInventory inventory, ItemStack stack) {
		throw new RuntimeException("Not implemented");
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
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

	@Nonnull
	@Override
	public String getItemStackDisplayName(@Nonnull ItemStack stack) {
		String baseName = getUnlocalizedName(stack);
		String adjective = PocketRegistry.instance.getUpgradeAdjective(stack, null);

		if (adjective == null) return Helpers.translateToLocal(baseName + ".name");
		return Helpers.translateToLocal(baseName + ".upgraded.name").replace("%s", Helpers.translateToLocal(adjective));
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
}
