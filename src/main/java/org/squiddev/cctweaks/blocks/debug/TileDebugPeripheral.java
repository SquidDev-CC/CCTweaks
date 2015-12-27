package org.squiddev.cctweaks.blocks.debug;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.util.EnumFacing;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHost;
import org.squiddev.cctweaks.blocks.TileBase;
import org.squiddev.cctweaks.core.utils.DebugLogger;

/**
 * A peripheral that logs events and prints what side it comes from
 */
public class TileDebugPeripheral extends TileBase implements IPeripheralHost {
	private final IPeripheral[] sides = new IPeripheral[6];

	@Override
	public IPeripheral getPeripheral(EnumFacing side) {
		int s = side.ordinal();
		if (sides[s] != null) return sides[s];
		return sides[s] = createPeripheral(s);
	}

	protected IPeripheral createPeripheral(int side) {
		return new SidedPeripheral(side);
	}

	public static class SidedPeripheral implements IPeripheral {
		private final String sideName;

		public SidedPeripheral(int side) {
			this.sideName = EnumFacing.VALUES[side].getName();
		}

		@Override
		public String getType() {
			return "sided";
		}

		@Override
		public String[] getMethodNames() {
			return new String[]{"getSide"};
		}

		@Override
		public Object[] callMethod(IComputerAccess computer, ILuaContext context, int function, Object[] arguments) throws LuaException, InterruptedException {
			return new Object[]{sideName};
		}

		@Override
		public void attach(IComputerAccess computer) {
			DebugLogger.debug("Attaching to computer " + computer + " #" + computer.getID() + " with " + computer.getAttachmentName() + " for side " + sideName);
		}

		@Override
		public void detach(IComputerAccess computer) {
			DebugLogger.debug("Detaching from computer " + computer + " #" + computer.getID() + " with " + computer.getAttachmentName() + " for side " + sideName);
		}

		@Override
		public boolean equals(IPeripheral peripheral) {
			return peripheral == this;
		}
	}
}
