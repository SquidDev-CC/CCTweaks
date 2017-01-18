package org.squiddev.cctweaks.integration.jei;

import com.google.common.collect.Lists;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;
import org.squiddev.cctweaks.api.pocket.IPocketUpgrade;
import org.squiddev.cctweaks.core.pocket.CraftingPocketUpgrade;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class PocketUpgradeWrapper extends BlankRecipeWrapper {
	private static final ItemStack[] COMPUTERS = new ItemStack[]{
		PocketComputerItemFactory.create(-1, null, ComputerFamily.Normal, false),
		PocketComputerItemFactory.create(-1, null, ComputerFamily.Advanced, false)
	};

	private final IPocketUpgrade upgrade;

	public PocketUpgradeWrapper(IPocketUpgrade upgrade) {
		this.upgrade = upgrade;
	}

	@Nonnull
	@Override
	public List<?> getInputs() {
		return Arrays.asList(
			upgrade.getCraftingItem(),
			Arrays.asList(COMPUTERS)
		);
	}

	@Nonnull
	@Override
	public List<?> getOutputs() {
		List<ItemStack> list = Lists.newArrayListWithExpectedSize(COMPUTERS.length);
		for (ItemStack computer : COMPUTERS) {
			list.add(CraftingPocketUpgrade.setNBT(computer.copy(), upgrade));
		}

		return list;
	}
}
