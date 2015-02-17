package squiddev.cctweaks.core.blocks;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.tileentity.TileEntity;

public abstract class TilePeripheralWrapper extends TileEntity implements IPeripheral {
	protected final IPeripheral peripheral;

	public TilePeripheralWrapper(IPeripheral peripheral) {
		this.peripheral = peripheral;
	}

	@Override
	public String getType() {
		return peripheral.getType();
	}

	@Override
	public String[] getMethodNames() {
		return peripheral.getMethodNames();
	}

	@Override
	public Object[] callMethod(IComputerAccess access, ILuaContext context, int index, Object[] objects) throws LuaException, InterruptedException {
		return peripheral.callMethod(access, context, index, objects);
	}

	@Override
	public void attach(IComputerAccess access) {
		peripheral.attach(access);
	}

	@Override
	public void detach(IComputerAccess access) {
		peripheral.detach(access);
	}

	@Override
	public boolean equals(IPeripheral other) {
		return other != null && (peripheral.getClass().isAssignableFrom(other.getClass()));
	}
}
