package org.squiddev.cctweaks.integration.jei;

import com.google.common.collect.Lists;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class PocketModemUpgradeWrapper extends BlankRecipeWrapper {
	private static final ComputerFamily[] FAMILIES = new ComputerFamily[]{
		ComputerFamily.Normal, ComputerFamily.Advanced,
	};

	@Nonnull
	@Override
	public List<?> getInputs() {
		List<ItemStack> computers = Lists.newArrayListWithExpectedSize(FAMILIES.length);
		for (ComputerFamily family : FAMILIES) {
			computers.add(PocketComputerItemFactory.create(-1, null, family, false));
		}

		return Arrays.asList(PeripheralItemFactory.create(PeripheralType.WirelessModem, null, 1), computers);
	}

	@Nonnull
	@Override
	public List<?> getOutputs() {
		List<ItemStack> computers = Lists.newArrayListWithExpectedSize(FAMILIES.length);
		for (ComputerFamily family : FAMILIES) {
			computers.add(PocketComputerItemFactory.create(-1, null, family, true));
		}

		return computers;
	}
}
