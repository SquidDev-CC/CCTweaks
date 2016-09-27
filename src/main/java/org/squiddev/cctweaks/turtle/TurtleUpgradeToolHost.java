package org.squiddev.cctweaks.turtle;

import dan200.computercraft.api.turtle.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.registry.Registry;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Allows
 */
public class TurtleUpgradeToolHost extends TurtleUpgradeBase {
	protected static final Map<ITurtleAccess, ToolHostPlayer> players = new WeakHashMap<ITurtleAccess, ToolHostPlayer>();

	public TurtleUpgradeToolHost() {
		this("toolHost", Config.Turtle.ToolHost.upgradeId, new ItemStack(Registry.itemToolHost, 1, 0));
	}

	public TurtleUpgradeToolHost(String name, int id, ItemStack stack) {
		super(name, id, stack);
	}

	@Override
	public TurtleUpgradeType getType() {
		return TurtleUpgradeType.Tool;
	}

	@Override
	public TurtleCommandResult useTool(ITurtleAccess turtle, TurtleSide side, TurtleVerb verb, EnumFacing direction) {
		if (!Config.Turtle.ToolHost.enabled) return null;

		switch (verb) {
			case Attack:
				return getPlayer(turtle).attack(turtle, direction);
			case Dig:
				return getPlayer(turtle).dig(turtle, direction);
		}

		return null;
	}

	public static ToolHostPlayer getPlayer(ITurtleAccess turtle) {
		ToolHostPlayer player = players.get(turtle);
		if (player == null) players.put(turtle, player = new ToolHostPlayer(turtle));
		return player;
	}

	@Override
	public void preInit() {
		super.preInit();
		EntityRegistry.registerModEntity(ToolHostPlayer.class, CCTweaks.ID + ":fakePlayer", 0, CCTweaks.instance, Integer.MAX_VALUE, Integer.MAX_VALUE, false);
	}
}
