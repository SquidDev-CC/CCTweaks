package org.squiddev.cctweaks.turtle;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaTask;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.shared.turtle.core.InteractDirection;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
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

		final int dir = direction.toWorldDir(access);

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

	public Object[] doUse(DelayedTask task, int direction, boolean sneak, int duration) throws LuaException, InterruptedException {
		player.updateInformation(access, direction);
		player.posY += 1.5;
		player.loadWholeInventory(access);
		player.setSneaking(sneak);

		ForgeDirection fDirection = ForgeDirection.VALID_DIRECTIONS[direction];
		MovingObjectPosition hit = findHit(fDirection, 0.65);
		ItemStack stack = player.getItem(access);
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
						int x = hit.blockX;
						int y = hit.blockY;
						int z = hit.blockZ;
						if (!world.getBlock(x, y, z).isAir(world, x, y, z)) {
							if (ForgeEventFactory.onPlayerInteract(player, PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, x, y, z, hit.sideHit, world).isCanceled()) {
								return new Object[]{true, "block", "interact"};
							}

							Object[] result = onPlayerRightClick(stack, x, y, z, hit.sideHit, hit.hitVec);
							if (result != null) return result;
						}
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
