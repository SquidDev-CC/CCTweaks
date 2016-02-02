package org.squiddev.cctweaks.items;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.blocks.BlockComputer;
import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.items.ComputerItemFactory;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import dan200.computercraft.shared.turtle.items.TurtleItemFactory;
import dan200.computercraft.shared.util.ImpostorShapelessRecipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.RecipeSorter;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.utils.ComputerAccessor;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.util.List;


public class ItemComputerUpgrade extends ItemComputerAction {
	public ItemComputerUpgrade() {
		super("computerUpgrade");
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos position, EnumFacing side, float hitX, float hitY, float hitZ) {
		return Config.Computer.computerUpgradeEnabled && super.onItemUseFirst(stack, player, world, position, side, hitX, hitY, hitZ);
	}

	@Override
	protected boolean useComputer(ItemStack stack, EntityPlayer player, TileComputerBase computerTile, EnumFacing side) {
		BlockPos position = computerTile.getPos();
		World world = computerTile.getWorld();

		// Check we can copy the tile and it is a normal computer
		if (computerTile.getFamily() != ComputerFamily.Normal || ComputerAccessor.tileCopy == null) {
			return false;
		}

		// Set metadata
		IBlockState state = world.getBlockState(position);
		if (ComputerAccessor.tileCopy == null || state.getValue(BlockComputer.Properties.ADVANCED)) {
			return false;
		}

		world.setBlockState(position, state.withProperty(BlockComputer.Properties.ADVANCED, true));
		return true;
	}

	@Override
	protected boolean useTurtle(ItemStack stack, EntityPlayer player, TileTurtle computerTile, EnumFacing side) {
		BlockPos position = computerTile.getPos();
		World world = computerTile.getWorld();

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
		world.setBlockState(position, ComputerCraft.Blocks.turtleAdvanced.getDefaultState());
		TileEntity newTile = world.getTileEntity(position);

		// Transfer state
		if (newTile == null || !(newTile instanceof TileTurtle)) {
			return false;
		}

		TileTurtle newTurtle = (TileTurtle) newTile;
		newTurtle.transferStateFrom(computerTile);

		newTurtle.createServerComputer().setWorld(world);
		newTurtle.createServerComputer().setPosition(position);
		newTurtle.updateBlock();

		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		list.add(StatCollector.translateToLocal("gui.tooltip.cctweaks.computerUpgrade.normal"));
	}

	@Override
	public void init() {
		super.init();
		if (!Config.Computer.computerUpgradeEnabled) return;

		ItemStack stack = new ItemStack(this);
		if (Config.Computer.computerUpgradeCrafting) {
			GameRegistry.addRecipe(stack, "GGG", "GSG", "GSG", 'G', Items.gold_ingot, 'S', Blocks.stone);
		}


		RecipeSorter.register(CCTweaks.RESOURCE_DOMAIN + ":computer_upgrade_crafting", CraftingComputerUpgrade.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
		GameRegistry.addRecipe(new CraftingComputerUpgrade());

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
				TurtleItemFactory.create(-1, null, null, ComputerFamily.Advanced, null, null, 0, null, null),
				new Object[]{
					TurtleItemFactory.create(-1, null, null, ComputerFamily.Normal, null, null, 0, null, null),
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
