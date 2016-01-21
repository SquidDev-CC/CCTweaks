package org.squiddev.cctweaks.turtle;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.api.turtle.TurtleUpgradeType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.turtle.IExtendedTurtleUpgrade;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.registry.Registry;

public class TurtleUpgradeToolManipulator extends TurtleUpgradeToolHost implements IExtendedTurtleUpgrade {
	@Override
	public IPeripheral createPeripheral(ITurtleAccess turtle, TurtleSide side) {
		return new ToolHostPeripheral(turtle, getPlayer(turtle));
	}

	@Override
	public int getUpgradeID() {
		return Config.Turtle.ToolHost.advancedUpgradeId;
	}

	@Override
	public String getUnlocalisedAdjective() {
		return "turtle." + CCTweaks.RESOURCE_DOMAIN + ".toolHost.advanced.adjective";
	}

	@Override
	public TurtleUpgradeType getType() {
		return TurtleUpgradeType.Tool;
	}

	@Override
	public ItemStack getCraftingItem() {
		return new ItemStack(Registry.itemToolHost, 1, 1);
	}

	@Override
	public IIcon defaultIcon() {
		return Registry.itemToolHost.getIconFromDamage(1);
	}

	@Override
	public void upgradeChanged(ITurtleAccess turtle, TurtleSide side, ITurtleUpgrade oldUpgrade, ITurtleUpgrade newUpgrade) {
	}

	@Override
	public boolean alsoPeripheral() {
		return true;
	}
}
