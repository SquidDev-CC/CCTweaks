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
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Facing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.Events;
import org.squiddev.cctweaks.core.utils.FakeNetHandler;
import org.squiddev.cctweaks.core.utils.WorldPosition;

/**
 * Handles various turtle actions.
 *
 * Emulates large parts of {@link ItemInWorldManager}
 */
public class ToolHostPlayer extends TurtlePlayer {
	private final ITurtleAccess turtle;

	private ChunkCoordinates digPosition;
	private Block digBlock;

	private final Events.IDropConsumer consumer = new Events.IDropConsumer() {
		@Override
		public void consumeDrop(ItemStack drop) {
			ItemStack remainder = InventoryUtil.storeItems(drop, turtle.getInventory(), 0, turtle.getInventory().getSizeInventory(), turtle.getSelectedSlot());
			if (remainder != null) {
				ChunkCoordinates position = getPlayerCoordinates();
				WorldUtil.dropItemStack(remainder, worldObj, position.posX, position.posY, position.posZ, Facing.oppositeSide[turtle.getDirection()]);
			}
		}
	};

	public ToolHostPlayer(ITurtleAccess turtle) {
		super((WorldServer) turtle.getWorld());
		this.turtle = turtle;

		playerNetServerHandler = new FakeNetHandler(this);
	}

	public TurtleCommandResult attack(int direction) {
		updateInformation(direction);

		Vec3 rayDir = getLook(1.0f);
		Vec3 rayStart = Vec3
			.createVectorHelper(posX, posY, posZ)
			.addVector(rayDir.xCoord * 0.4, rayDir.yCoord * 0.4, rayDir.zCoord * 0.4);

		Entity hitEntity = WorldUtil.rayTraceEntities(turtle.getWorld(), rayStart, rayDir, 1.1);

		if (hitEntity != null) {
			loadInventory(getItem());

			Events.addEntityConsumer(hitEntity, consumer);
			attackTargetEntityWithCurrentItem(hitEntity);
			Events.removeEntityConsumer(hitEntity);

			unloadInventory(turtle);
			return TurtleCommandResult.success();
		}

		return TurtleCommandResult.failure("Nothing to attack here");
	}

	public TurtleCommandResult dig(int direction) {
		updateInformation(direction);

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

			loadInventory(getItem());

			ItemInWorldManager manager = theItemInWorldManager;
			for (int i = 0; i < Config.Turtle.ToolHost.digFactor; i++) {
				if (manager.durabilityRemainingOnBlock == -1) {
					manager.onBlockClicked(x, y, z, Facing.oppositeSide[direction]);
				} else {
					manager.updateBlockRemoving();
					if (manager.durabilityRemainingOnBlock >= 9) {

						IWorldPosition position = new WorldPosition(world, x, y, z);
						Events.addBlockConsumer(position, consumer);
						manager.uncheckedTryHarvestBlock(x, y, z);
						Events.removeBlockConsumer(position);

						manager.durabilityRemainingOnBlock = -1;

						break;
					}
				}
			}

			unloadInventory(turtle);

			return TurtleCommandResult.success();
		}

		return TurtleCommandResult.failure("Nothing to dig here");
	}

	@Override
	public ChunkCoordinates getPlayerCoordinates() {
		return turtle.getPosition();
	}

	/**
	 * Basically just {@link #getHeldItem()}
	 */
	public ItemStack getItem() {
		return turtle.getInventory().getStackInSlot(turtle.getSelectedSlot());
	}

	/**
	 * Update the player information
	 */
	public void updateInformation(int direction) {
		ChunkCoordinates position = turtle.getPosition();
		posX = position.posX + 0.5 + 0.48 * Facing.offsetsXForSide[direction];
		posY = position.posY + 0.5 + 0.48 * Facing.offsetsYForSide[direction];
		posZ = position.posZ + 0.5 + 0.48 * Facing.offsetsYForSide[direction];

		if (direction > 2) {
			rotationYaw = DirectionUtil.toYawAngle(direction);
			rotationPitch = 0.0F;
		} else {
			rotationYaw = DirectionUtil.toYawAngle(turtle.getDirection());
			rotationPitch = DirectionUtil.toPitchAngle(direction);
		}

		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		prevRotationYaw = rotationYaw;
		prevRotationPitch = rotationPitch;
	}
}
