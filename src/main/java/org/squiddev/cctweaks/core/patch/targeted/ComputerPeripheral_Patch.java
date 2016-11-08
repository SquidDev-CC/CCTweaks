package org.squiddev.cctweaks.core.patch.targeted;

import dan200.computercraft.shared.computer.blocks.ComputerPeripheral;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.peripheral.IPeripheralTargeted;
import org.squiddev.patcher.visitors.MergeVisitor;

public class ComputerPeripheral_Patch extends ComputerPeripheral implements IPeripheralTargeted {
	@MergeVisitor.Stub
	private ServerComputer m_computer;

	@MergeVisitor.Stub
	public ComputerPeripheral_Patch(String type, ServerComputer computer) {
		super(type, computer);
	}

	@Override
	public Object getTarget() {
		if (m_computer == null) return null;

		World world = m_computer.getWorld();
		BlockPos pos = m_computer.getPosition();

		if (world == null || pos == null) return null;

		return world.getTileEntity(pos);
	}
}
