package org.squiddev.cctweaks.core.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.api.IWorldPosition;

import javax.annotation.Nonnull;

public class EntityPosition implements IWorldPosition {
	public Entity entity;

	public EntityPosition(Entity entity) {
		this.entity = entity;
	}

	@Nonnull
	@Override
	public IBlockAccess getBlockAccess() {
		return entity.getEntityWorld();
	}

	@Nonnull
	@Override
	public BlockPos getPosition() {
		int y;
		if (entity instanceof EntityLivingBase) {
			EntityLivingBase entityLiving = (EntityLivingBase) entity;
			y = (int) (entityLiving.posY + entityLiving.getEyeHeight());
		} else {
			y = (int) entity.posY;
		}

		return new BlockPos(entity.posX, y, entity.posZ);
	}

}
