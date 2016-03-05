package org.squiddev.cctweaks.core.lua;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaTask;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import org.squiddev.cctweaks.api.CCTweaksAPI;

public abstract class DelayedTask implements ILuaTask {
	public int delay = -1;

	public Object[] execute(IComputerAccess computer, ILuaContext context) throws LuaException, InterruptedException {
		Object[] result = context.executeMainThreadTask(this);
		if (delay > 0) CCTweaksAPI.instance().luaEnvironment().sleep(computer, context, delay);
		return result;
	}
}
