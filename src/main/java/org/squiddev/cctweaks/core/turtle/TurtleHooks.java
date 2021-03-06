package org.squiddev.cctweaks.core.turtle;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.permissions.ITurtlePermissionProvider;
import dan200.computercraft.api.turtle.ITurtleAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.squiddev.cctweaks.api.CCTweaksAPI;

import java.util.List;

/**
 * Various hooks for turtle patches
 */
public class TurtleHooks {
	private static List<ITurtlePermissionProvider> permissionProviders;

	public static boolean rotate(ITurtleAccess turtle, BlockPos pos, EnumFacing side, Object[] args, String[] error) {
		if (args == null || args.length < 2) return false;
		if (!(args[1] instanceof String)) {
			if (error != null) error[0] = "Expected string";
			return false;
		}

		EnumFacing argDir = LuaDirection.getDirection((String) args[1]);
		if (argDir == null) {
			if (error != null) error[0] = "Unknown direction";
			return false;
		}

		EnumFacing direction = LuaDirection.orient(argDir, turtle.getDirection());

		World world = turtle.getWorld();
		BlockPos offsetPos = pos.offset(side);
		IBlockState state = world.getBlockState(offsetPos);
		if (state.getBlock() == Blocks.AIR) {
			state = world.getBlockState(pos);
		} else {
			pos = offsetPos;
		}

		EnumActionResult result = CCTweaksAPI.instance().rotationRegistry().rotate(world, pos, state, direction, turtle.getDirection());

		if (error != null) {
			switch (result) {
				case FAIL:
					error[0] = "Could not rotate";
					break;
				case PASS:
					error[0] = "Do not know how to rotate";
					break;
			}
		}

		return result == EnumActionResult.SUCCESS;
	}

	private static List<ITurtlePermissionProvider> getPermissionProviders() {
		if (permissionProviders == null) {
			permissionProviders = ReflectionHelper.getPrivateValue(ComputerCraft.class, null, "permissionProviders");
		}

		return permissionProviders;
	}
}
