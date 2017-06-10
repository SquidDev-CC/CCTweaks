package org.squiddev.cctweaks.turtle;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.shared.turtle.core.TurtlePlayer;
import dan200.computercraft.shared.util.DirectionUtil;
import dan200.computercraft.shared.util.InventoryUtil;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.McEvents;
import org.squiddev.cctweaks.core.turtle.TurtleHooks;
import org.squiddev.cctweaks.core.utils.FakeNetHandler;
import org.squiddev.cctweaks.core.utils.WorldPosition;

import javax.annotation.Nonnull;

/**
 * Handles various turtle actions.
 */
public class ToolHostPlayer extends TurtlePlayer {
	private BlockPos coordinates;
	private Vec3d positionVector;

	private BlockPos digPosition;
	private Block digBlock;

	private int currentDamage = -1;
	private int currentDamageState = -1;

	/**
	 * A copy of the active stack for applying/removing attributes
	 */
	private ItemStack activeStack;

	public ToolHostPlayer(ITurtleAccess turtle) {
		super((WorldServer) turtle.getWorld());
		connection = new FakeNetHandler(this);
	}

	public McEvents.IDropConsumer getConsumer(final ITurtleAccess turtle) {
		return new McEvents.IDropConsumer() {
			@Override
			public void consumeDrop(ItemStack drop) {
				storeItem(drop, new PlayerMainInvWrapper(inventory), turtle);
			}
		};
	}

	public TurtleCommandResult attack(ITurtleAccess turtle, Entity hitEntity) {
		if (hitEntity != null) {
			McEvents.addEntityConsumer(hitEntity, getConsumer(turtle));
			attackTargetEntityWithCurrentItem(hitEntity);
			McEvents.removeEntityConsumer(hitEntity);

			return TurtleCommandResult.success();
		}

		return TurtleCommandResult.failure("Nothing to attack here");
	}

	private void setState(Block block, BlockPos pos) {
		interactionManager.cancelDestroyingBlock();
		interactionManager.durabilityRemainingOnBlock = -1;

		digPosition = pos;
		digBlock = block;
		currentDamage = -1;
		currentDamageState = -1;
	}

	public TurtleCommandResult dig(ITurtleAccess turtle, EnumFacing direction, BlockPos pos) {
		World world = turtle.getWorld();
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		if (block != digBlock || !pos.equals(digPosition)) setState(block, pos);

		if (!world.isAirBlock(pos) && !state.getMaterial().isLiquid()) {
			if (ComputerCraft.turtlesObeyBlockProtection && !TurtleHooks.isBlockBreakable(world, pos, this)) {
				return TurtleCommandResult.failure("Cannot break protected block");
			}

			if (block == Blocks.BEDROCK || state.getBlockHardness(world, pos) <= -1) {
				return TurtleCommandResult.failure("Unbreakable block detected");
			}

			PlayerInteractionManager manager = interactionManager;
			for (int i = 0; i < Config.Turtle.ToolHost.digFactor; i++) {
				if (currentDamageState == -1) {
					manager.onBlockClicked(pos, direction.getOpposite());
					currentDamageState = manager.durabilityRemainingOnBlock;
				} else {
					currentDamage++;
					float hardness = state.getPlayerRelativeBlockHardness(this, world, pos) * (currentDamage + 1);
					int hardnessState = (int) (hardness * 10);

					if (hardnessState != currentDamageState) {
						world.sendBlockBreakProgress(getEntityId(), pos, hardnessState);
						currentDamageState = hardnessState;
					}

					if (hardness >= 1) {
						IWorldPosition position = new WorldPosition(world, pos);
						McEvents.addBlockConsumer(position, getConsumer(turtle));
						manager.tryHarvestBlock(pos);
						McEvents.removeBlockConsumer(position);

						setState(null, null);
						break;
					}
				}
			}

			return TurtleCommandResult.success();
		}

		return TurtleCommandResult.failure("Nothing to dig here");
	}

	@Override
	public Vec3d getPositionVector() {
		return positionVector;
	}

	/**
	 * Basically just {@link #getHeldItemMainhand()}
	 */
	@Nonnull
	public ItemStack getItem(ITurtleAccess turtle) {
		return turtle.getInventory().getStackInSlot(turtle.getSelectedSlot());
	}

	public void load(ITurtleAccess turtle, EnumFacing direction, boolean sneaking) {
		// Update the position arguments
		net.minecraft.util.math.BlockPos position = turtle.getPosition();
		positionVector = turtle.getVisualPosition(0);

		setPositionAndRotation(
			position.getX() + 0.5 + 0.48 * direction.getFrontOffsetX(),
			position.getY() + 0.5 + 0.48 * direction.getFrontOffsetY(),
			position.getZ() + 0.5 + 0.48 * direction.getFrontOffsetZ(),
			direction.getAxis() != EnumFacing.Axis.Y ? DirectionUtil.toYawAngle(direction) : DirectionUtil.toYawAngle(turtle.getDirection()),
			direction.getAxis() != EnumFacing.Axis.Y ? 0 : DirectionUtil.toPitchAngle(direction)
		);

		setSneaking(sneaking);

		// Apply item stack properties
		ItemStack currentStack = getItem(turtle);
		if (!currentStack.isEmpty()) {
			getAttributeMap().applyAttributeModifiers(currentStack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
			activeStack = currentStack.copy();
		}

		// Update the inventory
		IInventory turtleInventory = turtle.getInventory();
		int size = turtleInventory.getSizeInventory();
		int largerSize = inventory.getSizeInventory();

		inventory.currentItem = turtle.getSelectedSlot();

		for (int i = 0; i < size; i++) {
			inventory.setInventorySlotContents(i, turtleInventory.getStackInSlot(i));
		}
		for (int i = size; i < largerSize; i++) {
			inventory.setInventorySlotContents(i, ItemStack.EMPTY);
		}
	}

	public void unload(ITurtleAccess turtle) {
		// Revert to old properties
		if (activeStack != null && !activeStack.isEmpty()) {
			getAttributeMap().removeAttributeModifiers(activeStack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
			activeStack = null;
		}

		// Reset sneaking
		setSneaking(false);

		// Place items back into turtle, or world
		IItemHandlerModifiable turtleInventory = turtle.getItemHandler();
		int size = turtleInventory.getSlots();
		int largerSize = inventory.getSizeInventory();

		for (int i = 0; i < size; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			turtleInventory.setStackInSlot(i, stack.isEmpty() ? ItemStack.EMPTY : stack);
			inventory.setInventorySlotContents(i, ItemStack.EMPTY);
		}

		for (int i = size; i < largerSize; i++) {
			storeItem(inventory.getStackInSlot(i), turtleInventory, turtle);
			inventory.setInventorySlotContents(i, ItemStack.EMPTY);
		}
	}

	private static void storeItem(ItemStack stack, IItemHandler inventory, ITurtleAccess turtle) {
		ItemStack remainder = InventoryUtil.storeItems(stack, inventory, turtle.getSelectedSlot());
		if (!remainder.isEmpty()) {
			BlockPos position = turtle.getPosition();
			WorldUtil.dropItemStack(remainder, turtle.getWorld(), position, turtle.getDirection().getOpposite());
		}
	}
}
