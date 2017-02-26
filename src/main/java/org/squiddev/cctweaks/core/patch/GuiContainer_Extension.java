package org.squiddev.cctweaks.core.patch;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import org.lwjgl.input.Keyboard;
import org.squiddev.patcher.visitors.MergeVisitor;

import java.io.IOException;

public abstract class GuiContainer_Extension extends GuiContainer {
	@MergeVisitor.Stub
	public GuiContainer_Extension(Container inventorySlotsIn) {
		super(inventorySlotsIn);
	}

	@Override
	public void handleInput() throws IOException {
		// JEI incorrectly sets the repeat events filter, so we force it here.
		boolean previous = Keyboard.areRepeatEventsEnabled();
		if (!previous) Keyboard.enableRepeatEvents(true);

		super.handleInput();

		if (!previous) Keyboard.enableRepeatEvents(false);
	}
}
