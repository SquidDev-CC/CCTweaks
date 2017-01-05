package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.network.ComputerCraftPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import org.squiddev.cctweaks.api.IContainerComputer;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.cctweaks.lua.patch.Computer_Patch;
import org.squiddev.patcher.visitors.MergeVisitor;

/**
 * Add isMostlyOn method
 */
@MergeVisitor.Rename(
	from = "org/squiddev/cctweaks/lua/patch/Computer_Patch",
	to = "dan200/computercraft/core/computer/Computer"
)
public class ServerComputer_Patch extends ServerComputer {
	@MergeVisitor.Stub
	private Computer_Patch m_computer;

	@MergeVisitor.Stub
	public ServerComputer_Patch() {
		super(null, -1, null, -1, null, -1, -1);
	}

	public boolean isMostlyOn() {
		return m_computer.isMostlyOn();
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
