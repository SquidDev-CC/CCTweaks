package org.squiddev.cctweaks.core.integration.multipart;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.TSlottedPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.client.proxy.ComputerCraftProxyClient;
import dan200.computercraft.client.render.FixedRenderBlocks;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
import dan200.computercraft.shared.peripheral.modem.ModemPeripheral;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import dan200.computercraft.shared.peripheral.modem.TileModemBase;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.NetworkHelpers;
import org.squiddev.cctweaks.api.network.NetworkVisitor;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.cctweaks.core.utils.ComputerAccessor;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

public class ModemPart extends AbstractPart implements INetworkNode, TSlottedPart {
	private static IIcon[] icons;

	public static final String NAME = CCTweaks.NAME + ":networkModem";

	protected int direction = 0;
	protected ModemPeripheral modem;

	public ModemPart() {
	}

	public ModemPart(int direction, ModemPeripheral modem) {
		this.direction = direction;
		this.modem = modem;
	}

	public ModemPart(TileModemBase modem) {
		this.direction = modem.getDirection();

		try {
			this.modem = (ModemPeripheral) ComputerAccessor.modemBaseModem.get(modem);
		} catch (Exception e) {
			DebugLogger.error("Cannot get modem from tile");
			e.printStackTrace();
		}
	}

	@SideOnly(Side.CLIENT)
	private ModemRenderer render;

	@SideOnly(Side.CLIENT)
	public ModemRenderer getRender() {
		ModemRenderer draw = render;
		if (draw == null) {
			draw = render = new ModemRenderer();
		}
		return draw;
	}

	private final Object lock = new Object();

	@Override
	public String getType() {
		return NAME;
	}

	@Override
	public int getSlotMask() {
		return 1 << direction;
	}

	@Override
	public Iterable<Cuboid6> getOcclusionBoxes() {
		return Collections.singletonList(getBounds());
	}

	@Override
	public Iterable<IndexedCuboid6> getSubParts() {
		return Collections.singletonList(new IndexedCuboid6(direction, getBounds()));
	}

	@Override
	public Cuboid6 getBounds() {
		switch (direction) {
			case 0:
			default:
				return new Cuboid6(0.125D, 0.0D, 0.125D, 0.875D, 0.1875D, 0.875D);
			case 1:
				return new Cuboid6(0.125D, 0.8125D, 0.125D, 0.875D, 1.0D, 0.875D);
			case 2:
				return new Cuboid6(0.125D, 0.125D, 0.0D, 0.875D, 0.875D, 0.1875D);
			case 3:
				return new Cuboid6(0.125D, 0.125D, 0.8125D, 0.875D, 0.875D, 1.0D);
			case 4:
				return new Cuboid6(0.0D, 0.125D, 0.125D, 0.1875D, 0.875D, 0.875D);
			case 5:
				return new Cuboid6(0.8125D, 0.125D, 0.125D, 1.0D, 0.875D, 0.875D);
		}
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
		TextureUtils.bindAtlas(0);
		getRender().drawTile(world(), x(), y(), z());
		return true;
	}

	@Override
	public ItemStack pickItem(MovingObjectPosition hit) {
		return PeripheralItemFactory.create(PeripheralType.WiredModem, null, 1);
	}

	@Override
	public void writeDesc(MCDataOutput packet) {
		packet.writeByte(direction);
		packet.writeBoolean(modem != null && modem.isActive());
	}

	@Override
	public void readDesc(MCDataInput packet) {
		direction = packet.readByte();
		boolean active = packet.readBoolean();
	}

	@Override
	public void save(NBTTagCompound tag) {
		tag.setByte("direction", (byte) direction);
		tag.setBoolean("active", modem != null && modem.isActive());
	}

	@Override
	public void load(NBTTagCompound tag) {
		direction = tag.getByte("direction");
		boolean active = tag.getBoolean("active");
	}

	@Override
	public boolean canBeVisited(ForgeDirection from) {
		return true;
	}

	@Override
	public boolean canVisitTo(ForgeDirection to) {
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

	public class ModemRenderer extends FixedRenderBlocks {
		public IIcon[] getIcons() {
			IIcon[] icons;
			if ((icons = ModemPart.icons) == null) {

				try {
					Field field = TileCable.class.getDeclaredField("s_modemIcons");
					field.setAccessible(true);
					icons = (IIcon[]) field.get(null);
				} catch (ReflectiveOperationException e) {
					DebugLogger.error("Cannot find TileCable texture");
					e.printStackTrace();
					icons = new IIcon[8];
				}
				ModemPart.icons = icons;
			}

			return icons;
		}

		@Override
		public IIcon getBlockIcon(Block block, IBlockAccess world, int x, int y, int z, int side) {
			int tex = modem != null && modem.isActive() ? 2 : 0;
			int dir = direction;

			IIcon[] icons = getIcons();

			if (dir == 0 || dir == 1 || side == Facing.oppositeSide[dir]) return icons[tex];
			if (side == 2 || side == 5) return icons[tex + 1];
			return icons[tex];
		}

		public void drawTile(IBlockAccess world, int x, int y, int z) {
			setWorld(world);

			Block block = ComputerCraft.Blocks.cable;
			Cuboid6 bounds = getBounds();
			setRenderBounds(bounds.min.x, bounds.min.y, bounds.min.z, bounds.max.x, bounds.max.y, bounds.max.z);
			renderStandardBlock(block, x, y, z);
		}
	}
}
