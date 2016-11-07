package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.core.ServerComputerRegistry;
import org.squiddev.patcher.visitors.MergeVisitor;

/**
 * Make the server computer registry thread safe
 */
public class ServerComputerRegistry_Patch extends ServerComputerRegistry {
	public synchronized void update() {
		native_update();
	}

	public synchronized void add(int instanceID, ServerComputer computer) {
		native_add(instanceID, computer);
	}

	public synchronized void remove(int instanceID) {
		native_remove(instanceID);
	}

	@MergeVisitor.Stub
	@MergeVisitor.Rename(from = "update")
	private void native_update() {
	}

	@MergeVisitor.Stub
	@MergeVisitor.Rename(from = "add")
	private void native_add(int instanceID, ServerComputer computer) {
	}

	@MergeVisitor.Stub
	@MergeVisitor.Rename(from = "remove")
	private void native_remove(int instanceID) {
	}
}
