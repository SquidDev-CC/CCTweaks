package org.squiddev.cctweaks.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.registry.IClientModule;
import org.squiddev.cctweaks.core.registry.Module;
import org.squiddev.cctweaks.core.registry.Registry;
import org.squiddev.cctweaks.core.visualiser.VisualisationData;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

import static org.squiddev.cctweaks.core.visualiser.VisualisationData.Connection;
import static org.squiddev.cctweaks.core.visualiser.VisualisationData.Node;

/**
 * This is a helper to render a network when testing.
 */
public final class RenderNetworkOverlay extends Module implements IClientModule {
	public int ticksInGame;
	public static VisualisationData data;

	@SubscribeEvent
	public void onWorldRenderLast(RenderWorldLastEvent event) {
		++ticksInGame;
		if (data == null) return;

		Minecraft minecraft = Minecraft.getMinecraft();
		ItemStack stack = minecraft.thePlayer.getHeldItem();
		if (stack == null || stack.getItem() != Registry.itemDebugger) return;

		GL11.glPushMatrix();
		RenderManager renderManager = minecraft.getRenderManager();
		GL11.glTranslated(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ);

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		renderNetwork(data, new Color(Color.HSBtoRGB(ticksInGame % 200 / 200F, 0.6F, 1F)), 1f);

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
	}

	private void renderNetwork(VisualisationData data, Color color, float thickness) {
		MovingObjectPosition position = Minecraft.getMinecraft().objectMouseOver;

		Set<Node> nodes = new HashSet<Node>();

		for (Connection connection : data.connections) {
			Node a = connection.x, b = connection.y;

			if (a.position != null && b.position != null) {
				renderConnection(a.position, b.position, color, thickness);
			}

			// We render a label of all nodes at this point.
			if (position.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
				if (a.position != null) {
					if (a.position.equals(position.getBlockPos())) {
						nodes.add(a);
						if (b.position == null) nodes.add(b);
					}
				}

				if (b.position != null) {
					if (b.position.equals(position.getBlockPos())) {
						if (a.position == null) nodes.add(a);
						nodes.add(b);
					}
				}
			}
		}

		// Custom handling for networks with 0 connections (single node networks)
		if (data.connections.length == 0 && position.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
			for (Node node : data.nodes) {
				BlockPos nodePosition = node.position;
				if (nodePosition != null && nodePosition.equals(position.getBlockPos())) {
					nodes.add(node);
				}
			}
		}

		int counter = 0;
		for (Node node : nodes) {
			String name = node.position == null ? "\u00a78" + node.name : node.name;
			BlockPos pos = position.getBlockPos();
			renderLabel(pos.getX() + 0.5, pos.getY() + 1.5 + (counter++) * 0.4, pos.getZ() + 0.5, name);
		}
	}

	public void renderConnection(BlockPos aNode, BlockPos bNode, Color color, float thickness) {
		GL11.glPushMatrix();
		GL11.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(), (byte) 255);

		GL11.glScalef(1, 1, 1);

		GL11.glLineWidth(thickness);
		renderLine(aNode, bNode);

		GL11.glLineWidth(thickness * 3);
		GL11.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(), (byte) 64);
		renderLine(aNode, bNode);

		GL11.glPopMatrix();
	}

	private void renderLabel(double x, double y, double z, String label) {
		RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
		FontRenderer fontrenderer = renderManager.getFontRenderer();
		if (fontrenderer == null) return;

		float scale = 0.02666667f;
		GL11.glPushMatrix();

		GL11.glTranslated(x, y, z);
		GL11.glRotatef(-renderManager.playerViewY, 0, 1, 0);
		GL11.glRotatef(renderManager.playerViewX, 1, 0, 0);
		GL11.glScalef(-scale, -scale, scale);

		GL11.glDisable(GL11.GL_LIGHTING);

		WorldRenderer tessellator = Tessellator.getInstance().getWorldRenderer();

		int width = fontrenderer.getStringWidth(label);
		int xOffset = width / 2;

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA(0, 0, 0, 65);
		tessellator.addVertex(-xOffset - 1, -1, 0);
		tessellator.addVertex(-xOffset - 1, 8, 0);
		tessellator.addVertex(xOffset + 1, 8, 0);
		tessellator.addVertex(xOffset + 1, -1, 0);
		tessellator.finishDrawing();

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		fontrenderer.drawString(label, -width / 2, 0, 0xFFFFFFFF);

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glColor4f(1, 1, 1, 1);

		GL11.glPopMatrix();
	}

	private void renderLine(BlockPos a, BlockPos b) {
		WorldRenderer tessellator = Tessellator.getInstance().getWorldRenderer();
		tessellator.startDrawing(GL11.GL_LINES);
		tessellator.addVertex(a.getX() + 0.5, a.getY() + 0.5, a.getZ() + 0.5);
		tessellator.addVertex(b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5);
		tessellator.finishDrawing();
	}

	@Override
	public boolean canLoad() {
		return super.canLoad() && Config.Computer.debugWandEnabled;
	}

	@Override
	public void clientInit() {
		MinecraftForge.EVENT_BUS.register(this);
	}
}
