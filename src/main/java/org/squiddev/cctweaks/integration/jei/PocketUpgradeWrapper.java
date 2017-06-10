package org.squiddev.cctweaks.integration.jei;

import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;

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
		inputStack = PocketComputerItemFactory.create(-1, null, -1, family, null);
		upgradeStack = upgrade.getCraftingItem();
		outputStack = PocketComputerItemFactory.create(-1, null, -1, family, upgrade);
	}

	@Override
	public boolean isValid() {
		return upgradeStack != null;
	}

	@Override
	public void getIngredients(@Nonnull IIngredients ingredients) {
		ingredients.setInputs(ItemStack.class, Arrays.asList(upgradeStack, inputStack));
		ingredients.setOutput(ItemStack.class, outputStack);
	}
}
