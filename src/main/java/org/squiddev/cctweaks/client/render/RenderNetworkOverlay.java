package org.squiddev.cctweaks.client.render;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import org.lwjgl.opengl.GL11;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.SingleTypeUnorderedPair;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.NetworkAPI;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.registry.IClientModule;
import org.squiddev.cctweaks.core.registry.Module;
import org.squiddev.cctweaks.core.registry.Registry;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * This is a helper to render a network when testing.
 * It will only work on a single player.
 *
 * TODO: Sync connections between client and server.
 */
public final class RenderNetworkOverlay extends Module implements IClientModule {
	public int ticksInGame;
	public INetworkController controller;
	public World theWorld;

	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event) {
		/**
		 * This is the really hacky bit - we don't store network states on the server.
		 * and so we just grab it from an event
		 */
		if (!event.world.isRemote && event.entity instanceof EntityPlayer) theWorld = event.world;
	}

	@SubscribeEvent
	public void onWorldRenderLast(RenderWorldLastEvent event) {
		++ticksInGame;
		if (theWorld == null) return;

		ItemStack stack = Minecraft.getMinecraft().thePlayer.getHeldItem();
		if (stack != null && stack.getItem() == Registry.itemDebugger) {
			MovingObjectPosition position = Minecraft.getMinecraft().objectMouseOver;

			if (position.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
				IWorldNetworkNode node = NetworkAPI.registry().getNode(theWorld, position.blockX, position.blockY, position.blockZ);
				if (node != null && node.getAttachedNetwork() != null) {
					controller = node.getAttachedNetwork();
				}
			}
		} else {
			return;
		}

		if (controller == null) return;

		GL11.glPushMatrix();
		GL11.glTranslated(-RenderManager.renderPosX, -RenderManager.renderPosY, -RenderManager.renderPosZ);

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		renderNetwork(controller.getNodeConnections(), new Color(Color.HSBtoRGB(ticksInGame % 200 / 200F, 0.6F, 1F)), 1f);

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
	}

	private void renderNetwork(Set<SingleTypeUnorderedPair<INetworkNode>> connections, Color color, float thickness) {
		World world = theWorld;
		MovingObjectPosition position = Minecraft.getMinecraft().objectMouseOver;

		Set<INetworkNode> nodes = new HashSet<INetworkNode>();

		for (SingleTypeUnorderedPair<INetworkNode> connection : connections) {
			INetworkNode a = connection.x, b = connection.y;

			if (a instanceof IWorldNetworkNode && b instanceof IWorldNetworkNode) {
				IWorldNetworkNode aNode = (IWorldNetworkNode) a, bNode = (IWorldNetworkNode) b;
				IBlockAccess aWorld = aNode.getPosition().getWorld(), bWorld = bNode.getPosition().getWorld();

				if (aWorld == world && bWorld == world) {
					renderConnection(aNode, bNode, color, thickness);
				}
			}

			// We render a label of all nodes at this point.
			if (position.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
				if (a instanceof IWorldNetworkNode) {
					IWorldPosition nodePosition = ((IWorldNetworkNode) a).getPosition();
					if (nodePosition.getWorld() == world && nodePosition.getX() == position.blockX && nodePosition.getY() == position.blockY && nodePosition.getZ() == position.blockZ) {
						nodes.add(a);
						if (!(b instanceof IWorldNetworkNode)) nodes.add(b);
					}
				}

				if (b instanceof IWorldNetworkNode) {
					IWorldPosition nodePosition = ((IWorldNetworkNode) b).getPosition();
					if (nodePosition.getWorld() == world && nodePosition.getX() == position.blockX && nodePosition.getY() == position.blockY && nodePosition.getZ() == position.blockZ) {
						if (!(a instanceof IWorldNetworkNode)) nodes.add(a);
						nodes.add(b);
					}
				}
			}
		}

		int counter = 0;
		for (INetworkNode node : nodes) {
			renderLabel(position.blockX + 0.5, position.blockY + 1.5 + (counter++) * 0.4, position.blockZ + 0.5, node.toString());
		}
	}

	public void renderConnection(IWorldNetworkNode aNode, IWorldNetworkNode bNode, Color color, float thickness) {
		IWorldPosition aPos = aNode.getPosition();
		IWorldPosition bPos = bNode.getPosition();

		GL11.glPushMatrix();
		GL11.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(), (byte) 255);

		GL11.glScalef(1, 1, 1);

		GL11.glLineWidth(thickness);
		renderLine(aPos, bPos);

		GL11.glLineWidth(thickness * 3);
		GL11.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(), (byte) 64);
		renderLine(aPos, bPos);

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

	private void renderLine(IWorldPosition a, IWorldPosition b) {
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawing(GL11.GL_LINES);
		tessellator.addVertex(a.getX() + 0.5, a.getY() + 0.5, a.getZ() + 0.5);
		tessellator.addVertex(b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5);
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
