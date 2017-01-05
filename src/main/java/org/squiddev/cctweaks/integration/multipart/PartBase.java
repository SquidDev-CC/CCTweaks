package org.squiddev.cctweaks.integration.multipart;

import mcmultipart.client.multipart.IHitEffectsPart;
import mcmultipart.multipart.IOccludingPart;
import mcmultipart.multipart.Multipart;
import mcmultipart.multipart.MultipartRegistry;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.api.IWorldPosition;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public abstract class PartBase extends Multipart implements IHitEffectsPart, IOccludingPart, IWorldPosition {
	public abstract Block getBlock();

	public ItemStack getStack() {
		return new ItemStack(getBlock());
	}

	@Override
	public abstract void addSelectionBoxes(List<AxisAlignedBB> list);

	@Override
	public abstract void addCollisionBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity);

	@Override
	public ItemStack getPickBlock(EntityPlayer player, PartMOP hit) {
		return getStack();
	}

	@Override
	public List<ItemStack> getDrops() {
		return Collections.singletonList(getStack());
	}

	@Override
	public boolean addDestroyEffects(AdvancedEffectRenderer advancedEffectRenderer) {
		advancedEffectRenderer.addBlockDestroyEffects(
			getPos(),
			Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(
				getExtendedState(MultipartRegistry.getDefaultState(this).getBaseState())
			)
		);
		return true;
	}

	@Override
	public boolean addHitEffects(PartMOP partMOP, AdvancedEffectRenderer advancedEffectRenderer) {
		return true;
	}

	@Nonnull
	@Override
	public final IBlockAccess getBlockAccess() {
		return getWorld();
	}

	@Nonnull
	@Override
	public final BlockPos getPosition() {
		return getPos();
	}

	@Override
	public void onConverted(TileEntity tile) {
		super.onConverted(tile);
		onAdded();
	}
}
