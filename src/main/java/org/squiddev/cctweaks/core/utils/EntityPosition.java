package org.squiddev.cctweaks.core.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.api.IWorldPosition;

public class EntityPosition implements IWorldPosition {
	public Entity entity;

	public EntityPosition(Entity entity) {
		this.entity = entity;
	}

	@Override
	public IBlockAccess getWorld() {
		return entity.worldObj;
	}

	@Override
	public int getX() {
		return (int) entity.posX;
	}

	@Override
	public int getY() {
		if (entity instanceof EntityLivingBase) {
			EntityLivingBase entityLiving = (EntityLivingBase) entity;
			return (int) (entityLiving.posY + entityLiving.getEyeHeight());
		} else {
			return (int) entity.posY;
		}
	}

	@Override
	public int getZ() {
		return (int) entity.posZ;
	}
}
