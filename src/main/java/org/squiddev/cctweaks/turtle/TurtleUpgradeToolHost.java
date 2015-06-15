package org.squiddev.cctweaks.turtle;

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
		if (!Config.Turtle.ToolHost.enabled) return TurtleCommandResult.failure("Disabled");

		switch (verb) {
			case Attack:
				return getPlayer(turtle).attack(direction);
			case Dig:
				return getPlayer(turtle).dig(direction);
		}

		return TurtleCommandResult.failure("Unknown " + verb);
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
	public IIcon getIcon(ITurtleAccess turtle, TurtleSide side) {
		ItemStack item;
		IIcon icon;

		// Sometimes the turtle is null (if in the inventory).
		// Also, we should only render if the icon is an item - not a block
		if (turtle != null && (item = getItem(turtle)) != null && item.getItemSpriteNumber() == 1 && (icon = item.getItem().getIcon(item, 0)) != null) {
			return icon;
		}
		return Registry.itemToolHost.getIconFromDamage(0);
	}

	@Override
	public void update(ITurtleAccess turtle, TurtleSide side) {
	}
}
