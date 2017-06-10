package org.squiddev.cctweaks.command;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.core.IContainerComputer;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import org.squiddev.cctweaks.core.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ContainerAnyComputer extends Container implements IContainerComputer {
	private final IComputer computer;

	public ContainerAnyComputer(IComputer computer) {
		this.computer = computer;
	}

	@Override
	public boolean canInteractWith(@Nonnull EntityPlayer player) {
		if (computer instanceof ServerComputer) {
			ServerComputer computer = (ServerComputer) this.computer;

			// Ensure the computer is still loaded
			if (!ComputerCraft.serverComputerRegistry.contains(computer.getInstanceID())) {
				return false;
			}

			TileComputerBase tileBase = Helpers.getTile(computer);
			if (tileBase != null && !tileBase.isUsable(player, true)) {
				return false;
			}
		}

		return true;
	}

	@Nullable
	@Override
	public IComputer getComputer() {
		return computer;
	}
}
