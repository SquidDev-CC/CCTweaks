package squiddev.cctweaks.core.peripheral;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.DebugLib_Rewrite;

/**
 * squiddev.cctweaks.core.peripheral (CC-Tweaks
 */
public class DebuggerPeripheral implements IPeripheral {
	protected static final String name = "debugger";
	protected static final String[] methodNames = {
		"getDebug",
	};

	protected LuaValue debug = null;

	@Override
	public String getType() {
		return name;
	}

	@Override
	public String[] getMethodNames() {
		return methodNames;
	}

	@Override
	public Object[] callMethod(IComputerAccess access, ILuaContext context, int index, Object[] objects) throws LuaException, InterruptedException {
		switch (index) {
			case 0: // getDebug
				return new Object[]{this.debug};
		}

		return new Object[0];
	}

	@Override
	public void attach(IComputerAccess access) {


	}

	@Override
	public void detach(IComputerAccess access) {

	}

	@Override
	public boolean equals(IPeripheral peripheral) {
		return peripheral instanceof DebuggerPeripheral;
	}

	/**
	 * Create a debug library
	 *
	 * @return The current debug library
	 */
	protected LuaValue setupDebug() {
		LuaValue debug = this.debug;
		if (debug == null) {

			LuaTable env = new LuaTable();
			env.load(new DebugLib_Rewrite());

			debug = this.debug = env.get("debug");
		}

		return debug;
	}
}
