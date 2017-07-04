package org.squiddev.cctweaks.core.turtle;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.turtle.blocks.ITurtleTile;
import dan200.computercraft.shared.turtle.core.TurtlePlayer;
import dan200.computercraft.shared.util.InventoryUtil;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.CCTweaksAPI;
import org.squiddev.cctweaks.api.network.INetworkNodeProvider;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNodeHost;
import org.squiddev.cctweaks.api.network.NetworkAPI;
import org.squiddev.cctweaks.api.turtle.ITurtleFuelProvider;
import org.squiddev.cctweaks.core.registry.IModule;

import javax.annotation.Nonnull;

/**
 * Registers turtle related things
 */
public class DefaultTurtleProviders implements IModule {
	@Override
	public void preInit() {
		EntityRegistry.registerModEntity(
			new ResourceLocation(CCTweaks.ID, "ccFakePlayer"),
			TurtlePlayer.class, CCTweaks.ID + ":ccFakePlayer", 1, CCTweaks.instance,
			Integer.MAX_VALUE, Integer.MAX_VALUE, false
		);
	}

	@Override
	public void init() {
		// Add default furnace fuel provider
		CCTweaksAPI.instance().fuelRegistry().addFuelProvider(new ITurtleFuelProvider() {
			@Override
			public boolean canRefuel(@Nonnull ITurtleAccess turtle, @Nonnull ItemStack stack) {
				return TileEntityFurnace.isItemFuel(stack);
			}

			@Override
			public int refuel(@Nonnull ITurtleAccess turtle, @Nonnull ItemStack stack, int limit) {
				if (limit > stack.getCount()) limit = stack.getCount();

				int fuelToGive = TileEntityFurnace.getItemBurnTime(stack) * 5 / 100 * limit;
				ItemStack replacementStack = stack.getItem().getContainerItem(stack);

				// Remove 'n' items from the stack.
				int slot = turtle.getSelectedSlot();
				InventoryUtil.takeItems(limit, turtle.getItemHandler(), slot);
				if (!replacementStack.isEmpty()) {
					// If item is empty (bucket) then add it back
					replacementStack = InventoryUtil.storeItems(replacementStack, turtle.getItemHandler(), slot);
					if (!replacementStack.isEmpty()) {
						WorldUtil.dropItemStack(replacementStack, turtle.getWorld(), turtle.getPosition(), turtle.getDirection().getOpposite());
					}
				}

				return fuelToGive;
			}
		});

		// Allow upgrades with a network node
		// TODO: Bind all nodes into one like CablePart
		NetworkAPI.registry().addNodeProvider(new INetworkNodeProvider() {
			@Override
			public IWorldNetworkNode getNode(@Nonnull TileEntity tile) {
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
			public boolean isNode(@Nonnull TileEntity tile) {
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
	}
}
