package org.squiddev.cctweaks.core.block;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.ActionResult;
import org.squiddev.cctweaks.api.CCTweaksAPI;
import org.squiddev.cctweaks.api.block.BasicRotationHandler;
import org.squiddev.cctweaks.api.block.IRotationHandler;
import org.squiddev.cctweaks.api.block.IRotationRegistry;
import org.squiddev.cctweaks.api.block.PropertyRotationHandler;
import org.squiddev.cctweaks.core.registry.Module;

import javax.annotation.Nonnull;

public class VanillaRotationHandlers extends Module {
	private static final IRotationHandler BLOCK_DIRECTIONAL = new PropertyRotationHandler(BlockDirectional.FACING);

	@Override
	public void init() {
		IRotationRegistry registry = CCTweaksAPI.instance().rotationRegistry();

		// Non BlockDirectional things
		registry.register(BlockAnvil.class, new PropertyRotationHandler(BlockAnvil.FACING));
		registry.register(BlockBanner.class, new PropertyRotationHandler(BlockBanner.FACING));
		registry.register(BlockButtonStone.class, new PropertyRotationHandler(BlockButton.FACING, true));
		registry.register(BlockButtonWood.class, new PropertyRotationHandler(BlockButton.FACING, true));
		registry.register(BlockDispenser.class, new PropertyRotationHandler(BlockDispenser.FACING));
		registry.register(BlockDropper.class, new PropertyRotationHandler(BlockDispenser.FACING));
		registry.register(BlockWallSign.class, new PropertyRotationHandler(BlockWallSign.FACING));
		registry.register(BlockTorch.class, new PropertyRotationHandler(BlockTorch.FACING));
		registry.register(BlockEnderChest.class, new PropertyRotationHandler(BlockEnderChest.FACING));
		registry.register(BlockFurnace.class, new PropertyRotationHandler(BlockFurnace.FACING));
		registry.register(BlockHopper.class, new PropertyRotationHandler(BlockHopper.FACING));
		registry.register(BlockLadder.class, new PropertyRotationHandler(BlockLadder.FACING));
		registry.register(BlockSkull.class, new PropertyRotationHandler(BlockSkull.FACING));
		registry.register(BlockStairs.class, new PropertyRotationHandler(BlockStairs.FACING));
		registry.register(BlockStem.class, new PropertyRotationHandler(BlockStem.FACING));
		registry.register(BlockTrapDoor.class, new PropertyRotationHandler(BlockTrapDoor.FACING, true));
		registry.register(BlockTripWireHook.class, new PropertyRotationHandler(BlockTripWireHook.FACING, true));
		registry.register(BlockWallSign.class, new PropertyRotationHandler(BlockWallSign.FACING));

		// BlockDirectional things
		registry.register(BlockCocoa.class, BLOCK_DIRECTIONAL);
		registry.register(BlockFenceGate.class, BLOCK_DIRECTIONAL);
		registry.register(BlockPumpkin.class, BLOCK_DIRECTIONAL);
		registry.register(BlockRedstoneComparator.class, BLOCK_DIRECTIONAL);
		registry.register(BlockRedstoneRepeater.class, BLOCK_DIRECTIONAL);

		// More advanced things
		registry.register(BlockPistonBase.class, new PropertyRotationHandler(BlockPistonBase.FACING) {
			@Nonnull
			@Override
			public ActionResult rotate(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EnumFacing facing, @Nonnull EnumFacing rotatorFacing) {
				if (state.getValue(BlockPistonBase.EXTENDED)) return ActionResult.FAILURE;
				return super.rotate(world, pos, state, facing, rotatorFacing);
			}
		});
		registry.register(BlockLever.class, new BasicRotationHandler(true) {
			@Nonnull
			@Override
			protected IBlockState setDirection(@Nonnull IBlockState state, @Nonnull EnumFacing facing, @Nonnull EnumFacing rotatorFacing) {
				return state.withProperty(BlockLever.FACING, BlockLever.EnumOrientation.forFacings(facing, rotatorFacing));
			}
		});
		registry.register(BlockChest.class, new IRotationHandler() {
			@Nonnull
			@Override
			public ActionResult rotate(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EnumFacing facing, @Nonnull EnumFacing rotatorFacing) {
				if (!BlockChest.FACING.getAllowedValues().contains(facing)) return ActionResult.FAILURE;

				// Fail if we've got an adjacent chest
				for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
					if (world.getBlockState(pos.offset(enumfacing)).getBlock() == state.getBlock()) {
						return ActionResult.FAILURE;
					}
				}

				world.setBlockState(pos, state.withProperty(BlockChest.FACING, facing));
				return ActionResult.SUCCESS;
			}
		});

		// TODO: Door
	}
}
