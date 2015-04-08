package org.squiddev.cctweaks.core.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.squiddev.cctweaks.core.reference.Localisation;
import org.squiddev.cctweaks.core.utils.BlockNotifyFlags;
import org.squiddev.cctweaks.core.utils.ComputerAccessor;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.util.List;


public class ItemComputerUpgrade extends ItemComputerAction {
	public ItemComputerUpgrade() {
		super("computerUpgrade");
	}

	protected boolean upgradeComputer(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, TileComputerBase computerTile) {
		// Check we can copy the tile and it is a normal computer
		if (computerTile.getFamily() != ComputerFamily.Normal || ComputerAccessor.tileCopy == null) {
			return false;
		}

		// Set metadata
		int metadata = world.getBlockMetadata(x, y, z);
		world.setBlock(x, y, z, ComputerCraft.Blocks.computer, metadata + 8, BlockNotifyFlags.ALL);

		TileEntity newTile = world.getTileEntity(x, y, z);

		if (newTile == null || !(newTile instanceof TileComputerBase)) {
			return false;
		}

		// Why is it not public Dan?
		TileComputerBase newComputer = (TileComputerBase) newTile;
		try {
			ComputerAccessor.tileCopy.invoke(newComputer, computerTile);
		} catch (Exception e) {
			DebugLogger.warning("Cannot copy tile in ItemComputerUpgrade");
			return false;
		}

		// Setup computer
		newComputer.createServerComputer().setWorld(world);
		newComputer.updateBlock();

		return true;
	}

	protected boolean upgradeTurtle(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, TileTurtle computerTile) {
		// Ensure it is a normal computer
		if (computerTile.getFamily() != ComputerFamily.Normal) {
			return false;
		}
		// If we set the turtle as moved, the destroy method won't drop the items
		try {
			ComputerAccessor.turtleTileMoved.setBoolean(computerTile, true);
		} catch (Exception e) {
			DebugLogger.warning("Cannot set TurtleTile m_moved in ItemComputerUpgrade");
			return false;
		}

		// Set block as AdvancedTurtle
		world.setBlock(x, y, z, ComputerCraft.Blocks.turtleAdvanced);
		TileEntity newTile = world.getTileEntity(x, y, z);

		// Transfer state
		if (newTile == null || !(newTile instanceof TileTurtle)) {
			return false;
		}

		TileTurtle newTurtle = (TileTurtle) newTile;
		newTurtle.transferStateFrom(computerTile);

		newTurtle.createServerComputer().setWorld(world);
		newTurtle.createServerComputer().setPosition(x, y, z);
		newTurtle.updateBlock();

		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings("unchecked")
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		list.add(Localisation.Upgrades.Normal.getLocalised());
	}
}
