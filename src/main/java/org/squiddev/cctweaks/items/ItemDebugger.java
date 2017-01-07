package org.squiddev.cctweaks.items;

import dan200.computercraft.api.peripheral.IComputerAccess;
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
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.NetworkAPI;
import org.squiddev.cctweaks.blocks.debug.TileDebugPeripheral;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.utils.ComputerAccessor;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.cctweaks.core.utils.WorldPosition;
import org.squiddev.cctweaks.core.visualiser.NetworkPlayerWatcher;

import java.lang.reflect.Method;
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
	public void onUpdate(ItemStack stack, World world, Entity entity, int p_77663_4_, boolean p_77663_5_) {
		super.onUpdate(stack, world, entity, p_77663_4_, p_77663_5_);

		if (!world.isRemote && entity instanceof EntityPlayerMP && Config.Network.Visualisation.enabled) {
			EntityPlayerMP player = ((EntityPlayerMP) entity);
			if (player.getHeldItem() == stack) {
				MovingObjectPosition position = getMovingObjectPositionFromPlayer(world, player, false);
				NetworkPlayerWatcher.update(player, position == null ? null : position.getBlockPos());
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

			if (luaMachine instanceof LuaJLuaMachine) {
				org.luaj.vm2.LuaValue globals = (org.luaj.vm2.LuaValue) ComputerAccessor.luaMachineGlobals.get(luaMachine);
				globals.load(new org.luaj.vm2.lib.DebugLib());
			} else {
				try {
					Method method = luaMachine.getClass().getMethod("injectDebug");
					method.setAccessible(true);
					method.invoke(luaMachine);
				} catch (NoSuchMethodException e) {
					DebugLogger.warn("Computer machine (" + luaMachine.getClass().getName() + ") has no .injectDebug() method. Cannot inject debug library.");
					return false;
				}
			}
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

		player.addChatMessage(
			withColor("Tile: ", EnumChatFormatting.DARK_PURPLE)
				.appendSibling(info(tile.getClass().getSimpleName() + ": " + tile.getBlockType().getLocalizedName()))
		);

		{
			IPeripheral peripheral = PeripheralUtil.getPeripheral(tile.getWorld(), tile.getPos(), side);
			if (peripheral != null) {
				player.addChatMessage(withColor("Peripheral: ", EnumChatFormatting.AQUA).appendSibling(info(peripheral.getType())));

				if (peripheral instanceof TileDebugPeripheral.SidedPeripheral) {
					Set<IComputerAccess> computers = ((TileDebugPeripheral.SidedPeripheral) peripheral).computers();
					IChatComponent boundList = withColor("Bound as: ", EnumChatFormatting.AQUA);
					boolean first = true;
					for (IComputerAccess access : computers) {
						if (first) {
							first = false;
						} else {
							boundList.appendSibling(info(", "));
						}

						String text;
						ChatStyle style = new ChatStyle().setItalic(true);
						try {
							text = access.getAttachmentName() + " (#" + access.getID() + ")";
							style.setColor(EnumChatFormatting.GRAY);
						} catch (RuntimeException e) {
							text = e.getMessage();
							style.setColor(EnumChatFormatting.RED);
						}

						boundList.appendSibling(new ChatComponentText(text).setChatStyle(style));
					}

					player.addChatMessage(boundList);
				}
			}
		}

		{
			INetworkNode node = NetworkAPI.registry().getNode(tile);
			INetworkController controller = node != null ? node.getAttachedNetwork() : null;
			if (controller != null) {
				player.addChatMessage(withColor("Network", EnumChatFormatting.LIGHT_PURPLE));
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

		return true;
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
