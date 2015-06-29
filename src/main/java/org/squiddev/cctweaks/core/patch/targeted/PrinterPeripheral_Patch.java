package org.squiddev.cctweaks.core.patch.targeted;

import dan200.computercraft.shared.peripheral.printer.PrinterPeripheral;
import dan200.computercraft.shared.peripheral.printer.TilePrinter;
import org.squiddev.cctweaks.api.peripheral.IPeripheralTargeted;
import org.squiddev.patcher.visitors.MergeVisitor;

public class PrinterPeripheral_Patch extends PrinterPeripheral implements IPeripheralTargeted {
	@MergeVisitor.Stub
	private final TilePrinter m_printer;

	@MergeVisitor.Stub
	public PrinterPeripheral_Patch(TilePrinter printer) {
		super(printer);
		m_printer = null;
	}

	@Override
	public Object getTarget() {
		return m_printer;
	}
}
