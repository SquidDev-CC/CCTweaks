package org.squiddev.cctweaks.core.integration.multipart;

import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.client.render.FixedRenderBlocks;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.BlockCable;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.NetworkHelpers;
import org.squiddev.cctweaks.api.network.NetworkVisitor;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CablePart extends AbstractPart implements INetworkNode {
	public static final String NAME = CCTweaks.NAME + ":networkCable";
	public static final double MIN = 0.375D;
	public static final double MAX = 1 - MIN;

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

	private final Object lock = new Object();

	@Override
	public String getType() {
		return NAME;
	}

	@Override
	public Iterable<Cuboid6> getOcclusionBoxes() {
		List<Cuboid6> parts = new ArrayList<Cuboid6>();
		parts.add(new Cuboid6(MIN, MIN, MIN, MAX, MAX, MAX));

		if (tile() != null) {
			int x = x(), y = y(), z = z();
			World world = world();

			if (BlockCable.isCable(world, x - 1, y, z)) {
				parts.add(new Cuboid6(0, MIN, MIN, MIN, MAX, MAX));
			}
			if (BlockCable.isCable(world, x + 1, y, z)) {
				parts.add(new Cuboid6(MAX, MIN, MIN, 1, MAX, MAX));
			}

			if (BlockCable.isCable(world, x, y - 1, z)) {
				parts.add(new Cuboid6(MIN, 0, MIN, MAX, MIN, MAX));
			}
			if (BlockCable.isCable(world, x, y + 1, z)) {
				parts.add(new Cuboid6(MIN, MAX, MIN, MAX, 1, MAX));
			}
			if (BlockCable.isCable(world, x, y, z - 1)) {
				parts.add(new Cuboid6(MIN, MIN, 0, MAX, MAX, MIN));
			}
			if (BlockCable.isCable(world, x, y, z + 1)) {
				parts.add(new Cuboid6(MIN, MIN, MAX, MAX, MAX, 1));
			}
		}

		return parts;
	}

	@Override
	public Cuboid6 getBounds() {
		double xMin = MIN;
		double yMin = MIN;
		double zMin = MIN;
		double xMax = MAX;
		double yMax = MAX;
		double zMax = MAX;

		if (tile() != null) {
			int x = x(), y = y(), z = z();
			World world = world();

			if (BlockCable.isCable(world, x - 1, y, z)) xMin = 0.0D;
			if (BlockCable.isCable(world, x + 1, y, z)) xMax = 1.0D;
			if (BlockCable.isCable(world, x, y - 1, z)) yMin = 0.0D;
			if (BlockCable.isCable(world, x, y + 1, z)) yMax = 1.0D;
			if (BlockCable.isCable(world, x, y, z - 1)) zMin = 0.0D;
			if (BlockCable.isCable(world, x, y, z + 1)) zMax = 1.0D;
		}

		return new Cuboid6(xMin, yMin, zMin, xMax, yMax, zMax);
	}

	@Override
	public void harvest(MovingObjectPosition hit, EntityPlayer player) {
		World world = world();
		int x = x(), y = y(), z = z();

		super.harvest(hit, player);

		if (!world.isRemote) {
			NetworkHelpers.fireNetworkChanged(world, x, y, z);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getBrokenIcon(int side) {
		return ComputerCraft.Blocks.cable.getIcon(0, 0);
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

	@Override
	public ItemStack pickItem(MovingObjectPosition hit) {
		return PeripheralItemFactory.create(PeripheralType.Cable, null, 1);
	}

	@Override
	public boolean canVisit() {
		return true;
	}

	@Override
	public Map<String, IPeripheral> getConnectedPeripherals() {
		return null;
	}

	@Override
	public void receivePacket(Packet packet, int distanceTravelled) {
	}

	@Override
	public void invalidateNetwork() {
	}

	@Override
	public void networkChanged() {
		if (!world().isRemote) {
			NetworkHelpers.fireNetworkInvalidate(world(), x(), y(), z());
		}
	}

	@Override
	public NetworkVisitor.SearchLoc[] getExtraNodes() {
		return null;
	}

	@Override
	public Object lock() {
		return lock;
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
