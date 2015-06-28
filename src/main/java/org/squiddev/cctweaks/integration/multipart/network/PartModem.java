package org.squiddev.cctweaks.integration.multipart.network;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import com.google.common.base.Objects;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.client.render.FixedRenderBlocks;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHost;
import org.squiddev.cctweaks.core.network.modem.BasicModem;
import org.squiddev.cctweaks.core.network.modem.DirectionalPeripheralModem;
import org.squiddev.cctweaks.core.utils.ComputerAccessor;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.lang.reflect.Field;
import java.util.Collections;

public class PartModem extends PartSidedNetwork implements IPeripheralHost {
	@SideOnly(Side.CLIENT)
	private static IIcon[] icons;

	public static final String NAME = CCTweaks.NAME + ":networkModem";

	protected WiredModem modem = new WiredModem();

	public PartModem() {
	}

	public PartModem(int direction) {
		this.direction = (byte) direction;
	}

	public PartModem(TileCable modem) {
		try {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setByte("node_direction", (byte) modem.getDirection());
			tag.setInteger("modem_id", ComputerAccessor.cablePeripheralId.getInt(modem));
			tag.setBoolean("modem_enabled", (modem.getAnim() & BasicModem.MODEM_PERIPHERAL) == BasicModem.MODEM_PERIPHERAL);
			load(tag);
		} catch (Exception e) {
			DebugLogger.error("Cannot get modem from tile", e);
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

	@Override
	public String getType() {
		return NAME;
	}

	@Override
	public void harvest(MovingObjectPosition hit, EntityPlayer player) {
		World world = world();

		super.harvest(hit, player);

		if (!world.isRemote) {
			modem.destroy();
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
	public ItemStack getItem() {
		return PeripheralItemFactory.create(PeripheralType.WiredModem, null, 1);
	}

	@Override
	public void update() {
		if (world().isRemote) return;

		if (modem.modem.pollChanged()) markDirty();
	}

	@Override
	public boolean doesTick() {
		return true;
	}

	@Override
	public void onNeighborChanged() {
		if (modem.updateEnabled()) {
			markDirty();
			modem.getAttachedNetwork().invalidateNode(modem);
		}
	}

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack item) {
		if (player.isSneaking()) return false;
		if (world().isRemote) return true;
		if (modem.getAttachedNetwork() == null) return false;

		String name = modem.getPeripheralName();

		modem.toggleEnabled();

		String newName = modem.getPeripheralName();

		if (!Objects.equal(name, newName)) {
			if (name != null) {
				player.addChatMessage(new ChatComponentTranslation("gui.computercraft:wired_modem.peripheral_disconnected", name));
			}

			if (newName != null) {
				player.addChatMessage(new ChatComponentTranslation("gui.computercraft:wired_modem.peripheral_connected", newName));
			}

			modem.getAttachedNetwork().invalidateNode(modem);
			markDirty();
		}

		return true;
	}

	@Override
	public void onWorldSeparate() {
		super.onWorldSeparate();
		modem.destroy();
	}

	/**
	 * Marks the modem as dirty to trigger a block update and client sync
	 */
	protected void markDirty() {
		modem.refreshState();
		tile().notifyPartChange(this);
		tile().markDirty();
		sendDescUpdate();
	}

	@Override
	public void writeDesc(MCDataOutput packet) {
		packet.writeByte(direction);
		packet.writeByte(modem.state);
	}

	@Override
	public void readDesc(MCDataInput packet) {
		direction = packet.readByte();
		modem.setState(packet.readByte());
	}

	@Override
	public void save(NBTTagCompound tag) {
		super.save(tag);
		tag.setBoolean("modem_enabled", modem.isEnabled());
		tag.setInteger("modem_id", modem.id);
	}

	@Override
	public Iterable<String> getFields() {
		return Collections.singletonList("modem_enabled");
	}

	@Override
	public void load(NBTTagCompound tag) {
		super.load(tag);
		modem.id = tag.getInteger("modem_id");
	}

	@Override
	public void loadLazy(NBTTagCompound tag) {
		modem.setPeripheralEnabled(tag.getBoolean("modem_enabled"));
	}

	@Override
	public IPeripheral getPeripheral(int side) {
		if (side == direction) return modem.modem;
		return null;
	}

	@Override
	public IWorldNetworkNode getNode() {
		return modem;
	}

	@SideOnly(Side.CLIENT)
	public class ModemRenderer extends FixedRenderBlocks {
		public IIcon[] getIcons() {
			IIcon[] icons;
			if ((icons = PartModem.icons) == null) {

				try {
					Field field = TileCable.class.getDeclaredField("s_modemIcons");
					field.setAccessible(true);
					icons = (IIcon[]) field.get(null);
				} catch (ReflectiveOperationException e) {
					DebugLogger.error("Cannot find TileCable texture", e);
					icons = new IIcon[8];
				}
				PartModem.icons = icons;
			}

			return icons;
		}

		@Override
		public IIcon getBlockIcon(Block block, IBlockAccess world, int x, int y, int z, int side) {
			int dir = direction;
			int texture = modem.state * 2;

			IIcon[] icons = getIcons();

			if (side == dir) {
				// Use the dark side for the peripheral side
				return icons[texture + 1];
			} else if (dir == 0 || dir == 1 || side == Facing.oppositeSide[dir]) {
				// Use the cable texture for the cable side or if the modem
				// is facing up/down to prevent textures being on the wrong side
				return icons[texture];
			} else if (side == 2 || side == 5) {
				// If the side is north/east use the side texture to prevent
				// the dark line being on the wrong side
				return icons[texture + 1];
			}

			return icons[texture];
		}

		public void drawTile(IBlockAccess world, int x, int y, int z) {
			setWorld(world);

			Block block = ComputerCraft.Blocks.cable;
			Cuboid6 bounds = getBounds();
			setRenderBounds(bounds.min.x, bounds.min.y, bounds.min.z, bounds.max.x, bounds.max.y, bounds.max.z);
			renderStandardBlock(block, x, y, z);
		}
	}

	public class WiredModem extends DirectionalPeripheralModem {
		@Override
		public IWorldPosition getPosition() {
			return PartModem.this;
		}

		@Override
		public int getDirection() {
			return direction;
		}
	}
}
