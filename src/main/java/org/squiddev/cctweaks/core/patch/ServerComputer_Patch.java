package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.network.ComputerCraftPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import org.squiddev.cctweaks.api.IContainerComputer;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.cctweaks.lua.patch.ComputerThread_Rewrite;
import org.squiddev.cctweaks.lua.patch.Computer_Patch;
import org.squiddev.cctweaks.lua.patch.iface.IComputerEnvironmentExtended;
import org.squiddev.patcher.visitors.MergeVisitor;

/**
 * - Adds {@link IComputerEnvironmentExtended} and suspending events on timeout
 * - Adds isMostlyOn for detecting when a computer is on or starting up
 * - Various network changes
 */
@MergeVisitor.Rename(
	from = {"org/squiddev/cctweaks/lua/patch/Computer_Patch", "org/squiddev/cctweaks/lua/patch/ComputerThread_Rewrite"},
	to = {"dan200/computercraft/core/computer/Computer", "dan200/computercraft/core/computer/ComputerThread"}
)
public class ServerComputer_Patch extends ServerComputer implements IComputerEnvironmentExtended {
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
	public ServerComputer_Patch() {
		super(null, -1, null, -1, null, -1, -1);
	}

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

		native_handlePacket(packet, sender);
	}

	public void broadcastState() {
		ComputerCraftPacket packet = new ComputerCraftPacket();
		packet.m_packetType = 7;
		packet.m_dataInt = new int[]{this.getInstanceID()};
		packet.m_dataNBT = new NBTTagCompound();
		writeDescription(packet.m_dataNBT);

		if (Config.Packets.updateLimiting && m_world != null && m_position != null) {
			int distance = MathHelper.clamp_int(MinecraftServer.getServer().getConfigurationManager().getViewDistance(), 3, 32) * 16;

			// Send to players within the render distance
			ComputerCraft.networkEventChannel.sendToAllAround(
				encode(packet),
				new NetworkRegistry.TargetPoint(
					m_world.provider.getDimensionId(),
					m_position.getX() + 0.5,
					m_position.getY() + 0.5,
					m_position.getZ() + 0.5,
					distance
				)
			);

			// Send to all players outside the range who are using the terminal
			for (EntityPlayerMP player : MinecraftServer.getServer().getConfigurationManager().getPlayerList()) {
				Container container = player.openContainer;
				if (container instanceof IContainerComputer && ((IContainerComputer) container).getComputer() == this) {
					if (player.worldObj != m_world || player.getDistanceSq(m_position) > distance * distance) {
						ComputerCraft.sendToPlayer(player, packet);
					}
				}
			}
		} else {
			ComputerCraft.sendToAllPlayers(packet);
		}
	}

	@MergeVisitor.Stub
	@MergeVisitor.Rename(from = "handlePacket")
	public void native_handlePacket(ComputerCraftPacket packet, EntityPlayer sender) {
	}

	private static FMLProxyPacket encode(ComputerCraftPacket packet) {
		PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
		packet.toBytes(buffer);
		return new FMLProxyPacket(buffer, "CC");
	}
}
