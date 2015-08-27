package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.client.render.TileEntityTurtleRenderer;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.turtle.blocks.ITurtleTile;
import dan200.computercraft.shared.turtle.items.ITurtleItem;
import dan200.computercraft.shared.util.Colour;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.squiddev.cctweaks.client.render.Camera;
import org.squiddev.cctweaks.client.render.RenderInfo;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.registry.Registry;
import org.squiddev.patcher.visitors.MergeVisitor;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
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

	//region Turtle wand
	protected boolean hasTurtleWand() {
		ItemStack stack = Minecraft.getMinecraft().thePlayer.getHeldItem();
		return stack != null && stack.getItem() == Registry.itemTurtleWand;
	}

	protected void scale(TileEntity tile) {
		if (!hasTurtleWand()) return;

		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);

		GL11.glTranslatef(0.5f, 0.5f, 0.5f);

		double scale = getScaleForCandidate(new Vector3d(tile.xCoord, tile.yCoord, tile.zCoord));
		GL11.glScaled(scale, scale, scale);
		GL11.glTranslatef(-0.5f, -0.5f, -0.5f);
	}

	protected void postScale() {
		if (!hasTurtleWand()) return;

		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);
	}

	/*
		This is adapted from EnderIO (https://github.com/SleepyTrousers/EnderIO/)
	 */
	public static double getScaleForCandidate(Vector3d loc) {
		Camera view = RenderInfo.getView();
		if (!view.isValid()) return 1.0d;

		float ratio;
		{
			Vector2d sp = view.getScreenPoint(new Vector3d(loc.x + 0.5, loc.y + 0.5, loc.z + 0.5));
			sp.sub(RenderInfo.getMidpoint());
			double d = sp.length();
			if (d != d) {
				d = 0f;
			}

			ratio = (float) d / Minecraft.getMinecraft().displayWidth;
		}

		//smoothly zoom to a larger size, starting when the point is the middle 20% of the screen
		float start = 0.2f;
		float end = 0.01f;
		double mix = MathHelper.clamp_float((start - ratio) / (start - end), 0, 1);
		double scale = 1;
		if (mix > 0) {
			loc.sub(RenderInfo.getEyePosition(Minecraft.getMinecraft().thePlayer));
			double distance = Math.sqrt(loc.lengthSquared());
			scale = RenderInfo.getTanFovRad() * distance;

			//Using this scale will give us the block full screen, we will make it 20% of the screen
			scale *= 0.2;

			//only apply 70% of the scaling so more distance targets are still smaller than closer targets
			scale *= 1 - 0.7 * MathHelper.clamp_float((float) distance / 128, 0, 1);

			scale = Math.max(1, (scale * mix) + (1 - mix));

			// DebugLogger.debug("Screen distance: %d, Actual distance: %d, scale: %d, mix: %d", d, Math.sqrt(lSquared), mix);
		}

		return scale;
	}
	//endregion

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
