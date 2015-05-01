package org.squiddev.cctweaks.core.items;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.items.ComputerItemFactory;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import dan200.computercraft.shared.turtle.items.TurtleItemFactory;
import dan200.computercraft.shared.util.ImpostorShapelessRecipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.oredict.RecipeSorter;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.core.registry.ComputerUpgradeCrafting;
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
			DebugLogger.warn("Cannot copy tile in ItemComputerUpgrade", e);
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
			DebugLogger.warn("Cannot set TurtleTile m_moved in ItemComputerUpgrade", e);
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
		list.add(StatCollector.translateToLocal("gui.tooltip.cctweaks.computerUpgrade.normal"));
	}

	@Override
	public void init() {
		super.init();
		RecipeSorter.register(CCTweaks.RESOURCE_DOMAIN + ":computer_upgrade_crafting", ComputerUpgradeCrafting.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");

		ItemStack stack = new ItemStack(this);
		GameRegistry.addRecipe(stack, "GGG", "GSG", "GSG", 'G', Items.gold_ingot, 'S', Blocks.stone);
		GameRegistry.addRecipe(new ComputerUpgradeCrafting());

		// Add some impostor recipes for NEI. We just use CC's default ones
		{
			// Computer
			GameRegistry.addRecipe(new ImpostorShapelessRecipe(
				ComputerItemFactory.create(-1, null, ComputerFamily.Advanced),
				new Object[]{
					ComputerItemFactory.create(-1, null, ComputerFamily.Normal),
					stack
				}
			));

			// Turtle (Is is silly to include every possible upgrade so we just do the normal one)
			GameRegistry.addRecipe(new ImpostorShapelessRecipe(
				TurtleItemFactory.create(-1, null, null, ComputerFamily.Advanced, null, null, 0),
				new Object[]{
					TurtleItemFactory.create(-1, null, null, ComputerFamily.Normal, null, null, 0),
					stack
				}
			));

			// Non-wireless pocket computer
			GameRegistry.addRecipe(new ImpostorShapelessRecipe(
				PocketComputerItemFactory.create(-1, null, ComputerFamily.Advanced, false),
				new Object[]{
					PocketComputerItemFactory.create(-1, null, ComputerFamily.Normal, false),
					stack
				}
			));

			// Wireless pocket computer
			GameRegistry.addRecipe(new ImpostorShapelessRecipe(
				PocketComputerItemFactory.create(-1, null, ComputerFamily.Advanced, true),
				new Object[]{
					PocketComputerItemFactory.create(-1, null, ComputerFamily.Normal, true),
					stack
				}
			));
		}
	}
}
