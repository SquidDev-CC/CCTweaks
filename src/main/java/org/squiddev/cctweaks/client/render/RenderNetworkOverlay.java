package org.squiddev.cctweaks.client.render;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.registry.IClientModule;
import org.squiddev.cctweaks.core.registry.Module;
import org.squiddev.cctweaks.core.registry.Registry;
import org.squiddev.cctweaks.core.visualiser.VisualisationData;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

import static org.squiddev.cctweaks.core.visualiser.VisualisationData.*;

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

		ItemStack stack = Minecraft.getMinecraft().thePlayer.getHeldItem();
		if (stack == null || stack.getItem() != Registry.itemDebugger) return;

		GL11.glPushMatrix();
		GL11.glTranslated(-RenderManager.renderPosX, -RenderManager.renderPosY, -RenderManager.renderPosZ);

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
					Position nodePosition = a.position;
					if (nodePosition.x == position.blockX && nodePosition.y == position.blockY && nodePosition.z == position.blockZ) {
						nodes.add(a);
						if (b.position == null) nodes.add(b);
					}
				}

				if (b.position != null) {
					Position nodePosition = b.position;
					if (nodePosition.x == position.blockX && nodePosition.y == position.blockY && nodePosition.z == position.blockZ) {
						if (a.position == null) nodes.add(a);
						nodes.add(b);
					}
				}
			}
		}

		// Custom handling for networks with 0 connections (single node networks)
		if (data.connections.length == 0 && position.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
			for (Node node : nodes) {
				Position nodePosition = node.position;
				if (nodePosition != null && nodePosition.x == position.blockX && nodePosition.y == position.blockY && nodePosition.z == position.blockZ) {
					nodes.add(node);
				}
			}
		}

		int counter = 0;
		boolean sneaking = Minecraft.getMinecraft().thePlayer.isSneaking();
		for (Node node : nodes) {
			if (sneaking) {
				for (String peripheral : node.peripherals) {
					renderLabel(position.blockX + 0.5, position.blockY + 1.5 + (counter++) * 0.4, position.blockZ + 0.5, "\u00a71" + peripheral);
				}
			}

			String name = node.position == null ? "\u00a78" + node.name : node.name;
			renderLabel(position.blockX + 0.5, position.blockY + 1.5 + (counter++) * 0.4, position.blockZ + 0.5, name);
		}
	}

	public void renderConnection(Position aNode, Position bNode, Color color, float thickness) {
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
		if (label == null) return;

		RenderManager renderManager = RenderManager.instance;
		FontRenderer fontrenderer = renderManager.getFontRenderer();
		if (fontrenderer == null) return;

		float scale = 0.02666667f;
		GL11.glPushMatrix();

		GL11.glTranslated(x, y, z);
		GL11.glRotatef(-renderManager.playerViewY, 0, 1, 0);
		GL11.glRotatef(renderManager.playerViewX, 1, 0, 0);
		GL11.glScalef(-scale, -scale, scale);

		GL11.glDisable(GL11.GL_LIGHTING);

		Tessellator tessellator = Tessellator.instance;

		int width = fontrenderer.getStringWidth(label);
		int xOffset = width / 2;

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA(0, 0, 0, 65);
		tessellator.addVertex(-xOffset - 1, -1, 0);
		tessellator.addVertex(-xOffset - 1, 8, 0);
		tessellator.addVertex(xOffset + 1, 8, 0);
		tessellator.addVertex(xOffset + 1, -1, 0);
		tessellator.draw();

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		fontrenderer.drawString(label, -width / 2, 0, 0xFFFFFFFF);

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glColor4f(1, 1, 1, 1);

		GL11.glPopMatrix();
	}

	private void renderLine(Position a, Position b) {
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawing(GL11.GL_LINES);
		tessellator.addVertex(a.x + 0.5, a.y + 0.5, a.z + 0.5);
		tessellator.addVertex(b.x + 0.5, b.y + 0.5, b.z + 0.5);
		tessellator.draw();
	}

	@Override
	public boolean canLoad() {
		return super.canLoad() && Config.Testing.debug && Config.Computer.debugWandEnabled;
	}

	@Override
	public void clientInit() {
		MinecraftForge.EVENT_BUS.register(this);
	}
}
