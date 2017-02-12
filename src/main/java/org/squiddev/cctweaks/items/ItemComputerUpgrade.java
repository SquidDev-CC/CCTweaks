package org.squiddev.cctweaks.items;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.blocks.BlockComputer;
import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.items.ComputerItemFactory;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import dan200.computercraft.shared.turtle.items.TurtleItemFactory;
import dan200.computercraft.shared.util.ImpostorRecipe;
import dan200.computercraft.shared.util.ImpostorShapelessRecipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.RecipeSorter;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.utils.ComputerAccessor;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.cctweaks.core.utils.Helpers;
import org.squiddev.cctweaks.core.utils.InventoryUtils;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemComputerUpgrade extends ItemComputerAction {
	public ItemComputerUpgrade() {
		super("computerUpgrade");
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos position, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (Config.Computer.computerUpgradeEnabled) {
			return super.onItemUse(player, world, position, hand, side, hitX, hitY, hitZ);
		} else {
			return EnumActionResult.PASS;
		}
	}

	@Override
	protected boolean useComputer(ItemStack stack, EntityPlayer player, TileComputerBase computerTile, EnumFacing side) {
		net.minecraft.util.math.BlockPos position = computerTile.getPos();
		World world = computerTile.getWorld();

		// Check we can copy the tile and it is a normal computer
		if (computerTile.getFamily() != ComputerFamily.Normal) {
			return false;
		}

		// Set metadata
		IBlockState state = world.getBlockState(position);
		if (state.getValue(BlockComputer.Properties.ADVANCED)) {
			return false;
		}

		// Ensure we can break/place this block
		if (!world.isBlockModifiable(player, computerTile.getPos())) {
			return false;
		}

		world.setBlockState(position, state.withProperty(BlockComputer.Properties.ADVANCED, true));
		computerTile.invalidate();
		computerTile.validate();

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

		if (!player.capabilities.isCreativeMode) {
			int remaining = InventoryUtils.extractItems(player.inventory, Items.GOLD_INGOT, 7);
			if (remaining > 0) {
				player.sendMessage(new TextComponentString("7 gold required. Need " + remaining + " more.").setStyle(new Style().setColor(TextFormatting.DARK_RED)));
				return false;
			}

			player.inventoryContainer.detectAndSendChanges();
		}

		// Ensure we can break/place this block
		if (!world.isBlockModifiable(player, computerTile.getPos())) {
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

		// Invalidate & revalidate to unload the instance ID
		newTurtle.invalidate();
		newTurtle.validate();

		// And update the computer
		newTurtle.createServerComputer().setWorld(world);
		newTurtle.createServerComputer().setPosition(position);
		newTurtle.updateBlock();

		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		list.add(Helpers.translateToLocal("gui.tooltip.cctweaks.computerUpgrade.normal"));
	}

	@Override
	public void init() {
		super.init();
		if (!Config.Computer.computerUpgradeEnabled) return;

		ItemStack stack = new ItemStack(this);
		if (Config.Computer.computerUpgradeCrafting) {
			GameRegistry.addRecipe(stack, "GGG", "GSG", "GSG", 'G', Items.GOLD_INGOT, 'S', Blocks.STONE);
		}


		RecipeSorter.register(CCTweaks.ID + ":computer_upgrade_crafting", CraftingComputerUpgrade.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
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
			ItemStack gold = new ItemStack(Items.GOLD_INGOT);
			GameRegistry.addRecipe(new ImpostorRecipe(3, 3,
				new ItemStack[]{
					gold, gold, gold,
					gold, TurtleItemFactory.create(-1, null, null, ComputerFamily.Normal, null, null, 0, null), gold,
					gold, stack, gold,
				},
				TurtleItemFactory.create(-1, null, null, ComputerFamily.Advanced, null, null, 0, null)
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
