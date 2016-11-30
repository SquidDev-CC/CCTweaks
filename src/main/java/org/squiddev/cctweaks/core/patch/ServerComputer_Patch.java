package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.shared.computer.core.ServerComputer;
import org.squiddev.cctweaks.lua.patch.Computer_Patch;
import org.squiddev.patcher.visitors.MergeVisitor;

/**
 * Add isMostlyOn method
 */
@MergeVisitor.Rename(
	from = "org/squiddev/cctweaks/lua/patch/Computer_Patch",
	to = "dan200/computercraft/core/computer/Computer"
)
public class ServerComputer_Patch extends ServerComputer {
	@MergeVisitor.Stub
	private Computer_Patch m_computer;

	@MergeVisitor.Stub
	public ServerComputer_Patch() {
		super(null, -1, null, -1, null, -1, -1);
	}

	public boolean isMostlyOn() {
		return m_computer.isMostlyOn();
	}
}
