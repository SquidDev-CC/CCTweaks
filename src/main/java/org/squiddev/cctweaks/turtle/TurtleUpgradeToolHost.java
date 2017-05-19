package org.squiddev.cctweaks.turtle;

import dan200.computercraft.api.turtle.*;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.registry.Registry;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Allows
 */
public class TurtleUpgradeToolHost extends TurtleUpgradeBase {
	protected static final Map<ITurtleAccess, ToolHostPlayer> players = new WeakHashMap<ITurtleAccess, ToolHostPlayer>();

	public TurtleUpgradeToolHost() {
		this("toolHost", Config.Turtle.ToolHost.upgradeId);
	}

	public TurtleUpgradeToolHost(String name, int id) {
		super(name, id);
	}

	@Nonnull
	@Override
	public TurtleUpgradeType getType() {
		return TurtleUpgradeType.Tool;
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Registry.itemToolHost, 1, 0);
	}

	@Nonnull
	@Override
	public TurtleCommandResult useTool(@Nonnull ITurtleAccess turtle, @Nonnull TurtleSide side, @Nonnull TurtleVerb verb, @Nonnull EnumFacing direction) {
		if (!Config.Turtle.ToolHost.enabled) return TurtleCommandResult.failure("Tool host not enabled");

		switch (verb) {
			case Attack: {
				ToolHostPlayer player = getPlayer(turtle);
				player.load(turtle, direction, false);

				try {

					Vec3d rayDir = player.getLook(1.0f);
					Vec3d rayStart = new Vec3d(player.posX, player.posY, player.posZ);

					Pair<Entity, Vec3d> hit = WorldUtil.rayTraceEntities(turtle.getWorld(), rayStart, rayDir, 1.5);

					return player.attack(turtle, hit == null ? null : hit.getLeft());
				} finally {
					player.unload(turtle);
				}
			}
			case Dig: {
				ToolHostPlayer player = getPlayer(turtle);
				player.load(turtle, direction, false);

				try {
					return player.dig(turtle, direction, turtle.getPosition().offset(direction));
				} finally {
					player.unload(turtle);
				}
			}
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
