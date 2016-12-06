package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.modem.IReceiver;
import dan200.computercraft.shared.peripheral.modem.ModemPeripheral;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.*;
import org.squiddev.cctweaks.core.McEvents;
import org.squiddev.cctweaks.core.network.cable.SingleModemCable;
import org.squiddev.cctweaks.core.network.modem.DirectionalPeripheralModem;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.cctweaks.core.utils.Helpers;
import org.squiddev.patcher.visitors.MergeVisitor;

@MergeVisitor.Rename(from = "dan200/computercraft/shared/peripheral/modem/TileCable$Packet", to = "org/squiddev/cctweaks/api/network/Packet")
public class TileCable_Patch extends TileCable_Ignore implements IWorldNetworkNodeHost, IWorldPosition {

	public static final double MIN = 0.375;
	public static final double MAX = 1 - MIN;

	@MergeVisitor.Stub
	private boolean m_destroyed;
	@MergeVisitor.Stub
	private boolean m_peripheralAccessAllowed;

	protected DirectionalPeripheralModem modem;
	protected SingleModemCable cable;
	protected NBTTagCompound lazyTag;

	/**
	 * The patcher doesn't enable constructors (yet) so we lazy load the modem
	 *
	 * @return The resulting modem
	 */
	public DirectionalPeripheralModem getModem() {
		if (modem == null) {
			return modem = new DirectionalPeripheralModem() {
				@MergeVisitor.Rewrite
				protected boolean ANNOTATION;

				@Override
				public EnumFacing getDirection() {
					return TileCable_Patch.this.getDirection();
				}

				@Override
				public IWorldPosition getPosition() {
					return TileCable_Patch.this;
				}

				@Override
				public boolean canConnect(EnumFacing from) {
					return true;
				}

				@Override
				public void setPeripheralEnabled(boolean peripheralEnabled) {
					super.setPeripheralEnabled(peripheralEnabled);

					// Required for OpenPeripheral's PeripheralProxy
					// https://github.com/OpenMods/OpenPeripheral-Addons/blob/master/src/main/java/openperipheral/addons/peripheralproxy/TileEntityPeripheralProxy.java#L23-L32
					m_peripheralAccessAllowed = peripheralEnabled;
				}

				@Override
				protected boolean isPeripheralEnabled() {
					return super.isPeripheralEnabled() && !m_destroyed && getPeripheralTypeSafe() == PeripheralType.WiredModemWithCable;
				}
			};
		}
		return modem;
	}

	public SingleModemCable getCable() {
		if (cable == null) {
			return cable = new SingleModemCable() {
				@MergeVisitor.Rewrite
				protected boolean ANNOTATION;

				@Override
				public DirectionalPeripheralModem getModem() {
					return TileCable_Patch.this.getModem();
				}

				@Override
				public IWorldPosition getPosition() {
					return TileCable_Patch.this;
				}

				@Override
				public boolean canConnect(EnumFacing direction) {
					// Can't be visited by other nodes if it is destroyed
					if (m_destroyed || worldObj == null) return false;

					// Or has no cable or is the side it is facing
					PeripheralType type = getPeripheralTypeSafe();
					return type == PeripheralType.Cable || (type == PeripheralType.WiredModemWithCable && direction != getDirection());
				}
			};
		}
		return cable;
	}

	@Override
	public void destroy() {
		if (!m_destroyed) {
			m_destroyed = true;
			getModem().destroy();
			getCable().destroy();
		}
		super.destroy();
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		destroy();
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	@Override
	public void validate() {
		super.validate();
		if (!worldObj.isRemote) {
			McEvents.schedule(new Runnable() {
				@MergeVisitor.Rewrite
				protected boolean ANNOTATION;

				@Override
				public void run() {
					// In case the tile is removed within this tick
					if (worldObj != null && worldObj.getBlockState(pos).getBlock() == ComputerCraft.Blocks.cable) {
						getCable().connect();
						if (lazyTag != null) {
							readLazyNBT(lazyTag);
							lazyTag = null;
						}
					}
				}
			});
		}
	}

	@Override
	public void onNeighbourChange() {
		// Update the neighbour first as this might break the type
		PeripheralType type = getPeripheralTypeSafe();
		nativeOnNeighbourChange();

		// TODO: Break the modem if we change
		if (type == PeripheralType.WiredModemWithCable) {
			if (getModem().updateEnabled()) {
				INetworkController controller = getModem().getAttachedNetwork();
				if (controller != null) controller.invalidateNode(modem);
				updateAnim();
			}
		}
	}

	@MergeVisitor.Stub
	@MergeVisitor.Rename(from = {"onNeighbourChange"})
	public void nativeOnNeighbourChange() {
	}

	private PeripheralType getPeripheralTypeSafe() {
		if (worldObj == null) return null;

		IBlockState state = worldObj.getBlockState(pos);
		if (state.getBlock() != ComputerCraft.Blocks.cable) return null;

		return ComputerCraft.Blocks.cable.getPeripheralType(state);
	}

	@Override
	public boolean onActivate(EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (getPeripheralTypeSafe() == PeripheralType.WiredModemWithCable && !player.isSneaking()) {
			if (!worldObj.isRemote) {

				String oldPeriphName = getModem().getPeripheralName();
				getModem().toggleEnabled();
				String periphName = getModem().getPeripheralName();

				if (!Helpers.equals(periphName, oldPeriphName)) {
					if (oldPeriphName != null) {
						player.addChatMessage(new TextComponentTranslation("gui.computercraft:wired_modem.peripheral_disconnected", oldPeriphName));
					}
					if (periphName != null) {
						player.addChatMessage(new TextComponentTranslation("gui.computercraft:wired_modem.peripheral_connected", periphName));
					}

					INetworkController controller = getModem().getAttachedNetwork();
					if (controller != null) controller.invalidateNode(modem);
					updateAnim();
					return true;
				}
			} else {
				return true;
			}
		}
		return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		getModem().id = tag.getInteger("peripheralID");
		if (worldObj == null) {
			lazyTag = tag;
		} else {
			readLazyNBT(tag);
		}
	}

	protected void readLazyNBT(NBTTagCompound tag) {
		getModem().setPeripheralEnabled(tag.getBoolean("peripheralAccess"));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		tag = super.writeToNBT(tag);
		if (lazyTag != null) {
			tag.setBoolean("peripheralAccess", tag.getBoolean("peripheralAccess"));
			tag.setInteger("peripheralID", tag.getInteger("peripheralID"));
		} else {
			tag.setBoolean("peripheralAccess", modem.isEnabled());
			tag.setInteger("peripheralID", modem.id);
		}
		return tag;
	}

	public void writeDescription(NBTTagCompound tag) {
		// Manually refresh the state to ensure modems display correctly.
		updateAnim();
		super.writeDescription(tag);
	}

	@Override
	protected ModemPeripheral createPeripheral() {
		return getModem().modem;
	}

	@Override
	protected void updateAnim() {
		getModem().refreshState();
		setAnim(modem.state);
	}

	@Override
	public IPeripheral getPeripheral(EnumFacing side) {
		if (side != getDirection()) return null;

		PeripheralType type = getPeripheralTypeSafe();
		if (type == PeripheralType.WiredModem || type == PeripheralType.WiredModemWithCable) {
			return getModem().modem;
		} else {
			return null;
		}
	}

	@Override
	public void update() {
		super.update();
		if (worldObj.isRemote) return;

		if (getModem().modem.pollChanged()) updateAnim();
	}

	@Override
	public void addReceiver(IReceiver receiver) {
		getModem().addReceiver(receiver);
	}

	@Override
	public void removeReceiver(IReceiver receiver) {
		getModem().removeReceiver(receiver);
	}

	@Override
	public void transmit(int channel, int replyChannel, Object payload, World world, Vec3d pos, double range, boolean interdimensional, Object senderObject) {
		getModem().transmit(channel, replyChannel, payload, world, pos, range, interdimensional, senderObject);
	}

	private void attachPeripheral(String name, IPeripheral peripheral) {
		getModem().attachPeripheral(name, peripheral);
	}

	@Deprecated
	private void detachPeripheral(String name) {
		getModem().detachPeripheral(name);
	}

	@Deprecated
	private String getTypeRemote(String remoteName) {
		return null;
	}

	@Deprecated
	private String[] getMethodNamesRemote(String remoteName) {
		return null;
	}

	@Deprecated
	private Object[] callMethodRemote(String remoteName, ILuaContext context, String method, Object[] arguments) {
		return null;
	}

	@Override
	public void networkChanged() {
		getCable().updateConnections();
		if (!worldObj.isRemote) {
			INetworkController controller = getModem().getAttachedNetwork();
			if (controller != null) controller.invalidateNode(modem);
		}
	}

	@Deprecated
	private void dispatchPacket(Packet packet) {
		INetworkController controller = getModem().getAttachedNetwork();
		if (controller != null) {
			controller.transmitPacket(modem, packet);
		} else {
			DebugLogger.warn("Discarding packet as not connected to a network");
		}
	}

	@Deprecated
	private void receivePacket(Packet packet, int distanceTravelled) {
		getModem().receivePacket(packet, distanceTravelled);
	}

	@Deprecated
	private void findPeripherals() {
		DebugLogger.deprecated("Handled by BasicModem");
	}

	@Override
	public void togglePeripheralAccess() {
		// This is needed for OpenPeripherals' peripheral proxy
		// See https://github.com/OpenMods/OpenPeripheral-Addons/blob/master/src/main/java/openperipheral/addons/peripheralproxy/TileEntityPeripheralProxy.java#L23-L32
		getModem().toggleEnabled();
	}

	@Override
	@Deprecated
	public String getConnectedPeripheralName() {
		return getModem().getPeripheralName();
	}

	@Deprecated
	private IPeripheral getConnectedPeripheral() {
		return getModem().isEnabled() ? modem.getPeripheral() : null;
	}

	@Override
	protected boolean isAttached() {
		return getModem().modem.getComputer() != null;
	}

	@Override
	public AxisAlignedBB getCableBounds() {
		BlockPos pos = this.pos;
		IBlockAccess world = worldObj;

		INetworkHelpers helpers = NetworkAPI.helpers();
		return new AxisAlignedBB(
			helpers.canConnect(world, pos, EnumFacing.WEST) ? 0 : MIN,
			helpers.canConnect(world, pos, EnumFacing.DOWN) ? 0 : MIN,
			helpers.canConnect(world, pos, EnumFacing.NORTH) ? 0 : MIN,
			helpers.canConnect(world, pos, EnumFacing.EAST) ? 1 : MAX,
			helpers.canConnect(world, pos, EnumFacing.UP) ? 1 : MAX,
			helpers.canConnect(world, pos, EnumFacing.SOUTH) ? 1 : MAX
		);
	}

	@Override
	public IWorldNetworkNode getNode() {
		return getCable();
	}

	@Override
	public IBlockAccess getBlockAccess() {
		return worldObj;
	}

	@Override
	public BlockPos getPosition() {
		return pos;
	}
}
