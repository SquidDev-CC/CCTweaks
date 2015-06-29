package org.squiddev.cctweaks.core.patch.targeted;

import dan200.computercraft.shared.peripheral.diskdrive.DiskDrivePeripheral;
import dan200.computercraft.shared.peripheral.diskdrive.TileDiskDrive;
import org.squiddev.cctweaks.api.peripheral.IPeripheralTargeted;
import org.squiddev.patcher.visitors.MergeVisitor;

public class DiskDrivePeripheral_Patch extends DiskDrivePeripheral implements IPeripheralTargeted {
	@MergeVisitor.Stub
	private final TileDiskDrive m_diskDrive;

	@MergeVisitor.Stub
	public DiskDrivePeripheral_Patch(TileDiskDrive diskDrive) {
		super(diskDrive);
		m_diskDrive = null;
	}

	@Override
	public Object getTarget() {
		return m_diskDrive;
	}
}
