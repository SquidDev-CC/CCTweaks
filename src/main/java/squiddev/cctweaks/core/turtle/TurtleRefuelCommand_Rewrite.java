package squiddev.cctweaks.core.turtle;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleCommand;
import dan200.computercraft.api.turtle.TurtleAnimation;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import net.minecraft.item.ItemStack;
import squiddev.cctweaks.core.registry.TurtleRefuelList;

/**
 * Complete rewrite of {@link dan200.computercraft.shared.turtle.core.TurtleRefuelCommand}
 * Uses the turtle refuel registry instead {@link TurtleRefuelList}.
 */
@SuppressWarnings("unused")
public class TurtleRefuelCommand_Rewrite implements ITurtleCommand {
	private int m_limit = 0;

	public TurtleRefuelCommand_Rewrite(int limit) {
		m_limit = limit;
	}

	public TurtleCommandResult execute(ITurtleAccess turtle) {
		ItemStack stack = turtle.getInventory().getStackInSlot(turtle.getSelectedSlot());
		if (stack == null) {
			return TurtleCommandResult.failure("No items to combust");
		}

		for (ITurtleRefuelSource source : TurtleRefuelList.refuelList) {
			if (source.canRefuel(turtle, stack, m_limit)) {
				if (m_limit == 0) {
					return TurtleCommandResult.success();
				} else {
					turtle.addFuel(source.refuel(turtle, stack, m_limit));
					turtle.playAnimation(TurtleAnimation.Wait);
					return TurtleCommandResult.success();
				}
			}
		}

		return TurtleCommandResult.failure("Cannot refuel from this");
	}
}
