package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.network.ComputerCraftPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import org.squiddev.cctweaks.api.IContainerComputer;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.cctweaks.lua.patch.ComputerThread_Rewrite;
import org.squiddev.cctweaks.lua.patch.Computer_Patch;
import org.squiddev.cctweaks.lua.patch.iface.IComputerEnvironmentExtended;
import org.squiddev.patcher.visitors.MergeVisitor;

/**
 * Add isMostlyOn method
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
		if (Config.Computer.safeNetworking && packet.m_packetType != ComputerCraftPacket.RequestComputerUpdate && packet.m_packetType != ComputerCraftPacket.RequestTileEntityUpdate) {
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


	@MergeVisitor.Stub
	@MergeVisitor.Rename(from = "handlePacket")
	public void native_handlePacket(ComputerCraftPacket packet, EntityPlayer sender) {
	}
}
