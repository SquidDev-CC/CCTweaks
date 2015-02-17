package squiddev.cctweaks.core.blocks;

import squiddev.cctweaks.core.peripheral.DebuggerPeripheral;

/**
 * @see DebuggerPeripheral
 */
public class TileDebugger extends TilePeripheralWrapper {

	public TileDebugger() {
		super(new DebuggerPeripheral());
	}
}
