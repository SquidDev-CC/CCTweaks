package org.squiddev.cctweaks.client.gui;

import dan200.computercraft.client.gui.GuiComputer;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.IComputer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.cctweaks.command.ContainerAnyComputer;

@SideOnly(Side.CLIENT)
public class GuiAnyComputer extends GuiComputer {
	public GuiAnyComputer(IComputer computer, ComputerFamily family) {
		super(
			new ContainerAnyComputer(computer), family, computer,
			computer.getTerminal().getWidth(), computer.getTerminal().getHeight()
		);
	}
}
