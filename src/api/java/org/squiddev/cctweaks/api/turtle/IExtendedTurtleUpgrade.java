package org.squiddev.cctweaks.api.turtle;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.api.turtle.TurtleUpgradeType;

/**
 * Extended functionality for turtle upgrades.
 */
public interface IExtendedTurtleUpgrade extends ITurtleUpgrade {
	/**
	 * Fired when an upgrade has changed on the turtle
	 *
	 * @param turtle     The turtle the upgrade has changed on
	 * @param side       The side <em>this</em> upgrade is attached to. The opposite side is the one which has changed.
	 * @param oldUpgrade The old upgrade. May be {@code null}.
	 * @param newUpgrade The new upgrade. May be {@code null}.
	 */
	void upgradeChanged(ITurtleAccess turtle, TurtleSide side, ITurtleUpgrade oldUpgrade, ITurtleUpgrade newUpgrade);

	/**
	 * Check if this upgrade is a peripheral and a tool.
	 *
	 * You should return {@link TurtleUpgradeType#Tool} from {@link ITurtleUpgrade#getType()} to allow
	 * this tool exposing a peripheral and being usable as a tool.
	 *
	 * @return If this is also a peripheral
	 */
	boolean alsoPeripheral();
}
