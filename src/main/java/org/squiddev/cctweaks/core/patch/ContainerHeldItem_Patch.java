package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.core.ServerComputerRegistry;
import dan200.computercraft.shared.media.inventory.ContainerHeldItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.cctweaks.api.IContainerComputer;
import org.squiddev.patcher.visitors.MergeVisitor;

import javax.annotation.Nullable;

/**
 * Implements {@link IContainerComputer} for the pocket computer container.
 *
 * Note: this container is also used by printed pages so we have to check it is a pocket computer first.
 */
public abstract class ContainerHeldItem_Patch extends ContainerHeldItem implements IContainerComputer {
	@MergeVisitor.Stub
	public ContainerHeldItem_Patch() {
		super(null, null);
	}

	@Nullable
	@Override
	public IComputer getComputer() {
		ItemStack stack = getStack();
		if (stack == null) return null;

		NBTTagCompound compound = stack.getTagCompound();
		if (compound == null) return null;

		if (!compound.hasKey("instanceID", 99) || !compound.hasKey("sessionID", 99)) return null;

		int instanceId = compound.getInteger("instanceID");
		int sessionId = compound.getInteger("sessionID");

		ServerComputerRegistry registry = ComputerCraft.serverComputerRegistry;
		return registry.getSessionID() == sessionId ? registry.get(instanceId) : null;
	}
}
