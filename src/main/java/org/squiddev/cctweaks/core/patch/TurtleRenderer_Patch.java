package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.client.render.TileEntityTurtleRenderer;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.turtle.blocks.ITurtleTile;
import dan200.computercraft.shared.turtle.items.ITurtleItem;
import dan200.computercraft.shared.util.Colour;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.patcher.visitors.MergeVisitor;

import java.util.List;

/**
 * Various patches to the turtle renderer
 */
public abstract class TurtleRenderer_Patch extends TileEntityTurtleRenderer {
	@Override
	public void renderInventoryTurtle(ItemStack stack) {
		if ((stack.getItem() instanceof ITurtleItem)) {
			ITurtleItem item = (ITurtleItem) stack.getItem();
			applyCustomNames(item.getLabel(stack), true);
			renderTurtle(null, item.getFamily(stack), item.getColour(stack), item.getUpgrade(stack, TurtleSide.Left), item.getUpgrade(stack, TurtleSide.Right), 0.0f, 0.0f, null, null);
		}
	}

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

	/**
	 * I'm leaving this here because I might implement it.
	 * However it will be pretty hard to get it working.
	 *
	 * This current function will not work as {@link ComputerCraft#getTurtleModelTextures(List, ComputerFamily, Colour)}
	 * requires a colour being set and we wouldn't calculate the list until later
	 */
	private void applyColour(final String label, final Colour colour) {
		if (Config.Turtle.funNames && label != null && label.equals("jeb_")) {
			int existed = 0;

			final int loops = existed / 25;
			final float factor = existed % 25 / 25.0F;
			final float[] partA = EntitySheep.fleeceColorTable[loops % EntitySheep.fleeceColorTable.length];
			final float[] partB = EntitySheep.fleeceColorTable[(loops + 1) % EntitySheep.fleeceColorTable.length];

			GL11.glColor4f(
				partA[0] * (1.0f - factor) + partB[0] * factor,
				partA[1] * (1.0f - factor) + partB[1] * factor,
				partA[2] * (1.0f - factor) + partB[2] * factor,
				1.0f
			);
		} else if (colour != null) {
			GL11.glColor4f(colour.getR(), colour.getG(), colour.getB(), 1.0f);
		} else {
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		}
	}

	@MergeVisitor.Stub
	private void renderTurtle(ITurtleTile a, ComputerFamily b, Colour c, ITurtleUpgrade d, ITurtleUpgrade e, float f, float g, ResourceLocation overlay, ResourceLocation hatOverlay) {
	}
}
