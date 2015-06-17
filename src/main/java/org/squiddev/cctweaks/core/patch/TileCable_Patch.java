package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.BlockCable;
import dan200.computercraft.shared.peripheral.modem.IReceiver;
import dan200.computercraft.shared.peripheral.modem.ModemPeripheral;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNodeHost;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.cctweaks.core.FmlEvents;
import org.squiddev.cctweaks.core.network.cable.SingleModemCable;
import org.squiddev.cctweaks.core.network.modem.BasicModem;
import org.squiddev.cctweaks.core.network.modem.DirectionalPeripheralModem;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.patcher.visitors.MergeVisitor;

import java.util.Objects;

import static org.squiddev.cctweaks.core.network.NetworkHelpers.canConnect;

@MergeVisitor.Rename(from = "dan200/computercraft/shared/peripheral/modem/TileCable$Packet", to = "org/squiddev/cctweaks/api/network/Packet")
public class TileCable_Patch extends TileCable implements IWorldNetworkNodeHost, IWorldPosition {
	public static final double MIN = 0.375;
	public static final double MAX = 1 - MIN;

	@MergeVisitor.Stub
	private boolean m_destroyed;
	@MergeVisitor.Stub
	private static IIcon[] s_cableIcons;
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
				public int getDirection() {
					return TileCable_Patch.this.getDirection();
				}

				@Override
				public IWorldPosition getPosition() {
					return TileCable_Patch.this;
				}

				@Override
				public boolean canConnect(ForgeDirection from) {
					return true;
				}

				@Override
				protected void setPeripheralEnabled(boolean peripheralEnabled) {
					super.setPeripheralEnabled(peripheralEnabled);

					// Required for OpenPeripheral's PeripheralProxy
					// https://github.com/OpenMods/OpenPeripheral-Addons/blob/master/src/main/java/openperipheral/addons/peripheralproxy/TileEntityPeripheralProxy.java#L23-L32
					m_peripheralAccessAllowed = peripheralEnabled;
				}

				@Override
				protected boolean isPeripheralEnabled() {
					return super.isPeripheralEnabled() && !m_destroyed && getPeripheralType() == PeripheralType.WiredModemWithCable;
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
				public boolean canConnect(ForgeDirection direction) {
					// Can't be visited by other nodes if it is destroyed
					if (m_destroyed) return false;

					// Or has no cable or is the side it is facing
					PeripheralType type = getPeripheralType();
					return type == PeripheralType.Cable || (type == PeripheralType.WiredModemWithCable && direction.ordinal() != getDirection());
				}
			};
		}
		return cable;
	}

	@Override
	public void destroy() {
		// TODO: Maybe on invalidate instead?
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
	public void validate() {
		// TODO: This is also called when being destroyed
		super.validate();
		if (!worldObj.isRemote) {
			FmlEvents.schedule(new Runnable() {
				@MergeVisitor.Rewrite
				protected boolean ANNOTATION;

				@Override
				public void run() {
					getCable().connect();
					if (lazyTag != null) {
						readLazyNBT(lazyTag);
						lazyTag = null;
					}
				}
			});
		}
	}

	@Override
	public void onNeighbourChange() {
		// Update the neighbour first as this might break the type
		nativeOnNeighbourChange();

		// TODO: Break the modem if we change
		if (getPeripheralType() == PeripheralType.WiredModemWithCable) {
			if (getModem().updateEnabled()) {
				modem.getAttachedNetwork().invalidateNetwork();
				updateAnim();
			}
		}
	}

	@MergeVisitor.Stub
	@MergeVisitor.Rename(from = {"onNeighbourChange"})
	public void nativeOnNeighbourChange() {
	}

	@Override
	public boolean onActivate(EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if ((getPeripheralType() == PeripheralType.WiredModemWithCable) && (!player.isSneaking())) {
			if (!worldObj.isRemote) {

				String oldPeriphName = getModem().getPeripheralName();
				getModem().toggleEnabled();
				String periphName = getModem().getPeripheralName();

				if (!Objects.equals(periphName, oldPeriphName)) {
					if (oldPeriphName != null) {
						player.addChatMessage(new ChatComponentTranslation("gui.computercraft:wired_modem.peripheral_disconnected", oldPeriphName));
					}
					if (periphName != null) {
						player.addChatMessage(new ChatComponentTranslation("gui.computercraft:wired_modem.peripheral_connected", periphName));
					}

					getModem().getAttachedNetwork().invalidateNetwork();
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
		if (worldObj == null) {
			lazyTag = tag;
		} else {
			readLazyNBT(tag);
		}
	}

	protected void readLazyNBT(NBTTagCompound tag) {
		getModem().setState(tag.getBoolean("peripheralAccess") ? BasicModem.MODEM_PERIPHERAL : 0);
		modem.id = tag.getInteger("peripheralID");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		if (lazyTag != null) {
			tag.setBoolean("peripheralAccess", tag.getBoolean("peripheralAccess"));
			tag.setInteger("peripheralID", tag.getInteger("peripheralID"));
		} else {
			tag.setBoolean("peripheralAccess", modem.isEnabled());
			tag.setInteger("peripheralID", modem.id);
		}
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
	public IPeripheral getPeripheral(int side) {
		return side == getDirection() ? getModem().modem : null;
	}

	@Override
	public void updateEntity() {
		// TODO: This should call TilePeripheralBase's updateEntity method instead
		super.updateEntity();
		if (worldObj.isRemote) return;

		if (getModem().modem.pollChanged()) updateAnim();
		modem.processQueue();
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
	public void transmit(int channel, int replyChannel, Object payload, double range, double xPos, double yPos, double zPos, Object senderObject) {
		getModem().transmit(channel, replyChannel, payload, range, xPos, yPos, zPos, senderObject);
	}

	@Deprecated
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
		if (!worldObj.isRemote) {
			getModem().invalidateNetwork();
		}
	}

	@Deprecated
	private void dispatchPacket(Packet packet) {
		getModem().transmitPacket(packet);
	}

	@Deprecated
	private void receivePacket(Packet packet, int distanceTravelled) {
		getModem().receivePacket(packet, distanceTravelled);
	}

	@Deprecated
	private void findPeripherals() {
		// TODO: Do we need to do something?
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
		int x = xCoord, y = yCoord, z = zCoord;
		IBlockAccess world = worldObj;

		return AxisAlignedBB.getBoundingBox(
			canConnect(world, x, y, z, ForgeDirection.WEST) ? 0 : MIN,
			canConnect(world, x, y, z, ForgeDirection.DOWN) ? 0 : MIN,
			canConnect(world, x, y, z, ForgeDirection.NORTH) ? 0 : MIN,
			canConnect(world, x, y, z, ForgeDirection.EAST) ? 1 : MAX,
			canConnect(world, x, y, z, ForgeDirection.UP) ? 1 : MAX,
			canConnect(world, x, y, z, ForgeDirection.SOUTH) ? 1 : MAX
		);
	}

	@Override
	public IIcon getTexture(int side) {
		PeripheralType type = getPeripheralType();
		if (BlockCable.renderAsModem) type = PeripheralType.WiredModem;

		switch (type) {
			case Cable:
			case WiredModemWithCable:
				int dir = -1;
				if (type == PeripheralType.WiredModemWithCable) {
					dir = getDirection();
					dir -= dir % 2;
				}

				int x = xCoord, y = yCoord, z = zCoord;
				IBlockAccess world = worldObj;

				if (canConnect(world, x, y, z, ForgeDirection.EAST) || canConnect(world, x, y, z, ForgeDirection.WEST)) {
					dir = dir == -1 || dir == 4 ? 4 : -2;
				}
				if (canConnect(world, x, y, z, ForgeDirection.UP) || canConnect(world, x, y, z, ForgeDirection.DOWN)) {
					dir = dir == -1 || dir == 0 ? 0 : -2;
				}
				if (canConnect(world, x, y, z, ForgeDirection.NORTH) || canConnect(world, x, y, z, ForgeDirection.SOUTH)) {
					dir = dir == -1 || dir == 2 ? 2 : -2;
				}

				if (dir == -1) dir = 2;

				if (dir >= 0 && (side == dir || side == Facing.oppositeSide[dir])) return s_cableIcons[1];
				return s_cableIcons[0];
		}

		return super.getTexture(side);
	}

	@Override
	public IWorldNetworkNode getNode() {
		return getCable();
	}

	@Override
	public IBlockAccess getWorld() {
		return worldObj;
	}

	@Override
	public int getX() {
		return xCoord;
	}

	@Override
	public int getY() {
		return yCoord;
	}

	@Override
	public int getZ() {
		return zCoord;
	}
}
