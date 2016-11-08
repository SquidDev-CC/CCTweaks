package org.squiddev.cctweaks.core.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.api.IWorldPosition;

public class EntityPosition implements IWorldPosition {
	public Entity entity;

	public EntityPosition(Entity entity) {
		this.entity = entity;
	}

	@Override
	public IBlockAccess getBlockAccess() {
		return entity.worldObj;
	}

	@Override
	public BlockPos getPosition() {
		int y;
		if (entity instanceof EntityLivingBase) {
			EntityLivingBase entityLiving = (EntityLivingBase) entity;
			y = (int) (entityLiving.posY + entityLiving.getEyeHeight());
		} else {
			y = (int) entity.posY;
		}

		return new BlockPos((int) entity.posX, y, (int) entity.posZ);
	}

}
