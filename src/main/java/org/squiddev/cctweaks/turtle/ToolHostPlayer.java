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
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
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
	private final ITurtleAccess turtle;

	private BlockPos digPosition;
	private Block digBlock;

	private final McEvents.IDropConsumer consumer = new McEvents.IDropConsumer() {
		@Override
		public void consumeDrop(ItemStack drop) {
			ItemStack remainder = InventoryUtil.storeItems(drop, turtle.getInventory(), 0, turtle.getInventory().getSizeInventory(), turtle.getSelectedSlot());
			if (remainder != null) {
				BlockPos position = getPlayerCoordinates();
				WorldUtil.dropItemStack(remainder, worldObj, position, turtle.getDirection().getOpposite());
			}
		}
	};

	public ToolHostPlayer(ITurtleAccess turtle) {
		super((WorldServer) turtle.getWorld());
		this.turtle = turtle;

		playerNetServerHandler = new FakeNetHandler(this);
	}

	public TurtleCommandResult attack(EnumFacing direction) {
		updateInformation(direction);

		Vec3 rayDir = getLook(1.0f);
		Vec3 rayStart = new Vec3(posX + rayDir.xCoord * 0.4, posY + rayDir.yCoord * 0.4, posZ + rayDir.zCoord * 0.4);

		Entity hitEntity = WorldUtil.rayTraceEntities(turtle.getWorld(), rayStart, rayDir, 1.1);

		if (hitEntity != null) {
			loadInventory(getItem());

			McEvents.addEntityConsumer(hitEntity, consumer);
			attackTargetEntityWithCurrentItem(hitEntity);
			McEvents.removeEntityConsumer(hitEntity);

			unloadInventory(turtle);
			return TurtleCommandResult.success();
		}

		return TurtleCommandResult.failure("Nothing to attack here");
	}

	public TurtleCommandResult dig(EnumFacing direction) {
		updateInformation(direction);

		BlockPos pos = getPlayerCoordinates().offset(direction);
		World world = turtle.getWorld();
		Block block = world.getBlockState(pos).getBlock();

		if (block != digBlock || !pos.equals(digPosition)) {
			theItemInWorldManager.cancelDestroyingBlock();
			theItemInWorldManager.durabilityRemainingOnBlock = -1;

			digPosition = pos;
			digBlock = block;
		}

		if (!world.isAirBlock(pos) && !block.getMaterial().isLiquid()) {
			if (block == Blocks.bedrock || block.getBlockHardness(world, pos) <= -1) {
				return TurtleCommandResult.failure("Unbreakable block detected");
			}

			loadInventory(getItem());

			ItemInWorldManager manager = theItemInWorldManager;
			for (int i = 0; i < Config.Turtle.ToolHost.digFactor; i++) {
				if (manager.durabilityRemainingOnBlock == -1) {
					manager.onBlockClicked(pos, direction.getOpposite());
				} else {
					manager.updateBlockRemoving();
					if (manager.durabilityRemainingOnBlock >= 9) {

						IWorldPosition position = new WorldPosition(world, pos);
						McEvents.addBlockConsumer(position, consumer);
						manager.tryHarvestBlock(pos);
						McEvents.removeBlockConsumer(position);

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

	public BlockPos getPlayerCoordinates() {
		return turtle.getPosition();
	}

	@Override
	public Vec3 getPositionVector() {
		return turtle.getVisualPosition(0);
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
	public void updateInformation(EnumFacing direction) {
		BlockPos position = turtle.getPosition();

		setPositionAndRotation(
			position.getX() + 0.5 + 0.51 * direction.getFrontOffsetX(),
			position.getY() - 1.1 + 0.51 * direction.getFrontOffsetY(),
			position.getZ() + 0.5 + 0.51 * direction.getFrontOffsetZ(),
			direction.getAxis() != EnumFacing.Axis.Y ? DirectionUtil.toYawAngle(direction) : DirectionUtil.toYawAngle(turtle.getDirection()),
			direction.getAxis() != EnumFacing.Axis.Y ? 0 : DirectionUtil.toPitchAngle(direction)
		);
	}
}
