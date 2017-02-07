package org.squiddev.cctweaks.core.turtle;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.permissions.ITurtlePermissionProvider;
import dan200.computercraft.api.turtle.ITurtleAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.squiddev.cctweaks.api.ActionResult;
import org.squiddev.cctweaks.api.CCTweaksAPI;
import org.squiddev.cctweaks.core.Config;

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
		if (state.getBlock() == Blocks.air) {
			state = world.getBlockState(pos);
		} else {
			pos = offsetPos;
		}

		ActionResult result = CCTweaksAPI.instance().rotationRegistry().rotate(world, pos, state, direction, turtle.getDirection());

		if (error != null) {
			switch (result) {
				case FAILURE:
					error[0] = "Could not rotate";
					break;
				case PASS:
					error[0] = "Do not know how to rotate";
					break;
			}
		}

		return result == ActionResult.SUCCESS;
	}

	private static List<ITurtlePermissionProvider> getPermissionProviders() {
		if (permissionProviders == null) {
			permissionProviders = ReflectionHelper.getPrivateValue(ComputerCraft.class, null, "permissionProviders");
		}

		return permissionProviders;
	}

	public static boolean isBlockBreakable(World world, BlockPos pos, EntityPlayer player) {
		if (Config.Turtle.useServerProtected) {
			MinecraftServer server = MinecraftServer.getServer();
			if (server != null && !world.isRemote && server.isBlockProtected(world, pos, player)) {
				return false;
			}
		}

		if (Config.Turtle.useBlockEvent) {
			IBlockState state = world.getBlockState(pos);
			if (MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(world, pos, state, player))) {
				return false;
			}
		}

		for (ITurtlePermissionProvider provider : getPermissionProviders()) {
			if (!provider.isBlockEditable(world, pos)) {
				return false;
			}
		}

		return true;
	}
}
