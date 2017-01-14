package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.shared.turtle.core.TurtlePlaceCommand;
import dan200.computercraft.shared.turtle.core.TurtlePlayer;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.squiddev.cctweaks.core.turtle.TurtleHooks;
import org.squiddev.patcher.visitors.MergeVisitor;

public class TurtlePlaceCommand_Patch extends TurtlePlaceCommand {
	public TurtlePlaceCommand_Patch() {
		super(null, null);
	}

	public static ItemStack deploy(ItemStack stack, ITurtleAccess turtle, EnumFacing direction, Object[] extraArguments, String[] errorMessage) {
		BlockPos playerPosition = WorldUtil.moveCoords(turtle.getPosition(), direction);
		TurtlePlayer turtlePlayer = createPlayer(turtle, playerPosition, direction);
		ItemStack remainder = deployOnEntity(stack, turtle, turtlePlayer, direction, extraArguments, errorMessage);
		if (remainder != stack) return remainder;


		BlockPos position = turtle.getPosition();
		BlockPos newPosition = WorldUtil.moveCoords(position, direction);


		remainder = deployOnBlock(stack, turtle, turtlePlayer, newPosition, direction.getOpposite(), extraArguments, true, errorMessage);
		if (remainder == stack) {
			remainder = deployOnBlock(stack, turtle, turtlePlayer, WorldUtil.moveCoords(newPosition, direction), direction.getOpposite(), extraArguments, false, errorMessage);
			if (remainder == stack) {
				if (direction.getAxis() != EnumFacing.Axis.Y) {
					remainder = deployOnBlock(stack, turtle, turtlePlayer, newPosition.down(), EnumFacing.UP, extraArguments, false, errorMessage);
				}

				if (remainder == stack) {
					remainder = deployOnBlock(stack, turtle, turtlePlayer, position, direction, extraArguments, false, errorMessage);
				}
			}
		}

		if (remainder != stack) {
			TurtleHooks.rotate(turtle, position, direction, extraArguments, errorMessage);
		}

		return remainder;
	}

	@MergeVisitor.Stub
	private static ItemStack deployOnEntity(ItemStack stack, final ITurtleAccess turtle, TurtlePlayer turtlePlayer, EnumFacing direction, Object[] extraArguments, String[] o_errorMessage) {
		throw new RuntimeException("Never");
	}

	@MergeVisitor.Stub
	private static ItemStack deployOnBlock(ItemStack stack, ITurtleAccess turtle, TurtlePlayer turtlePlayer, BlockPos position, EnumFacing side, Object[] extraArguments, boolean allowReplace, String[] o_errorMessage) {
		throw new RuntimeException("Never");
	}
}
