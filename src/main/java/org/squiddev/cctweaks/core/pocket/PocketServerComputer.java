package org.squiddev.cctweaks.core.pocket;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.network.ComputerCraftPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.squiddev.cctweaks.api.IContainerComputer;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.patch.ServerComputer_Patch;
import org.squiddev.cctweaks.core.patch.iface.IExtendedServerComputer;

import java.lang.ref.WeakReference;

public class PocketServerComputer extends ServerComputer {
	private final IExtendedServerComputer extended;
	private WeakReference<Entity> owner;

	public PocketServerComputer(World world, int computerID, String label, int instanceID, ComputerFamily family, int terminalWidth, int terminalHeight) {
		super(world, computerID, label, instanceID, family, terminalWidth, terminalHeight);
		this.extended = (IExtendedServerComputer) this;
	}

	public void setOwner(Entity entity) {
		Entity owner = getOwner();
		if (owner != entity) {
			this.owner = entity == null ? null : new WeakReference<Entity>(entity);
			if (entity instanceof EntityPlayerMP) sendState((EntityPlayerMP) entity);
		}
	}

	public Entity getOwner() {
		WeakReference<Entity> owner = this.owner;
		return owner == null ? null : owner.get();
	}

	/**
	 * @see ServerComputer_Patch#broadcastState(boolean)
	 */
	public void broadcastState(boolean initial) {
		World world = getWorld();
		BlockPos position = getPosition();
		Entity owner = getOwner();

		// If the computer state has changed, this is the initial state then send it.
		// If neither of these then the terminal must have changed so we want to send this
		// when terminal limiting is disabled.
		if (!Config.Packets.terminalLimiting || hasOutputChanged() || initial) {
			ComputerCraftPacket packet = extended.createStatePacket();
			extended.writeDescription(packet.m_dataNBT, !Config.Packets.terminalLimiting);

			if (Config.Packets.updateLimiting && world != null && position != null && !initial) {
				if (owner instanceof EntityPlayerMP) sendState((EntityPlayerMP) owner);
			} else {
				ComputerCraft.sendToAllPlayers(packet);
			}
		}

		// We'll have sent the terminal above if terminal limiting isn't enabled.
		// If terminal limiting is enabled then we obviously need to send the terminal to those interacting with it.
		if (Config.Packets.terminalLimiting) {
			ComputerCraftPacket termPacket = extended.createStatePacket();
			extended.writeDescription(termPacket.m_dataNBT, true);

			// Send the terminal data to those watching. Sadly this does mean we send
			// it twice, but I can live.
			FMLCommonHandler handler = FMLCommonHandler.instance();
			boolean sentOwner = false;
			if (handler != null) {
				MinecraftServer server = handler.getMinecraftServerInstance();
				for (EntityPlayerMP player : server.getPlayerList().getPlayerList()) {
					Container container = player.openContainer;
					if (container instanceof IContainerComputer && ((IContainerComputer) container).getComputer() == this) {
						if (player == owner) sentOwner = true;
						ComputerCraft.sendToPlayer(player, termPacket);
					}
				}
			}

			if (!sentOwner && owner instanceof EntityPlayerMP) {
				ComputerCraft.sendToPlayer((EntityPlayerMP) owner, termPacket);
			}
		}
	}
}
