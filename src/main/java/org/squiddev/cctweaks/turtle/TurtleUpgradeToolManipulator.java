package org.squiddev.cctweaks.turtle;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.api.turtle.TurtleUpgradeType;
import net.minecraft.item.ItemStack;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.turtle.IExtendedTurtleUpgrade;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.registry.Registry;

import javax.annotation.Nonnull;

public class TurtleUpgradeToolManipulator extends TurtleUpgradeToolHost implements IExtendedTurtleUpgrade {
	public TurtleUpgradeToolManipulator() {
		super("toolManipulator", Config.Turtle.ToolHost.advancedUpgradeId);
	}

	@Nonnull
	@Override
	protected ItemStack getStack() {
		return new ItemStack(Registry.itemToolHost, 1, 1);
	}

	@Override
	public IPeripheral createPeripheral(@Nonnull ITurtleAccess turtle, @Nonnull TurtleSide side) {
		return new ToolManipulatorPeripheral(turtle, getPlayer(turtle), side);
	}

	@Nonnull
	@Override
	public String getUnlocalisedAdjective() {
		return "turtle." + CCTweaks.ID + ".toolHost.advanced.adjective";
	}

	@Nonnull
	@Override
	public TurtleUpgradeType getType() {
		return TurtleUpgradeType.Both;
	}

	@Override
	public void upgradeChanged(@Nonnull ITurtleAccess turtle, @Nonnull TurtleSide side, ITurtleUpgrade oldUpgrade, ITurtleUpgrade newUpgrade) {
	}
}
