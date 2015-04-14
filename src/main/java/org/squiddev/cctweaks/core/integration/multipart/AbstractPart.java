package org.squiddev.cctweaks.core.integration.multipart;

import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.vec.Cuboid6;
import codechicken.multipart.*;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for multiparts
 */
public abstract class AbstractPart extends TMultiPart implements JNormalOcclusion, JIconHitEffects {
	@Override
	public boolean occlusionTest(TMultiPart npart) {
		return NormalOcclusionTest.apply(this, npart);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getBreakingIcon(Object subPart, int side) {
		return getBrokenIcon(side);
	}

	@SideOnly(Side.CLIENT)
	public void addHitEffects(MovingObjectPosition hit, EffectRenderer effectRenderer) {
		IconHitEffects.addHitEffects(this, hit, effectRenderer);
	}

	@SideOnly(Side.CLIENT)
	public void addDestroyEffects(MovingObjectPosition hit, EffectRenderer effectRenderer) {
		IconHitEffects.addDestroyEffects(this, effectRenderer, false);
	}

	@Override
	public Iterable<Cuboid6> getCollisionBoxes() {
		List<Cuboid6> boxes = new ArrayList<Cuboid6>();

		for (IndexedCuboid6 icube : getSubParts()) {
			boxes.add(icube);
		}

		return boxes;
	}
}
