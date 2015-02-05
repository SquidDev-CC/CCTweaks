package squiddev.cctweaks.core.turtle;

import dan200.computercraft.api.turtle.ITurtleAccess;
import net.minecraft.item.ItemStack;

public interface ITurtleRefuelSource {
	public boolean canRefuel(ITurtleAccess turtle, ItemStack stack, int limit);

	public int refuel(ITurtleAccess turtle, ItemStack stack, int limit);
}
