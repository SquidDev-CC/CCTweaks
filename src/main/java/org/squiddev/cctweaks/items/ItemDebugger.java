package org.squiddev.cctweaks.items;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.core.lua.LuaJLuaMachine;
import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.util.PeripheralUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.DebugLib;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.NetworkAPI;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.utils.ComputerAccessor;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.cctweaks.core.utils.WorldPosition;
import org.squiddev.cctweaks.core.visualiser.NetworkPlayerWatcher;
import org.squiddev.cctweaks.core.visualiser.VisualisationPacket;

import java.util.Set;

public class ItemDebugger extends ItemComputerAction {
	public ItemDebugger() {
		super("debugger");
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos position, EnumFacing side, float hitX, float hitY, float hitZ) {
		return Config.Computer.debugWandEnabled && super.onItemUseFirst(stack, player, world, position, side, hitX, hitY, hitZ);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		// Allow manually refreshing the network
		if (player.isSneaking() && !world.isRemote && player instanceof EntityPlayerMP && Config.Computer.debugWandEnabled) {
			handleWatcher((EntityPlayerMP) player, NetworkPlayerWatcher.get(player), true);
		}

		return super.onItemRightClick(stack, world, player);
	}

	protected void handleWatcher(EntityPlayerMP player, NetworkPlayerWatcher.Watcher watcher, boolean force) {
		if (watcher == null) return;
		if (watcher.changed() || force) VisualisationPacket.send(watcher.controller, player);
		if (watcher.controller == null) NetworkPlayerWatcher.remove(player);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int p_77663_4_, boolean p_77663_5_) {
		super.onUpdate(stack, world, entity, p_77663_4_, p_77663_5_);

		if (!world.isRemote && entity instanceof EntityPlayerMP && Config.Computer.debugWandEnabled) {
			EntityPlayerMP player = ((EntityPlayerMP) entity);
			if (player.getHeldItem() == stack) {
				MovingObjectPosition position = getMovingObjectPositionFromPlayer(world, player, false);
				if (position == null || position.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;
				handleWatcher(player, NetworkPlayerWatcher.update(player, position.getBlockPos()), false);
			}
		}
	}

	@Override
	protected boolean useComputer(ItemStack stack, EntityPlayer player, TileComputerBase computerTile, EnumFacing side) {
		ServerComputer serverComputer = computerTile.getServerComputer();
		if (serverComputer == null) return false;

		try {
			Object computer = ComputerAccessor.serverComputerComputer.get(serverComputer);
			Object luaMachine = ComputerAccessor.computerMachine.get(computer);

			if (!(luaMachine instanceof LuaJLuaMachine)) {
				DebugLogger.warn("Computer is not instance of LuaJLuaMachine, cannot get globals");
				return false;
			}

			LuaValue globals = (LuaValue) ComputerAccessor.luaMachineGlobals.get(luaMachine);
			globals.load(new DebugLib());
		} catch (NullPointerException e) {
			DebugLogger.warn("Could not add DebugLib", e);
			return false;
		} catch (IllegalAccessException e) {
			DebugLogger.warn("Could not add DebugLib", e);
			return false;
		} catch (Exception e) {
			DebugLogger.error("Unknown error in injecting DebugLib", e);
			return false;
		}

		return true;
	}

	@Override
	protected boolean useGeneric(ItemStack stack, EntityPlayer player, TileEntity tile, EnumFacing side) {
		IWorldPosition position = new WorldPosition(tile);

		player.addChatComponentMessage(
			withColor("Tile: ", EnumChatFormatting.DARK_PURPLE)
				.appendSibling(info(tile.getClass().getSimpleName() + ": " + tile.getBlockType().getLocalizedName()))
		);

		{
			IPeripheral peripheral = PeripheralUtil.getPeripheral(tile.getWorld(), tile.getPos(), side);
			if (peripheral != null) {
				player.addChatMessage(withColor("Peripheral: ", EnumChatFormatting.AQUA).appendSibling(info(peripheral.getType())));
			}
		}

		{
			INetworkNode node = NetworkAPI.registry().getNode(tile);
			INetworkController controller = node != null ? node.getAttachedNetwork() : null;
			if (controller != null) {
				player.addChatComponentMessage(withColor("Network", EnumChatFormatting.LIGHT_PURPLE));
				Set<INetworkNode> nodes = controller.getNodesOnNetwork();
				player.addChatMessage(withColor(" Size: ", EnumChatFormatting.AQUA).appendSibling(info(nodes.size() + " nodes")));

				boolean writtenHeader = false;
				for (INetworkNode remoteNode : nodes) {
					if (remoteNode == node || (remoteNode instanceof IWorldNetworkNode && position.equals(((IWorldNetworkNode) remoteNode).getPosition()))) {
						Set<String> peripherals = remoteNode.getConnectedPeripherals().keySet();
						if (!peripherals.isEmpty()) {
							if (!writtenHeader) {
								writtenHeader = true;
								player.addChatMessage(withColor(" Locals: ", EnumChatFormatting.AQUA));
							}
							player.addChatMessage(withColor("  " + remoteNode.toString() + " ", EnumChatFormatting.DARK_AQUA).appendSibling(info(peripherals)));
						}
					}
				}

				Set<String> remotes = controller.getPeripheralsOnNetwork().keySet();
				if (!remotes.isEmpty()) {
					player.addChatMessage(withColor(" Remotes: ", EnumChatFormatting.AQUA).appendSibling(info(remotes)));
				}
			}
		}

		return false;
	}

	private static IChatComponent withColor(String message, EnumChatFormatting color) {
		return new ChatComponentText(message).setChatStyle(new ChatStyle().setColor(color));
	}

	private static IChatComponent info(Iterable<String> message) {
		return info(StringUtils.join(message, ", "));
	}

	private static IChatComponent info(String message) {
		return new ChatComponentText(message).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY).setItalic(true));
	}
}
