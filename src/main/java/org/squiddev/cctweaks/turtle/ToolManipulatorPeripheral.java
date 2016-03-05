package org.squiddev.cctweaks.turtle;

import com.google.common.collect.Multimap;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaTask;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.*;
import dan200.computercraft.shared.turtle.core.InteractDirection;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.cctweaks.api.network.INetworkCompatiblePeripheral;
import org.squiddev.cctweaks.core.lua.DelayedTask;
import org.squiddev.cctweaks.core.turtle.TurtleRegistry;

public class ToolManipulatorPeripheral implements IPeripheral, INetworkCompatiblePeripheral {
	private final ITurtleAccess access;
	private final ToolHostPlayer player;
	private final TurtleSide side;

	public ToolManipulatorPeripheral(ITurtleAccess access, ToolHostPlayer player, TurtleSide side) {
		this.access = access;
		this.player = player;
		this.side = side;
	}

	@Override
	public String getType() {
		return "tool_manipulator";
	}

	@Override
	public String[] getMethodNames() {
		return new String[]{
			"use", "useUp", "useDown",
			"canUse", "canUseUp", "canUseDown",
			"swing", "swingUp", "swingDown",
			"canSwing", "canSwingUp", "canSwingDown",
		};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] args) throws LuaException, InterruptedException {
		switch (method) {
			case 0:
				return use(computer, context, InteractDirection.Forward, args);
			case 1:
				return use(computer, context, InteractDirection.Up, args);
			case 2:
				return use(computer, context, InteractDirection.Down, args);
			case 3:
				return canUse(computer, context, InteractDirection.Forward, args);
			case 4:
				return canUse(computer, context, InteractDirection.Up, args);
			case 5:
				return canUse(computer, context, InteractDirection.Down, args);
			case 6:
				return swing(computer, context, InteractDirection.Forward, args);
			case 7:
				return swing(computer, context, InteractDirection.Up, args);
			case 8:
				return swing(computer, context, InteractDirection.Down, args);
			case 9:
				return canSwing(computer, context, InteractDirection.Forward, args);
			case 10:
				return canSwing(computer, context, InteractDirection.Up, args);
			case 11:
				return canSwing(computer, context, InteractDirection.Down, args);
		}

		return null;
	}

	//region Use
	public Object[] use(final IComputerAccess computer, ILuaContext context, InteractDirection direction, Object[] args) throws LuaException, InterruptedException {
		final int duration;
		final boolean sneak;
		if (args.length <= 0 || args[0] == null) {
			duration = 0;
		} else if (args[0] instanceof Number) {
			duration = ((Number) args[0]).intValue();

			if (duration < 0) throw new LuaException("Duration must be >= 0");
		} else {
			throw new LuaException("Expected number for argument #1");
		}

		if (args.length <= 1 || args[1] == null) {
			sneak = false;
		} else if (args[1] instanceof Boolean) {
			sneak = (Boolean) args[1];
		} else {
			throw new LuaException("Expected boolean for argument #2");
		}

		final EnumFacing dir = direction.toWorldDir(access);

		return new DelayedTask() {
			@Override
			public Object[] execute() throws LuaException {
				return doUse(this, computer, dir, sneak, duration);
			}
		}.execute(computer, context);
	}

	public Object[] doUse(DelayedTask task, IComputerAccess computer, EnumFacing direction, boolean sneak, int duration) throws LuaException {
		player.updateInformation(access, direction);
		player.posY += 1.5;
		player.loadWholeInventory(access);
		player.setSneaking(sneak);

		MovingObjectPosition hit = findHit(direction, 0.65);
		ItemStack stack = player.getItem(access);
		World world = player.worldObj;

		try {
			if (stack != null) {
				TurtleCommandResult result = TurtleRegistry.instance.use(access, computer, player, stack, direction, hit);
				if (result != null) return toObjectArray(result);
			}

			if (hit != null) {
				switch (hit.typeOfHit) {
					case ENTITY:
						if (stack != null && player.interactWith(hit.entityHit)) {
							return new Object[]{true, "entity", "interact"};
						}
						break;
					case BLOCK: {
						// When right next to a block the hit direction gets inverted. Try both to see if one works.
						Object[] result = tryUseOnBlock(world, hit, stack, hit.sideHit);
						if (result != null) return result;

						result = tryUseOnBlock(world, hit, stack, hit.sideHit.getOpposite());
						if (result != null) return result;
					}
				}
			}

			if (stack != null && !ForgeEventFactory.onPlayerInteract(player, PlayerInteractEvent.Action.RIGHT_CLICK_AIR, world, null, null).isCanceled()) {
				player.posX += direction.getFrontOffsetX() * 0.6;
				player.posY += direction.getFrontOffsetY() * 0.6;
				player.posZ += direction.getFrontOffsetZ() * 0.6;

				duration = Math.min(duration, stack.getMaxItemUseDuration());
				ItemStack old = stack.copy();
				ItemStack result = stack.useItemRightClick(player.worldObj, player);
				task.delay = duration;

				boolean using = player.isUsingItem();
				if (using && !ForgeEventFactory.onUseItemStop(player, player.itemInUse, duration)) {
					player.itemInUse.onPlayerStoppedUsing(player.worldObj, player, player.itemInUse.getMaxItemUseDuration() - duration);
					player.clearItemInUse();
				}

				if (using || !ItemStack.areItemStacksEqual(old, result)) {
					player.inventory.setInventorySlotContents(player.inventory.currentItem, result);
					return new Object[]{true, "item", "use"};
				} else {
					return new Object[]{false};
				}
			}
		} finally {
			player.clearItemInUse();
			player.unloadWholeInventory(access);
			player.setSneaking(false);
		}

		return new Object[]{false};
	}

	public Object[] tryUseOnBlock(World world, MovingObjectPosition hit, ItemStack stack, EnumFacing side) {
		if (!world.getBlockState(hit.getBlockPos()).getBlock().isAir(world, hit.getBlockPos())) {
			if (ForgeEventFactory.onPlayerInteract(player, PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, world, hit.getBlockPos(), side).isCanceled()) {
				return new Object[]{true, "block", "interact"};
			}

			Object[] result = onPlayerRightClick(stack, hit.getBlockPos(), side, hit.hitVec);
			if (result != null) return result;
		}

		return null;
	}

	public Object[] onPlayerRightClick(ItemStack stack, BlockPos pos, EnumFacing side, Vec3 look) {
		float xCoord = (float) look.xCoord - (float) pos.getX();
		float yCoord = (float) look.yCoord - (float) pos.getY();
		float zCoord = (float) look.zCoord - (float) pos.getZ();
		World world = player.worldObj;

		if (stack != null && stack.getItem() != null && stack.getItem().onItemUseFirst(stack, player, world, pos, side, xCoord, yCoord, zCoord)) {
			return new Object[]{true, "item", "use"};
		}

		if (!player.isSneaking() || stack == null || stack.getItem().doesSneakBypassUse(world, pos, player)) {
			IBlockState state = world.getBlockState(pos);
			if (state.getBlock().onBlockActivated(world, pos, state, player, side, xCoord, yCoord, zCoord)) {
				return new Object[]{true, "block", "interact"};
			}
		}

		if (stack == null) return null;

		if (stack.getItem() instanceof ItemBlock) {
			ItemBlock itemBlock = (ItemBlock) stack.getItem();
			if (!itemBlock.canPlaceBlockOnSide(world, pos, side, player, stack)) {
				return null;
			}
		}

		if (stack.onItemUse(player, world, pos, side, xCoord, yCoord, zCoord)) {
			if (stack.stackSize <= 0) {
				MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, stack));
			}

			return new Object[]{true, "place"};
		}

		return null;
	}
	//endregion

	//region Swing
	public Object[] swing(final IComputerAccess computer, ILuaContext context, InteractDirection direction, Object[] args) throws LuaException, InterruptedException {
		final boolean sneak;

		if (args.length <= 0 || args[0] == null) {
			sneak = false;
		} else if (args[0] instanceof Boolean) {
			sneak = (Boolean) args[0];
		} else {
			throw new LuaException("Expected boolean");
		}

		final EnumFacing dir = direction.toWorldDir(access);

		return access.executeCommand(context, new ITurtleCommand() {
			@Override
			public TurtleCommandResult execute(ITurtleAccess iTurtleAccess) {
				try {
					return doSwing(computer, dir, sneak);
				} catch (LuaException e) {
					return TurtleCommandResult.failure(e.getMessage());
				}
			}
		});
	}

	public TurtleCommandResult doSwing(IComputerAccess computer, EnumFacing direction, boolean sneak) throws LuaException {
		player.updateInformation(access, direction);
		player.posY += 1.5;
		player.loadWholeInventory(access);
		player.setSneaking(sneak);

		ItemStack stack = player.getItem(access);
		TurtleAnimation animation = side == TurtleSide.Left ? TurtleAnimation.SwingLeftTool : TurtleAnimation.SwingRightTool;
		MovingObjectPosition hit = findHit(direction, 0.65);

		try {
			if (stack != null) {
				TurtleCommandResult result = TurtleRegistry.instance.swing(access, computer, player, stack, direction, hit);
				if (result != null) {
					if (result.isSuccess()) access.playAnimation(animation);
					return result;
				}
			}

			if (hit != null) {
				switch (hit.typeOfHit) {
					case ENTITY: {
						TurtleCommandResult result = player.attack(access, direction);
						if (result.isSuccess()) access.playAnimation(animation);
						return result;
					}
					case BLOCK: {
						TurtleCommandResult result = player.dig(access, direction);
						if (result.isSuccess()) access.playAnimation(animation);
						return result;
					}
				}
			}
		} finally {
			player.clearItemInUse();
			player.unloadWholeInventory(access);
			player.setSneaking(false);
		}

		return TurtleCommandResult.failure("Nothing to do here");
	}
	//endregion

	//region Can use
	public Object[] canUse(final IComputerAccess computer, ILuaContext context, InteractDirection dir, Object[] args) throws LuaException, InterruptedException {
		final EnumFacing direction = dir.toWorldDir(access);
		return context.executeMainThreadTask(new ILuaTask() {
			@Override
			public Object[] execute() throws LuaException {
				player.updateInformation(access, direction);
				player.posY += 1.5;
				player.loadWholeInventory(access);

				ItemStack stack = player.getHeldItem();
				MovingObjectPosition hit = findHit(direction, 0.65);

				try {
					if (stack != null) {
						boolean result = TurtleRegistry.instance.canUse(access, player, stack, direction, hit);
						if (result) return new Object[]{true};
					}

					return new Object[]{false};
				} finally {
					player.unloadWholeInventory(access);
				}
			}
		});
	}

	public Object[] canSwing(final IComputerAccess computer, ILuaContext context, InteractDirection dir, Object[] args) throws LuaException, InterruptedException {
		final EnumFacing direction = dir.toWorldDir(access);
		return context.executeMainThreadTask(new ILuaTask() {
			@Override
			public Object[] execute() throws LuaException {
				player.updateInformation(access, direction);
				player.loadWholeInventory(access);

				ItemStack stack = player.getHeldItem();
				MovingObjectPosition hit = findHit(direction, 0.65);

				try {
					if (stack != null) {
						boolean result = TurtleRegistry.instance.canSwing(access, player, stack, direction, hit);
						if (result) return new Object[]{true};

						if (hit.entityHit != null) {
							@SuppressWarnings("unchecked") Multimap<String, AttributeModifier> map = stack.getAttributeModifiers();
							for (AttributeModifier modifier : map.get(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName())) {
								if (modifier.getAmount() > 0) {
									return new Object[]{true};
								}
							}
						}
					}

					if (hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
						Block block = access.getWorld().getBlockState(hit.getBlockPos()).getBlock();
						if (block.canHarvestBlock(access.getWorld(), hit.getBlockPos(), player)) {
							return new Object[]{true};
						}
					}

					return new Object[]{false};
				} finally {
					player.unloadWholeInventory(access);
				}
			}
		});
	}
	//endregion

	public MovingObjectPosition findHit(EnumFacing facing, double range) {
		Vec3 origin = new Vec3(player.posX, player.posY, player.posZ);
		Vec3 blockCenter = origin.addVector(
			facing.getFrontOffsetX() * 0.51,
			facing.getFrontOffsetY() * 0.51,
			facing.getFrontOffsetZ() * 0.51
		);
		Vec3 target = blockCenter.addVector(
			facing.getFrontOffsetX() * range,
			facing.getFrontOffsetY() * range,
			facing.getFrontOffsetZ() * range
		);

		MovingObjectPosition hit = player.worldObj.rayTraceBlocks(origin, target);
		Pair<Entity, Vec3> pair = WorldUtil.rayTraceEntities(player.worldObj, origin, target, 1.1);
		Entity entity = pair == null ? null : pair.getLeft();

		if (entity instanceof EntityLivingBase && (hit == null || origin.squareDistanceTo(blockCenter) > player.getDistanceSqToEntity(entity))) {
			return new MovingObjectPosition(entity);
		} else {
			return hit;
		}
	}

	@Override
	public void attach(IComputerAccess computer) {
	}

	@Override
	public void detach(IComputerAccess computer) {
	}

	@Override
	public boolean equals(Object other) {
		return other == this || (other instanceof ToolManipulatorPeripheral && access.equals(((ToolManipulatorPeripheral) other).access));
	}

	@Override
	public int hashCode() {
		return access.hashCode();
	}

	@Override
	public boolean equals(IPeripheral other) {
		return equals((Object) other);
	}

	public static Object[] toObjectArray(TurtleCommandResult result) {
		if (result.isSuccess()) {
			Object[] resultVals = result.getResults();
			if (resultVals == null) {
				return new Object[]{true};
			} else {
				Object[] returnVals = new Object[resultVals.length + 1];
				returnVals[0] = true;
				System.arraycopy(resultVals, 0, returnVals, 1, resultVals.length);
				return returnVals;
			}
		} else {
			String message = result.getErrorMessage();
			return message == null ? new Object[]{false} : new Object[]{false, result.getErrorMessage()};
		}
	}
}
