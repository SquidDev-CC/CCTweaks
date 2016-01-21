package org.squiddev.cctweaks.turtle;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.shared.turtle.core.InteractDirection;
import net.minecraft.item.ItemStack;
import org.squiddev.cctweaks.api.CCTweaksAPI;
import org.squiddev.cctweaks.api.lua.IExtendedLuaTask;
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

	public Object[] use(IComputerAccess computer, ILuaContext context, final InteractDirection direction, Object[] args) throws LuaException, InterruptedException {
		final ItemStack oldStack = access.getInventory().getStackInSlot(access.getSelectedSlot());
		if (oldStack == null) throw new LuaException("No item here");

		int dur;
		if (args.length == 0) {
			dur = oldStack.getMaxItemUseDuration();
		} else {
			if (args[0] instanceof Number) {
				dur = ((Number) args[0]).intValue();

				if (dur < 0) throw new LuaException("Duration must be >= 0");
			} else {
				throw new LuaException("Expected number");
			}
		}

		final int duration = dur;

		return CCTweaksAPI.instance().luaEnvironment().executeTask(computer, context, new IExtendedLuaTask() {
			public ItemStack setupStack() throws LuaException {
				ItemStack stack = access.getInventory().getStackInSlot(access.getSelectedSlot());
				if (stack != oldStack) throw new LuaException("The stack moved");

				player.updateInformation(direction.toWorldDir(access));
				player.loadWholeInventory();

				return stack;
			}

			@Override
			public Object[] execute() throws LuaException {
				ItemStack stack = setupStack();
				stack.getItem().onPlayerStoppedUsing(stack, access.getWorld(), player, duration);
				player.unloadInventory(access);

				return null;
			}

			@Override
			public void update() throws LuaException {
				setupStack().useItemRightClick(access.getWorld(), player);
				player.unloadInventory(access);
			}
		}, duration);
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
	public boolean equals(IPeripheral other) {
		return equals((Object) other);
	}
}
