package org.squiddev.cctweaks.turtle;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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
import org.squiddev.cctweaks.core.utils.DebugLogger;

public class TurtleUpgradeToolManipulator extends TurtleUpgradeToolHost implements IExtendedTurtleUpgrade {
	@Override
	public IPeripheral createPeripheral(ITurtleAccess turtle, TurtleSide side) {
		DebugLogger.debug("Creating peripherals with " + players.size() + " players");
		return new ToolManipulatorPeripheral(turtle, getPlayer(turtle), side);
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
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ITurtleAccess turtle, TurtleSide side) {
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
