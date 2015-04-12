package org.squiddev.cctweaks.core.integration.multipart;

import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.IconHitEffects;
import codechicken.multipart.JIconHitEffects;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.TMultiPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.client.render.FixedRenderBlocks;
import dan200.computercraft.shared.peripheral.common.BlockCable;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import net.minecraft.block.Block;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class CablePart extends TMultiPart implements JNormalOcclusion, JIconHitEffects {
	@SideOnly(Side.CLIENT)
	private static CableRenderer render;

	@SideOnly(Side.CLIENT)
	public static CableRenderer getRender() {
		CableRenderer draw = render;
		if (draw == null) {
			draw = render = new CableRenderer();
		}
		return draw;
	}

	private List<Cuboid6> collisions = Collections.singletonList(getCollision());
	public static final String NAME = CCTweaks.NAME + ":networkCable";

	@Override
	public String getType() {
		return NAME;
	}

	@Override
	public Iterable<Cuboid6> getOcclusionBoxes() {
		return collisions;
	}

	@Override
	public Iterable<Cuboid6> getCollisionBoxes() {
		return getOcclusionBoxes();
	}

	@Override
	public Iterable<IndexedCuboid6> getSubParts() {
		return Collections.singletonList(new IndexedCuboid6(0, getCollision()));
	}

	public Cuboid6 getCollision() {
		return new Cuboid6(0.375D, 0.375D, 0.375D, 0.625D, 0.625D, 0.625D);
	}

	@Override
	public Cuboid6 getBounds() {
		return getCollision();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getBreakingIcon(Object subPart, int side) {
		return getBrokenIcon(side);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getBrokenIcon(int side) {
		return ComputerCraft.Blocks.cable.getIcon(0, 0);
	}

	public void addHitEffects(MovingObjectPosition hit, EffectRenderer effectRenderer) {
		IconHitEffects.addHitEffects(this, hit, effectRenderer);
	}

	public void addDestroyEffects(MovingObjectPosition hit, EffectRenderer effectRenderer) {
		IconHitEffects.addDestroyEffects(this, effectRenderer, false);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderStatic(Vector3 pos, int pass) {
		if (pass == 0) {
			TextureUtils.bindAtlas(0);
			getRender().drawTile(world(), x(), y(), z());
			return true;
		}
		return false;
	}

	public static class CableRenderer extends FixedRenderBlocks {
		public IIcon[] icons;

		public IIcon[] getIcons() {
			IIcon[] icons;
			if ((icons = this.icons) == null) {

				try {
					Field field = TileCable.class.getDeclaredField("s_cableIcons");
					field.setAccessible(true);
					icons = (IIcon[]) field.get(null);
				} catch (ReflectiveOperationException e) {
					DebugLogger.error("Cannot find TileCable texture");
					e.printStackTrace();
					icons = new IIcon[2];
				}
				this.icons = icons;
			}

			return icons;
		}

		@Override
		public IIcon getBlockIcon(Block block, IBlockAccess world, int x, int y, int z, int side) {
			int dir = -1;

			if ((BlockCable.isCable(world, x - 1, y, z)) || (BlockCable.isCable(world, x + 1, y, z))) {
				dir = dir == -1 || dir == 4 ? 4 : -2;
			}
			if ((BlockCable.isCable(world, x, y - 1, z)) || (BlockCable.isCable(world, x, y + 1, z))) {
				dir = dir == -1 || dir == 0 ? 0 : -2;
			}
			if ((BlockCable.isCable(world, x, y, z - 1)) || (BlockCable.isCable(world, x, y, z + 1))) {
				dir = dir == -1 || dir == 2 ? 2 : -2;
			}
			if (dir == -1) dir = 2;

			if ((dir >= 0) && ((side == dir) || (side == Facing.oppositeSide[dir]))) {
				return getIcons()[1];
			}

			return getIcons()[0];
		}

		public void drawTile(IBlockAccess world, int x, int y, int z) {
			setWorld(world);

			Block block = ComputerCraft.Blocks.cable;
			setRenderBounds(0.375D, 0.375D, 0.375D, 0.625D, 0.625D, 0.625D);
			renderStandardBlock(block, x, y, z);

			if (BlockCable.isCable(world, x, y - 1, z)) {
				setRenderBounds(0.375D, 0.0D, 0.375D, 0.625D, 0.375D, 0.625D);
				renderStandardBlock(block, x, y, z);
			}
			if (BlockCable.isCable(world, x, y + 1, z)) {
				setRenderBounds(0.375D, 0.625D, 0.375D, 0.625D, 1.0D, 0.625D);
				renderStandardBlock(block, x, y, z);
			}
			if (BlockCable.isCable(world, x, y, z - 1)) {
				setRenderBounds(0.375D, 0.375D, 0.0D, 0.625D, 0.625D, 0.375D);
				renderStandardBlock(block, x, y, z);
			}
			if (BlockCable.isCable(world, x, y, z + 1)) {
				setRenderBounds(0.375D, 0.375D, 0.625D, 0.625D, 0.625D, 1.0D);
				renderStandardBlock(block, x, y, z);
			}
			if (BlockCable.isCable(world, x - 1, y, z)) {
				setRenderBounds(0.0D, 0.375D, 0.375D, 0.375D, 0.625D, 0.625D);
				renderStandardBlock(block, x, y, z);
			}
			if (BlockCable.isCable(world, x + 1, y, z)) {
				setRenderBounds(0.625D, 0.375D, 0.375D, 1.0D, 0.625D, 0.625D);
				renderStandardBlock(block, x, y, z);
			}

			block.setBlockBoundsBasedOnState(world, x, y, z);
		}
	}
}
