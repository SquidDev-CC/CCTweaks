package org.squiddev.cctweaks.items;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.core.lua.LuaJLuaMachine;
import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.util.PeripheralUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.apache.commons.lang3.StringUtils;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.DebugLib;
import org.squiddev.cctweaks.api.network.INetworkAccess;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.NetworkAPI;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.network.modem.BasicModemPeripheral;
import org.squiddev.cctweaks.core.utils.ComputerAccessor;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ItemDebugger extends ItemComputerAction {
	public ItemDebugger() {
		super("debugger");
	}

	@Override
	protected boolean upgradeComputer(ItemStack stack, EntityPlayer player, TileComputerBase computerTile, int side) {
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

		} catch (ReflectiveOperationException e) {
			DebugLogger.warn("Could not add DebugLib", e);
			return false;
		} catch (NullPointerException e) {
			DebugLogger.warn("Could not add DebugLib", e);
			return false;
		} catch (Exception e) {
			DebugLogger.error("Unknown error in injecting DebugLib", e);
			return false;
		}

		return true;
	}

	@Override
	protected boolean itemUse(ItemStack stack, EntityPlayer player, TileEntity tile, int side) {
		Set<String> locals = new HashSet<String>();
		Set<String> remotes = new HashSet<String>();
		IPeripheral peripheral;

		INetworkNode node = NetworkAPI.registry().getNode(tile);
		if (node != null) {
			Map<String, IPeripheral> p = node.getConnectedPeripherals();
			if (p != null) locals.addAll(p.keySet());
		}

		peripheral = PeripheralUtil.getPeripheral(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord, side);
		if (peripheral != null) {
			if (peripheral instanceof BasicModemPeripheral) {
				Map<String, IPeripheral> p = ((BasicModemPeripheral) peripheral).modem.peripheralsByName();
				if (p != null) remotes.addAll(p.keySet());
			}
		}

		if (tile instanceof INetworkAccess) {
			Map<String, IPeripheral> p = ((INetworkAccess) tile).peripheralsByName();
			if (p != null) remotes.addAll(p.keySet());
		}

		if (peripheral != null || !locals.isEmpty() || !remotes.isEmpty()) {
			player.addChatComponentMessage(
				new ChatComponentText("Tile: " + tile.getClass().getSimpleName() + ": " + tile.getBlockType().getLocalizedName())
					.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.LIGHT_PURPLE))
			);

			if (peripheral != null) player.addChatMessage(headerChat("Peripheral", peripheral.getType()));
			if (!locals.isEmpty()) player.addChatMessage(headerChat("Locals", locals));
			if (!remotes.isEmpty()) player.addChatMessage(headerChat("Remotes", remotes));
			return true;
		}

		return false;
	}

	@Override
	public boolean canLoad() {
		return Config.config.enableDebugWand;
	}

	public static IChatComponent headerChat(String header, String message) {
		return new ChatComponentText(header + ": ")
			.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA))
			.appendSibling(new ChatComponentText(message)
					.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY).setItalic(true))
			);
	}

	public static IChatComponent headerChat(String header, Iterable<String> message) {
		return headerChat(header, StringUtils.join(message, ", "));
	}
}
