package org.squiddev.cctweaks;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ClientComputer;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import org.squiddev.cctweaks.client.gui.GuiAnyComputer;
import org.squiddev.cctweaks.command.ContainerAnyComputer;
import org.squiddev.cctweaks.core.utils.Helpers;

public class GuiHandler implements IGuiHandler {
	private static final ComputerFamily[] FAMILIES = ComputerFamily.values();
	private static final int GUI_COMPUTER = 101;

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int arg1, int arg2, int arg3) {
		switch (id) {
			case GUI_COMPUTER: {
				ServerComputer computer = ComputerCraft.serverComputerRegistry.get(arg1);
				return computer == null ? null : new ContainerAnyComputer(computer);
			}
			default:
				return null;
		}
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int arg1, int arg2, int arg3) {
		switch (id) {
			case GUI_COMPUTER: {
				ClientComputer computer = ComputerCraft.clientComputerRegistry.get(arg1);
				ComputerFamily family;
				if (arg2 >= 0 && arg2 <= FAMILIES.length) {
					family = FAMILIES[arg2];
				} else {
					family = computer.isColour() ? ComputerFamily.Advanced : ComputerFamily.Normal;
				}
				return computer == null ? null : new GuiAnyComputer(computer, family);
			}
			default:
				return null;
		}
	}

	public static void openComputer(EntityPlayer player, ServerComputer computer) {
		player.openGui(
			CCTweaks.instance, GUI_COMPUTER, player.getEntityWorld(),
			computer.getInstanceID(), Helpers.guessFamily(computer).ordinal(), 0
		);
	}
}
