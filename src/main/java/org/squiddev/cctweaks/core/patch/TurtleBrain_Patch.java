package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import dan200.computercraft.shared.turtle.core.TurtleBrain;
import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.cctweaks.api.turtle.IExtendedTurtleUpgrade;
import org.squiddev.patcher.visitors.MergeVisitor;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Adds upgrade changed handler
 */
public class TurtleBrain_Patch extends TurtleBrain {
	@MergeVisitor.Stub
	private Map<TurtleSide, ITurtleUpgrade> m_upgrades;
	@MergeVisitor.Stub
	private Map<TurtleSide, NBTTagCompound> m_upgradeNBTData;
	@MergeVisitor.Stub
	private TileTurtle m_owner;

	@MergeVisitor.Stub
	public TurtleBrain_Patch(TileTurtle turtle) {
		super(turtle);
	}

	@Override
	public void setUpgrade(@Nonnull TurtleSide side, ITurtleUpgrade upgrade) {
		ITurtleUpgrade oldUpgrade = m_upgrades.get(side);
		if (oldUpgrade == upgrade) {
			return;
		} else if (oldUpgrade != null) {
			m_upgrades.remove(side);
		}

		if (m_upgradeNBTData.containsKey(side)) {
			m_upgradeNBTData.remove(side);
		}

		if (upgrade != null) m_upgrades.put(side, upgrade);

		if (m_owner.getWorld() != null) {
			updatePeripherals(m_owner.createServerComputer());
			m_owner.updateBlock();

			if (!m_owner.getWorld().isRemote) {
				TurtleSide otherSide = side == TurtleSide.Left ? TurtleSide.Right : TurtleSide.Left;
				ITurtleUpgrade other = getUpgrade(otherSide);
				if (other != null && other instanceof IExtendedTurtleUpgrade) {
					((IExtendedTurtleUpgrade) other).upgradeChanged(this, otherSide, oldUpgrade, upgrade);
				}
			}
		}
	}
}
