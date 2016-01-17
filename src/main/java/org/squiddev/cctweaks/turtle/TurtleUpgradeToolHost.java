package org.squiddev.cctweaks.turtle;

import dan200.computercraft.api.turtle.*;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.registry.Registry;

import javax.vecmath.Matrix4f;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Allows
 */
public class TurtleUpgradeToolHost extends TurtleUpgradeBase {
	protected static final Map<ITurtleAccess, ToolHostPlayer> players = new WeakHashMap<ITurtleAccess, ToolHostPlayer>();

	public TurtleUpgradeToolHost() {
		super("toolHost", Config.Turtle.ToolHost.upgradeId, Registry.itemToolHost);
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
				return getPlayer(turtle).attack(direction);
			case Dig:
				return getPlayer(turtle).dig(direction);
		}

		return null;
	}

	public static ItemStack getItem(ITurtleAccess turtle) {
		return turtle == null ? null : turtle.getInventory().getStackInSlot(turtle.getSelectedSlot());
	}

	public static ToolHostPlayer getPlayer(ITurtleAccess turtle) {
		ToolHostPlayer player = players.get(turtle);
		if (player == null) players.put(turtle, player = new ToolHostPlayer(turtle));
		return player;
	}


	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings("deprecation")
	public Pair<IBakedModel, Matrix4f> getModel(ITurtleAccess access, TurtleSide side) {
		ItemStack stack = getItem(access);
		if (stack == null) return super.getModel(access, side);

		float xOffset = side == TurtleSide.Left ? -0.40625F : 0.40625F;
		Matrix4f transform = new Matrix4f(0.0F, 0.0F, -1.0F, 1.0F + xOffset, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F);

		return Pair.of(getMesher().getItemModel(stack), transform);
	}
}
