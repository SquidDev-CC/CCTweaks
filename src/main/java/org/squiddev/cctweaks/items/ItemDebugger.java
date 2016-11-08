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
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
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
import org.squiddev.cctweaks.core.visualiser.VisualisationPacket;
import org.squiddev.cctweaks.lua.lib.cobalt.CobaltMachine;
import org.squiddev.cctweaks.lua.lib.rembulan.RembulanMachine;

import javax.annotation.Nonnull;
import java.util.Set;

public class ItemDebugger extends ItemComputerAction {
	public ItemDebugger() {
		super("debugger");
	}

	@Override
	public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos position, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		if (Config.Computer.debugWandEnabled) {
			return super.onItemUseFirst(stack, player, world, position, side, hitX, hitY, hitZ, hand);
		} else {
			return EnumActionResult.PASS;
		}
	}

	@Override
	@Nonnull
	public ActionResult<ItemStack> onItemRightClick(@Nonnull ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
		// Allow manually refreshing the network
		if (player.isSneaking() && !world.isRemote && player instanceof EntityPlayerMP && Config.Computer.debugWandEnabled) {
			handleWatcher((EntityPlayerMP) player, NetworkPlayerWatcher.get(player), true);
		}

		return super.onItemRightClick(stack, world, player, hand);
	}

	private void handleWatcher(EntityPlayerMP player, NetworkPlayerWatcher.Watcher watcher, boolean force) {
		if (watcher == null) return;
		if (watcher.changed() || force) VisualisationPacket.send(watcher.controller, player);
		if (watcher.controller == null) NetworkPlayerWatcher.remove(player);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int p_77663_4_, boolean p_77663_5_) {
		super.onUpdate(stack, world, entity, p_77663_4_, p_77663_5_);

		if (!world.isRemote && entity instanceof EntityPlayerMP && Config.Computer.debugWandEnabled) {
			EntityPlayerMP player = ((EntityPlayerMP) entity);
			if (player.getHeldItem(EnumHand.MAIN_HAND) == stack || player.getHeldItem(EnumHand.OFF_HAND) == stack) {
				RayTraceResult position = getMovingObjectPositionFromPlayer(world, player);
				if (position == null || position.typeOfHit != RayTraceResult.Type.BLOCK) return;
				handleWatcher(player, NetworkPlayerWatcher.update(player, position.getBlockPos()), false);
			}
		}
	}

	private static RayTraceResult getMovingObjectPositionFromPlayer(World world, EntityPlayer player) {
		Vec3d vec3 = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);

		float pitch = player.rotationPitch;
		float yaw = player.rotationYaw;

		float f2 = MathHelper.cos(-yaw * 0.017453292F - 3.1415927F);
		float f3 = MathHelper.sin(-yaw * 0.017453292F - 3.1415927F);
		float f4 = -MathHelper.cos(-pitch * 0.017453292F);
		float f5 = MathHelper.sin(-pitch * 0.017453292F);
		float f6 = f3 * f4;
		float f7 = f2 * f4;

		double distance = 5.0D;
		if (player instanceof EntityPlayerMP) {
			distance = ((EntityPlayerMP) player).interactionManager.getBlockReachDistance();
		}

		Vec3d look = vec3.addVector(f6 * distance, f5 * distance, f7 * distance);

		return world.rayTraceBlocks(vec3, look, false, true, false);
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
			} else if (luaMachine instanceof CobaltMachine) {
				((CobaltMachine) luaMachine).injectDebug();
			} else if (luaMachine instanceof RembulanMachine) {
				((RembulanMachine) luaMachine).injectDebug();
			} else {
				DebugLogger.warn("Computer machine is unknown (" + luaMachine.getClass().getName() + "). Cannot inject debug library.");
				return false;
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
			withColor("Tile: ", TextFormatting.DARK_PURPLE)
				.appendSibling(info(tile.getClass().getSimpleName() + ": " + tile.getBlockType().getLocalizedName()))
		);

		{
			IPeripheral peripheral = PeripheralUtil.getPeripheral(tile.getWorld(), tile.getPos(), side);
			if (peripheral != null) {
				player.addChatMessage(withColor("Peripheral: ", TextFormatting.AQUA).appendSibling(info(peripheral.getType())));

				if (peripheral instanceof TileDebugPeripheral.SidedPeripheral) {
					Set<IComputerAccess> computers = ((TileDebugPeripheral.SidedPeripheral) peripheral).computers();
					ITextComponent boundList = withColor("Bound as: ", TextFormatting.AQUA);
					boolean first = true;
					for (IComputerAccess access : computers) {
						if (first) {
							first = false;
						} else {
							boundList.appendSibling(info(", "));
						}

						String text;
						Style style = new Style().setItalic(true);
						try {
							text = access.getAttachmentName() + " (#" + access.getID() + ")";
							style.setColor(TextFormatting.GRAY);
						} catch (RuntimeException e) {
							text = e.getMessage();
							style.setColor(TextFormatting.RED);
						}

						boundList.appendSibling(new TextComponentString(text).setStyle(style));
					}

					player.addChatMessage(boundList);
				}
			}
		}

		{
			INetworkNode node = NetworkAPI.registry().getNode(tile);
			INetworkController controller = node != null ? node.getAttachedNetwork() : null;
			if (controller != null) {
				player.addChatMessage(withColor("Network", TextFormatting.LIGHT_PURPLE));
				Set<INetworkNode> nodes = controller.getNodesOnNetwork();
				player.addChatMessage(withColor(" Size: ", TextFormatting.AQUA).appendSibling(info(nodes.size() + " nodes")));

				boolean writtenHeader = false;
				for (INetworkNode remoteNode : nodes) {
					if (remoteNode == node || (remoteNode instanceof IWorldNetworkNode && position.equals(((IWorldNetworkNode) remoteNode).getPosition()))) {
						Set<String> peripherals = remoteNode.getConnectedPeripherals().keySet();
						if (!peripherals.isEmpty()) {
							if (!writtenHeader) {
								writtenHeader = true;
								player.addChatMessage(withColor(" Locals: ", TextFormatting.AQUA));
							}
							player.addChatMessage(withColor("  " + remoteNode.toString() + " ", TextFormatting.DARK_AQUA).appendSibling(info(peripherals)));
						}
					}
				}

				Set<String> remotes = controller.getPeripheralsOnNetwork().keySet();
				if (!remotes.isEmpty()) {
					player.addChatMessage(withColor(" Remotes: ", TextFormatting.AQUA).appendSibling(info(remotes)));
				}
			}
		}

		return false;
	}

	private static ITextComponent withColor(String message, TextFormatting color) {
		return new TextComponentString(message).setStyle(new Style().setColor(color));
	}

	private static ITextComponent info(Iterable<String> message) {
		return info(StringUtils.join(message, ", "));
	}

	private static ITextComponent info(String message) {
		return new TextComponentString(message).setStyle(new Style().setColor(TextFormatting.GRAY).setItalic(true));
	}
}
