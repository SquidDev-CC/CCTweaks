package org.squiddev.cctweaks.integration.jei;

import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.turtle.items.TurtleItemFactory;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class TurtleUpgradeWrapper extends BlankRecipeWrapper implements IValidRecipeWrapper {
	public static final ComputerFamily[] FAMILIES = new ComputerFamily[]{
		ComputerFamily.Normal, ComputerFamily.Advanced,
	};

	private final ItemStack inputStack;
	private final ItemStack upgradeStack;
	private final ItemStack outputStack;

	public TurtleUpgradeWrapper(ITurtleUpgrade upgrade, ComputerFamily family) {
		this.inputStack = TurtleItemFactory.create(-1, null, null, family, null, null, 0, null);
		this.upgradeStack = upgrade.getCraftingItem();
		this.outputStack = TurtleItemFactory.create(-1, null, null, family, null, upgrade, 0, null);
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
