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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.squiddev.cctweaks.api.network.INetworkCompatiblePeripheral;
import org.squiddev.cctweaks.core.turtle.TurtleRegistry;
import org.squiddev.cctweaks.lua.lib.DelayedTask;

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

		final int dir = direction.toWorldDir(access);

		return new DelayedTask() {
			@Override
			public Object[] execute() throws LuaException {
				return doUse(this, computer, dir, sneak, duration);
			}
		}.execute(computer, context);
	}

	public Object[] doUse(DelayedTask task, IComputerAccess computer, int direction, boolean sneak, int duration) throws LuaException {
		player.updateInformation(access, direction);
		player.posY += 1.5;
		player.loadWholeInventory(access);
		player.setSneaking(sneak);

		ForgeDirection fDirection = ForgeDirection.VALID_DIRECTIONS[direction];
		MovingObjectPosition hit = findHit(fDirection, 0.65);
		ItemStack stack = player.getItem(access);
		World world = player.worldObj;

		try {
			if (stack != null) {
				TurtleCommandResult result = TurtleRegistry.instance.use(access, computer, player, stack, fDirection, hit);
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

						result = tryUseOnBlock(world, hit, stack, ForgeDirection.OPPOSITES[hit.sideHit]);
						if (result != null) return result;
					}
				}
			}

			if (stack != null && !ForgeEventFactory.onPlayerInteract(player, PlayerInteractEvent.Action.RIGHT_CLICK_AIR, 0, 0, 0, -1, world).isCanceled()) {
				player.posX += fDirection.offsetX * 0.6;
				player.posY += fDirection.offsetY * 0.6;
				player.posZ += fDirection.offsetZ * 0.6;

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

	public Object[] tryUseOnBlock(World world, MovingObjectPosition hit, ItemStack stack, int side) {
		int x = hit.blockX;
		int y = hit.blockY;
		int z = hit.blockZ;
		if (!world.getBlock(x, y, z).isAir(world, x, y, z)) {
			if (ForgeEventFactory.onPlayerInteract(player, PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, x, y, z, side, world).isCanceled()) {
				return new Object[]{true, "block", "interact"};
			}

			Object[] result = onPlayerRightClick(stack, x, y, z, side, hit.hitVec);
			if (result != null) return result;
		}

		return null;
	}

	public Object[] onPlayerRightClick(ItemStack stack, int x, int y, int z, int side, Vec3 look) {
		float xCoord = (float) look.xCoord - (float) x;
		float yCoord = (float) look.yCoord - (float) y;
		float zCoord = (float) look.zCoord - (float) z;
		World world = player.worldObj;

		if (stack != null && stack.getItem() != null && stack.getItem().onItemUseFirst(stack, player, world, x, y, z, side, xCoord, yCoord, zCoord)) {
			return new Object[]{true, "item", "use"};
		}

		if (!player.isSneaking() || stack == null || stack.getItem().doesSneakBypassUse(world, x, y, z, player)) {
			if (world.getBlock(x, y, z).onBlockActivated(world, x, y, z, player, side, xCoord, yCoord, zCoord)) {
				return new Object[]{true, "block", "interact"};
			}
		}

		if (stack == null) return null;

		if (stack.getItem() instanceof ItemBlock) {
			ItemBlock itemBlock = (ItemBlock) stack.getItem();

			ForgeDirection direction = ForgeDirection.VALID_DIRECTIONS[side];
			if (!world.canPlaceEntityOnSide(itemBlock.field_150939_a, x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ, false, side, null, stack)) {
				return null;
			}
		}

		if (stack.tryPlaceItemIntoWorld(player, world, x, y, z, side, xCoord, yCoord, zCoord)) {
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

		final int dir = direction.toWorldDir(access);

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

	public TurtleCommandResult doSwing(IComputerAccess computer, int direction, boolean sneak) throws LuaException {
		player.updateInformation(access, direction);
		player.posY += 1.5;
		player.loadWholeInventory(access);
		player.setSneaking(sneak);

		ForgeDirection fDirection = ForgeDirection.VALID_DIRECTIONS[direction];
		ItemStack stack = player.getItem(access);
		TurtleAnimation animation = side == TurtleSide.Left ? TurtleAnimation.SwingLeftTool : TurtleAnimation.SwingRightTool;
		MovingObjectPosition hit = findHit(fDirection, 0.65);

		try {
			if (stack != null) {
				TurtleCommandResult result = TurtleRegistry.instance.swing(access, computer, player, stack, fDirection, hit);
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
		final int direction = dir.toWorldDir(access);
		return context.executeMainThreadTask(new ILuaTask() {
			@Override
			public Object[] execute() throws LuaException {
				player.updateInformation(access, direction);
				player.posY += 1.5;
				player.loadWholeInventory(access);

				ItemStack stack = player.getHeldItem();
				ForgeDirection fDirection = ForgeDirection.VALID_DIRECTIONS[direction];
				MovingObjectPosition hit = findHit(fDirection, 0.65);

				try {
					if (stack != null) {
						boolean result = TurtleRegistry.instance.canUse(access, player, stack, fDirection, hit);
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
		final int direction = dir.toWorldDir(access);
		return context.executeMainThreadTask(new ILuaTask() {
			@Override
			public Object[] execute() throws LuaException {
				player.updateInformation(access, direction);
				player.posY += 1.5;
				player.loadWholeInventory(access);

				ItemStack stack = player.getHeldItem();
				ForgeDirection fDirection = ForgeDirection.VALID_DIRECTIONS[direction];
				MovingObjectPosition hit = findHit(fDirection, 0.65);

				try {
					if (stack != null) {
						boolean result = TurtleRegistry.instance.canSwing(access, player, stack, fDirection, hit);
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
						Block block = access.getWorld().getBlock(hit.blockX, hit.blockY, hit.blockZ);
						int meta = access.getWorld().getBlockMetadata(hit.blockX, hit.blockY, hit.blockZ);
						if (block.canHarvestBlock(player, meta)) return new Object[]{true};
					}

					return new Object[]{false};
				} finally {
					player.unloadWholeInventory(access);
				}
			}
		});
	}
	//endregion

	public MovingObjectPosition findHit(ForgeDirection facing, double range) {
		Vec3 origin = Vec3.createVectorHelper(
			player.posX,
			player.posY,
			player.posZ
		);
		Vec3 blockCenter = origin.addVector(
			facing.offsetX * 0.51,
			facing.offsetY * 0.51,
			facing.offsetZ * 0.51
		);
		Vec3 target = blockCenter.addVector(
			facing.offsetX * range,
			facing.offsetY * range,
			facing.offsetZ * range
		);

		MovingObjectPosition hit = player.worldObj.rayTraceBlocks(origin, target);
		Entity entity = WorldUtil.rayTraceEntities(player.worldObj, origin, target, 1.1);

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
