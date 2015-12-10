package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.client.render.TileEntityTurtleRenderer;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.turtle.blocks.ITurtleTile;
import dan200.computercraft.shared.util.Colour;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.patcher.visitors.MergeVisitor;

/**
 * Various patches to the turtle renderer
 */
public abstract class TurtleRenderer_Patch extends TileEntityTurtleRenderer {
	/*
	@Override
	public void renderInventoryTurtle(ItemStack stack) {
		if ((stack.getItem() instanceof ITurtleItem)) {
			ITurtleItem item = (ITurtleItem) stack.getItem();
			applyCustomNames(item.getLabel(stack), true);
			renderTurtle(null, item.getFamily(stack), item.getColour(stack), item.getUpgrade(stack, TurtleSide.Left), item.getUpgrade(stack, TurtleSide.Right), 0.0f, 0.0f, null, null);
		}
	}
	*/

	/**
	 * Apply custom names to this turtle
	 *
	 * @param label The turtle's label
	 * @param shift If we should undo a previous shift (such as for items)
	 */
	protected void applyCustomNames(String label, boolean shift) {
		if (Config.Turtle.funNames && label != null) {
			if (label.equals("Dinnerbone") || label.equals("Grumm")) {
				if (shift) GL11.glTranslatef(0.5f, 0.5f, 0.5f);
				GL11.glRotatef(180.0f, 0.0f, 0.0f, 1.0f);
				if (shift) GL11.glTranslatef(-0.5f, -0.5f, -0.5f);
			}
		}
	}

	protected void applyCustomNames(String label) {
		applyCustomNames(label, false);
	}

	@MergeVisitor.Stub
	private void renderTurtle(ITurtleTile a, ComputerFamily b, Colour c, ITurtleUpgrade d, ITurtleUpgrade e, float f, float g, ResourceLocation overlay, ResourceLocation hatOverlay) {
	}
}
