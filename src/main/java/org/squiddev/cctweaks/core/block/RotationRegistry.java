package org.squiddev.cctweaks.core.block;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.ActionResult;
import org.squiddev.cctweaks.api.block.IRotationHandler;
import org.squiddev.cctweaks.api.block.IRotationRegistry;

import javax.annotation.Nonnull;
import java.util.List;


public class RotationRegistry implements IRotationRegistry {
	private final Multimap<Block, IRotationHandler> blockHandlers = MultimapBuilder.hashKeys().arrayListValues().build();
	private final Multimap<Class<? extends Block>, IRotationHandler> classHandlers = MultimapBuilder.hashKeys().arrayListValues().build();
	private final List<IRotationHandler> generalHandlers = Lists.newArrayList();

	@Override
	public void register(@Nonnull IRotationHandler handler) {
		Preconditions.checkNotNull(handler, "handler cannot be null");
		generalHandlers.add(handler);
	}

	@Override
	public void register(@Nonnull Class<? extends Block> targetClass, @Nonnull IRotationHandler handler) {
		Preconditions.checkNotNull(targetClass, "targetClass cannot be null");
		Preconditions.checkNotNull(handler, "handler cannot be null");
		classHandlers.put(targetClass, handler);
	}

	@Override
	public void register(@Nonnull Block block, @Nonnull IRotationHandler handler) {
		Preconditions.checkNotNull(block, "block cannot be null");
		Preconditions.checkNotNull(handler, "handler cannot be null");
		blockHandlers.put(block, handler);
	}

	@Nonnull
	@Override
	public ActionResult rotate(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EnumFacing facing, @Nonnull EnumFacing rotatorFacing) {
		for (IRotationHandler handler : blockHandlers.get(state.getBlock())) {
			ActionResult result = handler.rotate(world, pos, state, facing, rotatorFacing);
			if (result != ActionResult.PASS) return result;
		}

		for (IRotationHandler handler : classHandlers.get(state.getBlock().getClass())) {
			ActionResult result = handler.rotate(world, pos, state, facing, rotatorFacing);
			if (result != ActionResult.PASS) return result;
		}

		for (IRotationHandler handler : generalHandlers) {
			ActionResult result = handler.rotate(world, pos, state, facing, rotatorFacing);
			if (result != ActionResult.PASS) return result;
		}

		return ActionResult.PASS;
	}
}