package org.squiddev.cctweaks.turtle;

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
import net.minecraftforge.common.ForgeHooks;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.McEvents;
import org.squiddev.cctweaks.core.utils.FakeNetHandler;
import org.squiddev.cctweaks.core.utils.WorldPosition;

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
				ItemStack remainder = InventoryUtil.storeItems(drop, turtle.getInventory(), 0, turtle.getInventory().getSizeInventory(), turtle.getSelectedSlot());
				if (remainder != null) {
					BlockPos position = turtle.getPosition();
					WorldUtil.dropItemStack(remainder, worldObj, position, turtle.getDirection().getOpposite());
				}
			}
		};
	}

	public TurtleCommandResult attack(ITurtleAccess turtle, EnumFacing direction) {
		updateInformation(turtle, direction);

		Vec3d rayDir = getLook(1.0f);
		Vec3d rayStart = new Vec3d(posX, posY, posZ);

		Pair<Entity, Vec3d> hit = WorldUtil.rayTraceEntities(turtle.getWorld(), rayStart, rayDir, 1.5);
		if (hit != null) {
			Entity hitEntity = hit.getLeft();
			loadInventory(getItem(turtle));

			McEvents.addEntityConsumer(hitEntity, getConsumer(turtle));
			attackTargetEntityWithCurrentItem(hitEntity);
			McEvents.removeEntityConsumer(hitEntity);

			unloadInventory(turtle);
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

	public TurtleCommandResult dig(ITurtleAccess turtle, EnumFacing direction) {
		updateInformation(turtle, direction);

		BlockPos pos = turtle.getPosition().offset(direction);
		World world = turtle.getWorld();
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		if (block != digBlock || !pos.equals(digPosition)) setState(block, pos);

		if (!world.isAirBlock(pos) && !block.getMaterial(state).isLiquid()) {
			if (block == Blocks.BEDROCK || block.getBlockHardness(state, world, pos) <= -1) {
				return TurtleCommandResult.failure("Unbreakable block detected");
			}

			loadInventory(getItem(turtle));

			PlayerInteractionManager manager = interactionManager;
			for (int i = 0; i < Config.Turtle.ToolHost.digFactor; i++) {
				if (currentDamageState == -1) {
					// TODO: Migrate checks to here
					manager.onBlockClicked(pos, direction.getOpposite());
					currentDamageState = manager.durabilityRemainingOnBlock;
				} else {
					currentDamage++;
					float hardness = ForgeHooks.blockStrength(state, this, world, pos) * (currentDamage + 1);
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

	@Override
	public void loadInventory(ItemStack currentStack) {
		// Copy properties over
		if (currentStack != null) {
			getAttributeMap().applyAttributeModifiers(currentStack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
			activeStack = currentStack.copy();
		}

		super.loadInventory(currentStack);
	}

	@Override
	public ItemStack unloadInventory(ITurtleAccess turtle) {
		// Revert to old properties
		if (activeStack != null) {
			getAttributeMap().removeAttributeModifiers(activeStack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
			activeStack = null;
		}

		return super.unloadInventory(turtle);
	}

	public void loadInventory(ITurtleAccess turtle) {
		loadInventory(getItem(turtle));
	}

	/**
	 * Basically just {@link #getHeldItem(net.minecraft.util.EnumHand)}
	 */
	public ItemStack getItem(ITurtleAccess turtle) {
		return turtle.getInventory().getStackInSlot(turtle.getSelectedSlot());
	}

	/**
	 * Update the player information
	 */
	public void updateInformation(ITurtleAccess turtle, EnumFacing direction) {
		BlockPos position = turtle.getPosition();
		positionVector = turtle.getVisualPosition(0);

		setPositionAndRotation(
			position.getX() + 0.5 + 0.48 * direction.getFrontOffsetX(),
			position.getY() + 0.5 + 0.48 * direction.getFrontOffsetY(),
			position.getZ() + 0.5 + 0.48 * direction.getFrontOffsetZ(),
			direction.getAxis() != EnumFacing.Axis.Y ? DirectionUtil.toYawAngle(direction) : DirectionUtil.toYawAngle(turtle.getDirection()),
			direction.getAxis() != EnumFacing.Axis.Y ? 0 : DirectionUtil.toPitchAngle(direction)
		);
	}

	public void loadWholeInventory(ITurtleAccess turtle) {
		IInventory turtleInventory = turtle.getInventory();
		int size = turtleInventory.getSizeInventory();
		int largerSize = inventory.getSizeInventory();

		for (int i = 0; i < size; i++) {
			inventory.setInventorySlotContents(i, turtleInventory.getStackInSlot(i));
		}
		for (int i = size; i < largerSize; i++) {
			inventory.setInventorySlotContents(i, null);
		}
	}

	public void unloadWholeInventory(ITurtleAccess turtle) {
		IInventory turtleInventory = turtle.getInventory();
		int size = turtleInventory.getSizeInventory();
		int largerSize = inventory.getSizeInventory();

		for (int i = 0; i < size; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			turtleInventory.setInventorySlotContents(i, stack == null || stack.stackSize <= 0 ? null : stack);
		}

		McEvents.IDropConsumer consumer = getConsumer(turtle);
		for (int i = size; i < largerSize; i++) {
			consumer.consumeDrop(inventory.getStackInSlot(i));
		}
	}
}
