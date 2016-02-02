package org.squiddev.cctweaks.turtle;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.registry.Registry;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Allows
 */
public class TurtleUpgradeToolHost implements ITurtleUpgrade {
	protected static final Map<ITurtleAccess, ToolHostPlayer> players = new WeakHashMap<ITurtleAccess, ToolHostPlayer>();

	@Override
	public int getUpgradeID() {
		return Config.Turtle.ToolHost.upgradeId;
	}

	@Override
	public String getUnlocalisedAdjective() {
		return "turtle." + CCTweaks.RESOURCE_DOMAIN + ".toolHost.adjective";
	}

	@Override
	public TurtleUpgradeType getType() {
		return TurtleUpgradeType.Tool;
	}

	@Override
	public ItemStack getCraftingItem() {
		return new ItemStack(Registry.itemToolHost);
	}

	@Override
	public IPeripheral createPeripheral(ITurtleAccess turtle, TurtleSide side) {
		return null;
	}

	@Override
	public TurtleCommandResult useTool(ITurtleAccess turtle, TurtleSide side, TurtleVerb verb, int direction) {
		if (!Config.Turtle.ToolHost.enabled) return null;

		switch (verb) {
			case Attack:
				return getPlayer(turtle).attack(turtle, direction);
			case Dig:
				return getPlayer(turtle).dig(turtle, direction);
		}

		return null;
	}

	public static ItemStack getItem(ITurtleAccess turtle) {
		return turtle.getInventory().getStackInSlot(turtle.getSelectedSlot());
	}

	public static ToolHostPlayer getPlayer(ITurtleAccess turtle) {
		ToolHostPlayer player = players.get(turtle);
		if (player == null) players.put(turtle, player = new ToolHostPlayer(turtle));
		return player;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ITurtleAccess turtle, TurtleSide side) {
		return Registry.itemToolHost.getIconFromDamage(0);
	}

	@Override
	public void update(ITurtleAccess turtle, TurtleSide side) {
	}
}
