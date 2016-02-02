package org.squiddev.cctweaks.turtle;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.shared.turtle.core.TurtlePlayer;
import dan200.computercraft.shared.util.DirectionUtil;
import dan200.computercraft.shared.util.InventoryUtil;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Facing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.McEvents;
import org.squiddev.cctweaks.core.utils.FakeNetHandler;
import org.squiddev.cctweaks.core.utils.WorldPosition;

/**
 * Handles various turtle actions.
 *
 * Emulates large parts of {@link ItemInWorldManager}
 */
public class ToolHostPlayer extends TurtlePlayer {
	private ChunkCoordinates digPosition;
	private Block digBlock;
	public ItemStack itemInUse;

	private ChunkCoordinates coordinates;

	public ToolHostPlayer(ITurtleAccess turtle) {
		super((WorldServer) turtle.getWorld());
		playerNetServerHandler = new FakeNetHandler(this);
	}

	public McEvents.IDropConsumer getConsumer(final ITurtleAccess turtle) {
		return new McEvents.IDropConsumer() {
			@Override
			public void consumeDrop(ItemStack drop) {
				ItemStack remainder = InventoryUtil.storeItems(drop, turtle.getInventory(), 0, turtle.getInventory().getSizeInventory(), turtle.getSelectedSlot());
				if (remainder != null) {
					ChunkCoordinates position = getPlayerCoordinates();
					WorldUtil.dropItemStack(remainder, worldObj, position.posX, position.posY, position.posZ, Facing.oppositeSide[turtle.getDirection()]);
				}
			}
		};
	}

	public TurtleCommandResult attack(ITurtleAccess turtle, int direction) {
		updateInformation(turtle, direction);

		Vec3 rayDir = getLook(1.0f);
		Vec3 rayStart = Vec3
			.createVectorHelper(posX, posY, posZ)
			.addVector(rayDir.xCoord * 0.4, rayDir.yCoord * 0.4, rayDir.zCoord * 0.4);

		Entity hitEntity = WorldUtil.rayTraceEntities(turtle.getWorld(), rayStart, rayDir, 1.1);

		if (hitEntity != null) {
			loadInventory(getItem(turtle));

			McEvents.addEntityConsumer(hitEntity, getConsumer(turtle));
			attackTargetEntityWithCurrentItem(hitEntity);
			McEvents.removeEntityConsumer(hitEntity);

			unloadInventory(turtle);
			return TurtleCommandResult.success();
		}

		return TurtleCommandResult.failure("Nothing to attack here");
	}

	public TurtleCommandResult dig(ITurtleAccess turtle, int direction) {
		updateInformation(turtle, direction);

		ChunkCoordinates pos = getPlayerCoordinates();
		pos.posX += Facing.offsetsXForSide[direction];
		pos.posY += Facing.offsetsYForSide[direction];
		pos.posZ += Facing.offsetsZForSide[direction];
		int x = pos.posX, y = pos.posY, z = pos.posZ;
		World world = turtle.getWorld();
		Block block = world.getBlock(x, y, z);

		if (block != digBlock || !pos.equals(digPosition)) {
			theItemInWorldManager.cancelDestroyingBlock(x, y, z);
			theItemInWorldManager.durabilityRemainingOnBlock = -1;

			digPosition = pos;
			digBlock = block;
		}

		if (!world.isAirBlock(x, y, z) && !block.getMaterial().isLiquid()) {
			if (block == Blocks.bedrock || block.getBlockHardness(world, x, y, z) <= -1) {
				return TurtleCommandResult.failure("Unbreakable block detected");
			}

			loadInventory(getItem(turtle));

			ItemInWorldManager manager = theItemInWorldManager;
			for (int i = 0; i < Config.Turtle.ToolHost.digFactor; i++) {
				if (manager.durabilityRemainingOnBlock == -1) {
					manager.onBlockClicked(x, y, z, Facing.oppositeSide[direction]);
				} else {
					manager.updateBlockRemoving();
					if (manager.durabilityRemainingOnBlock >= 9) {

						IWorldPosition position = new WorldPosition(world, x, y, z);
						McEvents.addBlockConsumer(position, getConsumer(turtle));
						manager.uncheckedTryHarvestBlock(x, y, z);
						McEvents.removeBlockConsumer(position);

						manager.durabilityRemainingOnBlock = -1;

						break;
					}
				}
			}

			return TurtleCommandResult.success();
		}

		return TurtleCommandResult.failure("Nothing to dig here");
	}

	@Override
	public ChunkCoordinates getPlayerCoordinates() {
		return coordinates;
	}

	/**
	 * Basically just {@link #getHeldItem()}
	 */
	public ItemStack getItem(ITurtleAccess turtle) {
		return turtle.getInventory().getStackInSlot(turtle.getSelectedSlot());
	}

	/**
	 * Update the player information
	 */
	public void updateInformation(ITurtleAccess turtle, int direction) {
		ChunkCoordinates position = coordinates = turtle.getPosition();

		setPositionAndRotation(
			position.posX + 0.5 + 0.51 * Facing.offsetsXForSide[direction],
			position.posY - 1.1 + 0.51 * Facing.offsetsYForSide[direction],
			position.posZ + 0.5 + 0.51 * Facing.offsetsZForSide[direction],
			direction > 2 ? DirectionUtil.toYawAngle(direction) : DirectionUtil.toYawAngle(turtle.getDirection()),
			direction > 2 ? 0 : DirectionUtil.toPitchAngle(direction)
		);

		ySize = -1.1f;
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

	@Override
	public void setItemInUse(ItemStack stack, int duration) {
		super.setItemInUse(stack, duration);
		itemInUse = stack;
	}

	@Override
	public void clearItemInUse() {
		super.clearItemInUse();
		itemInUse = null;
	}
}
