package org.squiddev.cctweaks.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.registry.IClientModule;
import org.squiddev.cctweaks.core.registry.Module;

import java.util.Map;

public class RenderSquidOverlay extends Module implements IClientModule, LayerRenderer<EntityPlayer> {
	private static final int SEGMENTS = 5;
	private static final int TENTACLES = 6;

	// Dimensions of the one tentacle
	private static final float LENGTH = 0.3f;
	private static final float WIDTH = 0.15f;

	private static final double EASING_TICKS = 5;
	private static final double OFFSET_SPEED = 0.1;
	private static final double OFFSET_VARIANCE = 3;

	private final double[] lastAngles = new double[TENTACLES];
	private final double[] offsets = new double[TENTACLES];

	private double tick = 0;

	@Override
	public void clientInit() {
		Map<String, RenderPlayer> skinMap = Minecraft.getMinecraft().getRenderManager().getSkinMap();
		skinMap.get("default").addLayer(this);
		skinMap.get("slim").addLayer(this);

		for (int i = 0; i < TENTACLES; i++) {
			lastAngles[i] = 30;
			offsets[i] = Math.random() * Math.PI * 2;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void doRenderLayer(EntityPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		String name = player.getDisplayName().getUnformattedText();
		if (!name.equals("SquidDev") || !Config.Misc.funRender) return;

		GlStateManager.disableLighting();
		GlStateManager.disableTexture2D();

		GlStateManager.pushMatrix();
		if (player.isSneaking()) {
			GlStateManager.translate(0F, 0.2F, 0F);
			GlStateManager.rotate(90F / (float) Math.PI, 1.0F, 0.0F, 0.0F);
		}

		GlStateManager.rotate(90, 1, 0, 0);
		GlStateManager.translate(0, 0.1, -0.3);

		GlStateManager.color(0, 0, 0, 1);

		final double angle;

		if (player.isSprinting()) {
			angle = 5;
		} else if (player.hurtTime > 0) {
			double progress = (double) player.hurtTime / player.maxHurtTime;
			angle = 30 - (progress * 25);
		} else {
			angle = 30;
		}

		tick = (tick + partialTicks) % (Math.PI * 2 / OFFSET_SPEED);

		for (int i = 0; i < TENTACLES; i++) {
			// Offset each tentacle by a random amount
			double lastAngle = lastAngles[i];
			double thisAngle = angle + Math.sin(offsets[i] + tick * OFFSET_SPEED) * OFFSET_VARIANCE;

			// Provide some basic easing on the angle
			if (Math.abs(lastAngle - thisAngle) > 1) {
				thisAngle = lastAngle - (lastAngle - thisAngle) / EASING_TICKS;
			}

			lastAngles[i] = thisAngle;

			GlStateManager.pushMatrix();

			GlStateManager.rotate(360.0f / TENTACLES * i, 0, 1, 0);
			GlStateManager.translate(0.1, 0, 0);

			GlStateManager.rotate((float) thisAngle, 0, 0, -1);

			Tessellator tessellator = Tessellator.getInstance();
			WorldRenderer renderer = tessellator.getWorldRenderer();

			for (int j = 0; j < SEGMENTS; j++) {
				renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
				tentacle(renderer);
				tessellator.draw();

				GlStateManager.translate(0, LENGTH, 0);
				GlStateManager.rotate((float) thisAngle, 0, 0, -1);
			}

			GlStateManager.popMatrix();
		}

		GlStateManager.popMatrix();

		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldCombineTextures() {
		return false;
	}

	private static void tentacle(WorldRenderer renderer) {
		renderer.pos(0, 0, -WIDTH / 2).endVertex();
		renderer.pos(0, 0, WIDTH / 2).endVertex();
		renderer.pos(0, LENGTH, WIDTH / 2).endVertex();
		renderer.pos(0, LENGTH, -WIDTH / 2).endVertex();

		renderer.pos(0, 0, -WIDTH / 2).endVertex();
		renderer.pos(0, 0, WIDTH / 2).endVertex();
		renderer.pos(WIDTH, 0, WIDTH / 2).endVertex();
		renderer.pos(WIDTH, 0, -WIDTH / 2).endVertex();

		renderer.pos(0, 0, -WIDTH / 2).endVertex();
		renderer.pos(0, LENGTH, -WIDTH / 2).endVertex();
		renderer.pos(WIDTH, LENGTH, -WIDTH / 2).endVertex();
		renderer.pos(WIDTH, 0, -WIDTH / 2).endVertex();

		renderer.pos(WIDTH, 0, -WIDTH / 2).endVertex();
		renderer.pos(WIDTH, 0, WIDTH / 2).endVertex();
		renderer.pos(WIDTH, LENGTH, WIDTH / 2).endVertex();
		renderer.pos(WIDTH, LENGTH, -WIDTH / 2).endVertex();

		renderer.pos(0, LENGTH, -WIDTH / 2).endVertex();
		renderer.pos(0, LENGTH, WIDTH / 2).endVertex();
		renderer.pos(WIDTH, LENGTH, WIDTH / 2).endVertex();
		renderer.pos(WIDTH, LENGTH, -WIDTH / 2).endVertex();

		renderer.pos(0, 0, WIDTH / 2).endVertex();
		renderer.pos(0, LENGTH, WIDTH / 2).endVertex();
		renderer.pos(WIDTH, LENGTH, WIDTH / 2).endVertex();
		renderer.pos(WIDTH, 0, WIDTH / 2).endVertex();
	}
}
