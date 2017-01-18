package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.shared.computer.core.ComputerRegistry;
import dan200.computercraft.shared.computer.core.ServerComputer;
import org.squiddev.patcher.visitors.MergeVisitor;

/**
 * Make the server computer registry thread safe
 */
@MergeVisitor.Rename(
	from = {"org/squiddev/cctweaks/core/patch/ServerComputer_Patch"},
	to = {"dan200/computercraft/shared/computer/core/ServerComputer"}
)
public class ServerComputerRegistry_Patch extends ComputerRegistry<ServerComputer> {
	public synchronized void update() {
		native_update();
	}

	public synchronized void add(int instanceID, ServerComputer computer) {
		super.add(instanceID, computer);

		// We force sending the state of the computer
		((ServerComputer_Patch) computer).broadcastState(true);
	}

	public synchronized void remove(int instanceID) {
		native_remove(instanceID);
	}

	@MergeVisitor.Stub
	@MergeVisitor.Rename(from = "update")
	private void native_update() {
	}

	@MergeVisitor.Stub
	@MergeVisitor.Rename(from = "remove")
	private void native_remove(int instanceID) {
	}
}
