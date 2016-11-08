package org.squiddev.cctweaks.integration.multipart.network;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import mcmultipart.MCMultiPartMod;
import mcmultipart.multipart.INormallyOccludingPart;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNodeHost;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHost;
import org.squiddev.cctweaks.core.network.modem.DirectionalPeripheralModem;
import org.squiddev.cctweaks.core.network.modem.SinglePeripheralModem;
import org.squiddev.cctweaks.core.utils.ComputerAccessor;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.cctweaks.core.utils.Helpers;
import org.squiddev.cctweaks.integration.multipart.PartSided;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class PartModem extends PartSided implements IWorldNetworkNodeHost, IPeripheralHost, ITickable, INormallyOccludingPart {
	private static final PropertyEnum<ModemType> MODEM = PropertyEnum.create("modem", ModemType.class);

	private enum ModemType implements IStringSerializable {
		OFF,
		ON,
		OFF_PERIPHERAL,
		ON_PERIPHERAL;

		private final String name;

		ModemType() {
			name = name().toLowerCase();
		}

		@Nonnull
		@Override
		public String getName() {
			return name;
		}


		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * Occlusion for collision detection
	 */
	private static final AxisAlignedBB[] OCCLUSION = new AxisAlignedBB[]{
		new AxisAlignedBB(0.125, 0.0, 0.125, 0.875, 0.1875, 0.875),
		new AxisAlignedBB(0.125, 0.8125, 0.125, 0.875, 1.0, 0.875),
		new AxisAlignedBB(0.125, 0.125, 0.0, 0.875, 0.875, 0.1875),
		new AxisAlignedBB(0.125, 0.125, 0.8125, 0.875, 0.875, 1.0),
		new AxisAlignedBB(0.0, 0.125, 0.125, 0.1875, 0.875, 0.875),
		new AxisAlignedBB(0.8125, 0.125, 0.125, 1.0, 0.875, 0.875),
	};

	/**
	 * Slightly smaller bounds
	 */
	private static final AxisAlignedBB[] BOUNDS = new AxisAlignedBB[]{
		new AxisAlignedBB(0.125, 0.0, 0.125, 0.875, 0.125, 0.875D),
		new AxisAlignedBB(0.125, 0.875, 0.125, 0.875, 1.0, 0.875D),
		new AxisAlignedBB(0.125, 0.125, 0.0, 0.875, 0.875, 0.125D),
		new AxisAlignedBB(0.125, 0.125, 0.875, 0.875, 0.875, 1.0D),
		new AxisAlignedBB(0.0, 0.125, 0.125, 0.125, 0.875, 0.875D),
		new AxisAlignedBB(0.875, 0.125, 0.125, 1.0, 0.875, 0.875D),
	};

	public PartModem() {
		this(EnumFacing.NORTH);
	}

	public PartModem(EnumFacing facing) {
		setSide(facing);
	}

	public PartModem(TileCable modem) {
		this(modem.getDirection());
		try {
			SinglePeripheralModem peripheralModem = (SinglePeripheralModem) ComputerAccessor.cableModem.get(modem);

			this.modem.id = peripheralModem.id;
			this.modem.setPeripheralEnabled(peripheralModem.isEnabled());

			// TODO: Can we keep parts of the original node?
		} catch (Exception e) {
			DebugLogger.error("Cannot get modem from tile", e);
		}
	}

	public final WiredModem modem = new WiredModem();

	//region Basic getters
	@Override
	public IWorldNetworkNode getNode() {
		return modem;
	}

	@Override
	public IPeripheral getPeripheral(EnumFacing side) {
		return side == getSide() ? modem.modem : null;
	}

	@Override
	public Block getBlock() {
		return ComputerCraft.Blocks.cable;
	}

	@Override
	public ItemStack getStack() {
		return PeripheralItemFactory.create(PeripheralType.WiredModem, null, 1);
	}

	@Override
	public ResourceLocation getModelPath() {
		return new ResourceLocation(CCTweaks.ID, "modem");
	}

	@Override
	public void addSelectionBoxes(List<AxisAlignedBB> list) {
		list.add(OCCLUSION[getSide().ordinal()]);
	}

	@Override
	public void addCollisionBoxes(AxisAlignedBB mask, List<net.minecraft.util.math.AxisAlignedBB> list, Entity collidingEntity) {
		AxisAlignedBB bounds = OCCLUSION[getSide().ordinal()];
		if (bounds.intersectsWith(mask)) list.add(bounds);
	}

	@Override
	public void addOcclusionBoxes(List<AxisAlignedBB> list) {
		list.add(BOUNDS[getSide().ordinal()]);
	}

	@Override
	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(MCMultiPartMod.multipart, SIDE, MODEM);
	}

	@Override
	@SuppressWarnings("deprecation")
	public IBlockState getActualState(IBlockState state) {
		return super.getActualState(state)
			.withProperty(MODEM, ModemType.values()[modem.state]);
	}
	//endregion

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setBoolean("modem_enabled", modem.isEnabled());
		tag.setInteger("modem_id", modem.id);
		return tag;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		modem.id = tag.getInteger("modem_id");
	}

	@Override
	public Iterable<String> getFields() {
		return Collections.singletonList("modem_enabled");
	}

	@Override
	public void readLazyNBT(NBTTagCompound tag) {
		modem.setPeripheralEnabled(tag.getBoolean("modem_enabled"));
	}

	@Override
	public void writeUpdatePacket(PacketBuffer buf) {
		super.writeUpdatePacket(buf);
		buf.writeByte(modem.state);
	}

	@Override
	public void readUpdatePacket(PacketBuffer buf) {
		super.readUpdatePacket(buf);
		modem.setState(buf.readByte());
	}

	@Override
	public void onUnloaded() {
		if (!getWorld().isRemote) modem.destroy();
		super.onUnloaded();
	}

	@Override
	public void onRemoved() {
		if (!getWorld().isRemote) modem.destroy();
		super.onRemoved();
	}

	@Override
	public void onNeighborBlockChange(Block block) {
		if (modem.updateEnabled()) {
			refreshPart();
			modem.getAttachedNetwork().invalidateNode(modem);
		}
	}

	@Override
	public void onNeighborTileChange(EnumFacing facing) {
		if (facing == getSide() && modem.updateEnabled()) {
			refreshPart();
			modem.getAttachedNetwork().invalidateNode(modem);
		}
	}

	private void refreshPart() {
		modem.refreshState();
		markDirty();
		sendUpdatePacket();
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack stack, PartMOP hit) {
		if (player.isSneaking()) return false;
		if (getWorld().isRemote) return true;
		if (modem.getAttachedNetwork() == null) return false;

		String name = modem.getPeripheralName();

		modem.toggleEnabled();

		String newName = modem.getPeripheralName();

		if (!Helpers.equals(name, newName)) {
			if (name != null) {
				player.addChatMessage(new TextComponentTranslation("gui.computercraft:wired_modem.peripheral_disconnected", name));
			}

			if (newName != null) {
				player.addChatMessage(new TextComponentTranslation("gui.computercraft:wired_modem.peripheral_connected", newName));
			}

			modem.getAttachedNetwork().invalidateNode(modem);
			refreshPart();
		}

		return true;
	}

	@Override
	public void update() {
		if (getWorld().isRemote) return;
		if (modem.modem.pollChanged()) refreshPart();
	}

	public class WiredModem extends DirectionalPeripheralModem {
		@Override
		public IWorldPosition getPosition() {
			return PartModem.this;
		}

		@Override
		public EnumFacing getDirection() {
			return getSide();
		}
	}
}
