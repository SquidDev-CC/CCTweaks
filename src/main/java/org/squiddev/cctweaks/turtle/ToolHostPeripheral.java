package org.squiddev.cctweaks.turtle;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaTask;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.shared.turtle.core.InteractDirection;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
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
import org.squiddev.cctweaks.api.CCTweaksAPI;
import org.squiddev.cctweaks.api.network.INetworkCompatiblePeripheral;
import org.squiddev.cctweaks.core.utils.DebugLogger;

public class ToolHostPeripheral implements IPeripheral, INetworkCompatiblePeripheral {
	private final ITurtleAccess access;
	private final ToolHostPlayer player;

	public ToolHostPeripheral(ITurtleAccess access, ToolHostPlayer player) {
		this.access = access;
		this.player = player;
	}

	@Override
	public String getType() {
		return "tool_manipulator";
	}

	@Override
	public String[] getMethodNames() {
		return new String[]{
			"use", "useUp", "useDown",
		};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] args) throws LuaException, InterruptedException {
		try {
			switch (method) {
				case 0:
					return use(computer, context, InteractDirection.Forward, args);
				case 1:
					return use(computer, context, InteractDirection.Up, args);
				case 2:
					return use(computer, context, InteractDirection.Down, args);
			}
		} catch (LuaException e) {
			throw e;
		} catch (Exception e) {
			DebugLogger.debug("Error in use", e);
			throw new LuaException(e.toString());
		}

		return null;
	}

	public Object[] use(IComputerAccess computer, ILuaContext context, InteractDirection direction, Object[] args) throws LuaException, InterruptedException {
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

		DelayedTask task = new DelayedTask() {
			@Override
			public Object[] execute() throws LuaException {
				try {
					return doUse(this, dir, sneak, duration);
				} catch (InterruptedException e) {
					throw new LuaException("Terminated");
				} catch (LuaException e) {
					e.printStackTrace();
					throw e;
				} catch (Exception e) {
					DebugLogger.error("Unknown error", e);
					throw new LuaException(e.toString());
				}
			}
		};
		Object[] result = context.executeMainThreadTask(task);
		if (task.delay > 0) CCTweaksAPI.instance().luaEnvironment().sleep(computer, context, task.delay);

		return result;
	}

	private abstract class DelayedTask implements ILuaTask {
		public int delay = -1;
	}

	public Object[] doUse(DelayedTask task, EnumFacing direction, boolean sneak, int duration) throws LuaException, InterruptedException {
		player.updateInformation(direction);
		player.loadWholeInventory();
		player.setSneaking(sneak);

		MovingObjectPosition hit = findHit(direction, 0.65);
		ItemStack stack = player.getItem();
		World world = player.worldObj;

		try {
			if (hit != null) {
				switch (hit.typeOfHit) {
					case ENTITY:
						if (stack != null && player.interactWith(hit.entityHit)) {
							return new Object[]{true, "entity", "interact"};
						}
						break;
					case BLOCK: {
						BlockPos pos = hit.getBlockPos();
						if (!world.getBlockState(pos).getBlock().isAir(world, pos)) {
							if (ForgeEventFactory.onPlayerInteract(player, PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, world, pos, hit.sideHit).isCanceled()) {
								return new Object[]{true, "block", "interact"};
							}

							Object[] result = onPlayerRightClick(stack, pos, hit.sideHit, hit.hitVec);
							if (result != null) return result;
						}
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
			player.unloadWholeInventory();
			player.setSneaking(false);
		}

		return new Object[]{false};
	}

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

	@Override
	public void attach(IComputerAccess computer) {
	}

	@Override
	public void detach(IComputerAccess computer) {
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) return true;
		if (!(other instanceof ToolHostPeripheral)) return false;

		return access.equals(((ToolHostPeripheral) other).access);
	}

	@Override
	public int hashCode() {
		return access.hashCode();
	}

	@Override
	public boolean equals(IPeripheral other) {
		return equals((Object) other);
	}
}
