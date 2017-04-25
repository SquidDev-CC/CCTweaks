package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.network.ComputerCraftPacket;
import dan200.computercraft.shared.util.NBTUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import org.squiddev.cctweaks.api.IContainerComputer;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.patch.iface.IExtendedServerComputer;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.cctweaks.lua.lib.LuaEnvironment;
import org.squiddev.cctweaks.lua.lib.ReadOnlyFileMount;
import org.squiddev.cctweaks.lua.patch.ComputerThread_Rewrite;
import org.squiddev.cctweaks.lua.patch.Computer_Patch;
import org.squiddev.cctweaks.lua.patch.iface.IComputerEnvironmentExtended;
import org.squiddev.patcher.visitors.MergeVisitor;

import java.io.File;

/**
 * - Adds {@link IComputerEnvironmentExtended} and suspending events on timeout
 * - Adds isMostlyOn for detecting when a computer is on or starting up
 * - Various network changes
 */
@MergeVisitor.Rename(
	from = {
		"org/squiddev/cctweaks/lua/patch/Computer_Patch",
		"org/squiddev/cctweaks/lua/patch/ComputerThread_Rewrite",
		"org/squiddev/cctweaks/core/patch/Terminal_Patch"
	},
	to = {
		"dan200/computercraft/core/computer/Computer",
		"dan200/computercraft/core/computer/ComputerThread",
		"dan200/computercraft/core/terminal/Terminal"
	}
)
public class ServerComputer_Patch extends ServerComputer implements IComputerEnvironmentExtended, org.squiddev.cctweaks.api.computer.IExtendedServerComputer, IExtendedServerComputer {
	private static final int TIMEOUT = 100;

	private boolean suspendable;

	@MergeVisitor.Stub
	private int m_ticksSincePing = 0;

	@MergeVisitor.Stub
	private World m_world;

	@MergeVisitor.Stub
	private BlockPos m_position;

	@MergeVisitor.Stub
	private Computer_Patch m_computer;

	@MergeVisitor.Stub
	private NBTTagCompound m_userData;

	@MergeVisitor.Stub
	public ServerComputer_Patch() {
		super(null, -1, null, -1, null, -1, -1);
	}

	@Override
	public boolean isMostlyOn() {
		return m_computer.isMostlyOn();
	}

	public void keepAlive() {
		boolean resume = m_ticksSincePing > TIMEOUT && isSuspendable();
		m_ticksSincePing = 0;
		if (resume) ComputerThread_Rewrite.resumeComputer(m_computer);
	}

	@Override
	public boolean hasTimedOut() {
		return !isSuspendable() && m_ticksSincePing > TIMEOUT;
	}

	@Override
	public boolean suspendEvents() {
		return isSuspendable() && m_ticksSincePing > TIMEOUT;
	}

	public void setSuspendable() {
		suspendable = true;
	}

	private boolean isSuspendable() {
		return Config.Computer.suspendInactive && suspendable;
	}

	@Override
	public void setCustomRom(int diskID) {
		if (diskID < 0 || !Config.Computer.CustomRom.enabled) return;

		IMount mount = new ReadOnlyFileMount(new File(ComputerCraft.getWorldDir(getWorld()), "computer/disk/" + diskID));
		m_computer.setRomMount(LuaEnvironment.getPreBios(), mount);
	}

	@Override
	public void handlePacket(ComputerCraftPacket packet, EntityPlayer sender) {
		// Allow Computer/Tile updates as they may happen at any time.
		if (Config.Packets.requireContainer && packet.m_packetType != ComputerCraftPacket.RequestComputerUpdate && packet.m_packetType != ComputerCraftPacket.RequestTileEntityUpdate) {
			if (sender == null) {
				DebugLogger.warn("Attempt to interact with computer #" + getInstanceID() + " at position " + getPosition() + " with no player");
				return;
			}

			Container container = sender.openContainer;
			if (!(container instanceof IContainerComputer)) {
				DebugLogger.warn("Attempt to interact with computer #" + getInstanceID() + " at position " + getPosition() + " with no container (player " + sender + ") (" + packet.m_packetType + ")");
				DebugLogger.warn("Container is " + container);
				return;
			}

			IComputer computer = ((IContainerComputer) container).getComputer();
			if (computer != this) {
				DebugLogger.warn("Attempt to interact with computer #" + getInstanceID() + " at position " + getPosition() + " with invalid container (player " + sender + ")");
				DebugLogger.warn("Computer is " + computer);
				return;
			}
		}

		switch (packet.m_packetType) {
			case ComputerCraftPacket.TurnOn:
				turnOn();
				break;
			case ComputerCraftPacket.Reboot:
				reboot();
				break;
			case ComputerCraftPacket.Shutdown:
				shutdown();
				break;
			case ComputerCraftPacket.QueueEvent: {
				String event = packet.m_dataString[0];
				Object[] arguments = null;
				if (packet.m_dataNBT != null) arguments = NBTUtil.decodeObjects(packet.m_dataNBT);
				queueEvent(event, arguments);
				break;
			}
			case ComputerCraftPacket.RequestComputerUpdate:
				sendState(sender, false);
				break;
			case ComputerCraftPacket.SetLabel: {
				String label = packet.m_dataString != null && packet.m_dataString.length >= 1 ? packet.m_dataString[0] : null;
				setLabel(label);
				break;
			}
		}
	}

	public void broadcastState() {
		broadcastState(false);
	}

	public void broadcastState(boolean initial) {
		// If the computer state has changed, this is the initial state then send it.
		// If neither of these then the terminal must have changed so we want to send this
		// when terminal limiting is disabled.
		if (!Config.Packets.terminalLimiting || hasOutputChanged() || initial) {
			ComputerCraftPacket packet = createStatePacket();
			writeDescription(packet.m_dataNBT, !Config.Packets.terminalLimiting);

			if (Config.Packets.updateLimiting && m_world != null && m_position != null && !initial) {
				FMLCommonHandler handler = FMLCommonHandler.instance();
				if (handler == null) return;
				MinecraftServer server = handler.getMinecraftServerInstance();
				if (server == null) return;
				ComputerCraft.networkEventChannel.sendToAllAround(
					encode(packet),
					new NetworkRegistry.TargetPoint(
						m_world.provider.getDimension(),
						m_position.getX() + 0.5,
						m_position.getY() + 0.5,
						m_position.getZ() + 0.5,
						MathHelper.clamp_int(server.getPlayerList().getViewDistance(), 3, 32) * 16
					)
				);
			} else {
				ComputerCraft.sendToAllPlayers(packet);
			}
		}

		// We'll have sent the terminal above if terminal limiting isn't enabled.
		// If terminal limiting is enabled then we obviously need to send the terminal to those interacting with it.
		// We send this even if there hasn't been a terminal change for two reasons:
		//  - Update the computer state of those out of range
		//  - Correct the terminal for those within range (it will have been cleared by the above packet).
		// However this does mean we end up sending packets twice to those within range and interacting with it.
		// Thankfully computer state doesn't change too much so this shouldn't be much of an issue.
		if (Config.Packets.terminalLimiting) {
			ComputerCraftPacket termPacket = createStatePacket();
			writeDescription(termPacket.m_dataNBT, true);

			FMLCommonHandler handler = FMLCommonHandler.instance();
			if (handler == null) return;

			MinecraftServer server = handler.getMinecraftServerInstance();
			if (server == null) return;

			// Send the terminal data to those watching. Sadly this does mean we send
			// it twice, but I can live.
			for (EntityPlayerMP player : server.getPlayerList().getPlayerList()) {
				Container container = player.openContainer;
				if (container instanceof IContainerComputer && ((IContainerComputer) container).getComputer() == this) {
					ComputerCraft.sendToPlayer(player, termPacket);
				}
			}
		}
	}

	public void sendState(EntityPlayer player) {
		sendState(player, true);
	}

	private void sendState(EntityPlayer player, boolean withTerminal) {
		ComputerCraftPacket packet = createStatePacket();
		writeDescription(packet.m_dataNBT, withTerminal || !Config.Packets.terminalLimiting);
		ComputerCraft.sendToPlayer(player, packet);
	}

	public ComputerCraftPacket createStatePacket() {
		ComputerCraftPacket packet = new ComputerCraftPacket();
		packet.m_packetType = ComputerCraftPacket.ComputerChanged;
		packet.m_dataInt = new int[]{getInstanceID()};
		packet.m_dataNBT = new NBTTagCompound();
		return packet;
	}

	public void writeDescription(NBTTagCompound tag, boolean withTerminal) {
		tag.setBoolean("colour", isColour());
		Terminal terminal = getTerminal();
		if (terminal != null) {
			NBTTagCompound termTag = new NBTTagCompound();
			termTag.setInteger("term_width", terminal.getWidth());
			termTag.setInteger("term_height", terminal.getHeight());
			((Terminal_Patch) terminal).writeToNBT(termTag, withTerminal);
			tag.setTag("terminal", termTag);
		}

		tag.setInteger("id", m_computer.getID());
		String label = m_computer.getLabel();
		if (label != null) tag.setString("label", label);

		tag.setBoolean("on", m_computer.isOn());
		tag.setBoolean("blinking", m_computer.isBlinking());
		if (m_userData != null) tag.setTag("userData", m_userData.copy());
	}

	public FMLProxyPacket encode(ComputerCraftPacket packet) {
		PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
		packet.toBytes(buffer);
		return new FMLProxyPacket(buffer, "CC");
	}
}
