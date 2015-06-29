package org.squiddev.cctweaks.core.patch.targeted;

import dan200.computercraft.shared.computer.blocks.ComputerPeripheral;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.peripheral.IPeripheralTargeted;
import org.squiddev.patcher.visitors.MergeVisitor;

public class ComputerPeripheral_Patch extends ComputerPeripheral implements IPeripheralTargeted {
	@MergeVisitor.Stub
	private World m_world;
	@MergeVisitor.Stub
	private ChunkCoordinates m_position;

	@MergeVisitor.Stub
	public ComputerPeripheral_Patch(String type, ServerComputer computer) {
		super(type, computer);
	}

	@Override
	public Object getTarget() {
		return m_position != null && m_world != null ? m_world.getTileEntity(m_position.posX, m_position.posY, m_position.posZ) : null;
	}
}
