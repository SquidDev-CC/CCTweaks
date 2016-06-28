package org.squiddev.cctweaks.core.turtle;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.turtle.blocks.ITurtleTile;
import dan200.computercraft.shared.util.InventoryUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.IFluidBlock;
import org.squiddev.cctweaks.api.CCTweaksAPI;
import org.squiddev.cctweaks.api.network.INetworkNodeProvider;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNodeHost;
import org.squiddev.cctweaks.api.network.NetworkAPI;
import org.squiddev.cctweaks.api.turtle.AbstractTurtleInteraction;
import org.squiddev.cctweaks.api.turtle.ITurtleFuelProvider;
import org.squiddev.cctweaks.core.registry.Module;

/**
 * Registers turtle related things
 */
public class DefaultTurtleProviders extends Module {
	@Override
	public void init() {
		// Add default furnace fuel provider
		CCTweaksAPI.instance().fuelRegistry().addFuelProvider(new ITurtleFuelProvider() {
			@Override
			public boolean canRefuel(ITurtleAccess turtle, ItemStack stack) {
				return TileEntityFurnace.isItemFuel(stack);
			}

			@Override
			public int refuel(ITurtleAccess turtle, ItemStack stack, int limit) {
				int fuelToGive = TileEntityFurnace.getItemBurnTime(stack) * 5 / 100 * limit;
				ItemStack replacementStack = stack.getItem().getContainerItem(stack);

				// Remove 'n' items from the stack.
				InventoryUtil.takeItems(limit, turtle.getInventory(), 0, turtle.getInventory().getSizeInventory(), turtle.getSelectedSlot());
				if (replacementStack != null) {
					// If item is empty (bucket) then add it back
					InventoryUtil.storeItems(replacementStack, turtle.getInventory(), 0, turtle.getInventory().getSizeInventory(), turtle.getSelectedSlot());
				}

				return fuelToGive;
			}
		});

		// Allow upgrades with a network node
		// TODO: Bind all nodes into one like CablePart
		NetworkAPI.registry().addNodeProvider(new INetworkNodeProvider() {
			@Override
			public IWorldNetworkNode getNode(TileEntity tile) {
				if (tile instanceof ITurtleTile) {
					ITurtleAccess turtle = ((ITurtleTile) tile).getAccess();

					for (TurtleSide side : TurtleSide.values()) {
						IWorldNetworkNode node = getNode(turtle, side);
						if (node != null) return node;
					}
				}

				return null;
			}

			@Override
			public boolean isNode(TileEntity tile) {
				return getNode(tile) != null;
			}

			public IWorldNetworkNode getNode(ITurtleAccess turtle, TurtleSide side) {
				ITurtleUpgrade upgrade = turtle.getUpgrade(side);
				if (upgrade != null) {
					if (upgrade instanceof IWorldNetworkNode) return (IWorldNetworkNode) upgrade;
					if (upgrade instanceof IWorldNetworkNodeHost) return ((IWorldNetworkNodeHost) upgrade).getNode();
				}

				IPeripheral peripheral = turtle.getPeripheral(side);
				if (peripheral != null) {
					if (peripheral instanceof IWorldNetworkNode) return (IWorldNetworkNode) peripheral;
					if (peripheral instanceof IWorldNetworkNodeHost) {
						return ((IWorldNetworkNodeHost) peripheral).getNode();
					}
				}

				return null;
			}
		});

		// Add bucket using provider
		CCTweaksAPI.instance().turtleRegistry().registerInteraction(new AbstractTurtleInteraction() {
			@Override
			public boolean canUse(ITurtleAccess turtle, FakePlayer player, ItemStack stack, EnumFacing direction, RayTraceResult hit) {
				if (!FluidContainerRegistry.isBucket(stack)) return false;

				BlockPos coords = turtle.getPosition().offset(direction);
				IBlockState state = turtle.getWorld().getBlockState(coords);
				Block block = state.getBlock();

				if (block.isAir(state, turtle.getWorld(), coords)) {
					return FluidContainerRegistry.isFilledContainer(stack);
				} else if (block instanceof IFluidBlock || block instanceof BlockLiquid || block.getMaterial(state).isLiquid()) {
					return FluidContainerRegistry.isEmptyContainer(stack);
				} else {
					return false;
				}
			}
		});
	}
}
