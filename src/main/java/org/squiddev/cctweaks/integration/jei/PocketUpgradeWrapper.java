package org.squiddev.cctweaks.integration.jei;

import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;
import org.squiddev.cctweaks.api.pocket.IPocketUpgrade;
import org.squiddev.cctweaks.core.pocket.CraftingPocketUpgrade;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PocketUpgradeWrapper extends BlankRecipeWrapper implements IValidRecipeWrapper {
	public static final ComputerFamily[] FAMILIES = new ComputerFamily[]{
		ComputerFamily.Normal, ComputerFamily.Advanced,
	};

	private final ItemStack inputStack;
	private final ItemStack upgradeStack;
	private final ItemStack outputStack;

	public PocketUpgradeWrapper(ItemStack inputStack, ItemStack upgradeStack, ItemStack outputStack) {
		this.inputStack = inputStack;
		this.upgradeStack = upgradeStack;
		this.outputStack = outputStack;
	}

	public PocketUpgradeWrapper(IPocketUpgrade upgrade, ComputerFamily family) {
		inputStack = PocketComputerItemFactory.create(-1, null, family, false);
		upgradeStack = upgrade.getCraftingItem();
		outputStack = CraftingPocketUpgrade.setNBT(inputStack.copy(), upgrade);
	}

	@Override
	public boolean isValid() {
		return upgradeStack != null;
	}

	@Nonnull
	@Override
	public List<?> getInputs() {
		return Arrays.asList(upgradeStack, inputStack);
	}

	@Nonnull
	@Override
	public List<?> getOutputs() {
		return Collections.singletonList(outputStack);
	}
}
