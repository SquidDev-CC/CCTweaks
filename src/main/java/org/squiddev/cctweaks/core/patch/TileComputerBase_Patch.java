package org.squiddev.cctweaks.core.patch;

import com.google.common.base.Objects;
import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import dan200.computercraft.shared.computer.core.ClientComputer;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.patcher.visitors.MergeVisitor;

/**
 * Ensures NBT data sync
 */
@MergeVisitor.Rename(
	from = "org/squiddev/cctweaks/core/patch/ServerComputer_Patch",
	to = "dan200/computercraft/shared/computer/core/ServerComputer"
)
public abstract class TileComputerBase_Patch extends TileComputerBase {
	@MergeVisitor.Stub
	public abstract ServerComputer_Patch createServerComputer();

	/**
	 * Detect whether various methods have changed and mark as dirty if so.
	 */
	public void update() {
		if (!worldObj.isRemote) {
			ServerComputer_Patch computer = this.createServerComputer();
			if (computer != null) {
				boolean oldOn = m_on || m_startOn;

				if (m_startOn) {
					computer.turnOn();
					m_startOn = false;
				}

				computer.keepAlive();
				if (computer.hasOutputChanged()) updateOutput();

				boolean changed = false;

				// Sync various properties, ensuring that the data is saved if required

				int id = computer.getID();
				int oldId = m_computerID;
				if (id != oldId) {
					m_computerID = id;
					changed = true;
				}

				String label = computer.getLabel();
				String oldLabel = m_label;
				if (!Objects.equal(label, oldLabel)) {
					m_label = label;
					changed = true;
				}

				boolean on = computer.isMostlyOn();
				if (on != oldOn) {
					m_on = on;
					changed = true;
				}

				if (changed) {
					DebugLogger.debug("Updating status: id=%s->%s, label=%s->%s, on=%s->%s", oldId, id, oldLabel, label, oldOn, on);
					markDirty();
				}
			}
		} else {
			ClientComputer computer = createClientComputer();
			if (computer != null && computer.hasOutputChanged()) updateBlock();
		}
	}
}
