package squiddev.cctweaks.asm;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.TurtleAnimation;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import net.minecraft.item.ItemStack;
import squiddev.cctweaks.registry.TurtleTweakRegistry;
import squiddev.cctweaks.turtle.ITurtleRefuelSource;

public class TurtleRefuelCommand_Tweak {
	private int m_limit = 0;
	public TurtleRefuelCommand_Tweak() {
		m_limit = 3;
	}

	public TurtleCommandResult execute(ITurtleAccess turtle)
	{
		ItemStack stack = turtle.getInventory().getStackInSlot(turtle.getSelectedSlot());
		for(ITurtleRefuelSource source : TurtleTweakRegistry.refuelList) {
			if(source.canRefuel(turtle, stack, m_limit)) {
				if(m_limit == 0) {
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
