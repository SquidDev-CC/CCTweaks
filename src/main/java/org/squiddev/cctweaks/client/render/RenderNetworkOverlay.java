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
			// Deselect if the current item isn't the debugger
			controller = null;
		}

		if (controller == null) return;

		GL11.glPushMatrix();
		GL11.glTranslated(-RenderManager.renderPosX, -RenderManager.renderPosY, -RenderManager.renderPosZ);

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);

		renderNetwork(controller.getNodeConnections(), new Color(Color.HSBtoRGB(ticksInGame % 200 / 200F, 0.6F, 1F)), 1f);

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
	}

	private void renderNetwork(Set<SingleTypeUnorderedPair<INetworkNode>> connections, Color color, float thickness) {
		World world = theWorld;

		for (SingleTypeUnorderedPair<INetworkNode> connection : connections) {
			INetworkNode a = connection.x, b = connection.y;

			if (a instanceof IWorldNetworkNode && b instanceof IWorldNetworkNode) {
				IWorldNetworkNode aNode = (IWorldNetworkNode) a, bNode = (IWorldNetworkNode) b;
				IBlockAccess aWorld = aNode.getPosition().getWorld(), bWorld = bNode.getPosition().getWorld();

				if (aWorld == world) {
					if (bWorld == world) {
						renderConnection(aNode, bNode, color, thickness);
					} else {
						renderSingleNode(aNode, bNode);
					}
				} else if (bWorld == world) {
					renderSingleNode(aNode, bNode);
				}
			} else if (a instanceof IWorldNetworkNode) {
				IWorldNetworkNode aNode = (IWorldNetworkNode) a;
				if (aNode.getPosition().getWorld() == world) {
					renderSingleNode(aNode, b);
				}
			} else if (b instanceof IWorldNetworkNode) {
				IWorldNetworkNode bNode = (IWorldNetworkNode) b;
				if (bNode.getPosition().getWorld() == world) {
					renderSingleNode(bNode, a);
				}
			}
		}
	}

	public void renderSingleNode(IWorldNetworkNode aNode, INetworkNode bNode) {
		MovingObjectPosition position = Minecraft.getMinecraft().objectMouseOver;

		if (position.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
			IWorldPosition nodePosition = aNode.getPosition();
			if (nodePosition.getX() == position.blockX && nodePosition.getY() == position.blockY && nodePosition.getZ() == position.blockZ) {
				renderLabel(position.blockX + 0.5, position.blockY + 1.5, position.blockZ + 0.5, bNode.toString());
			}
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
		RenderManager renderManager = RenderManager.instance;
		FontRenderer fontrenderer = renderManager.getFontRenderer();
		float scale = 0.02666667F;
		GL11.glPushMatrix();

		GL11.glTranslated(x, y, z);
		// GL11.glNormal3f(0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-renderManager.playerViewY, 0, 1, 0);
		GL11.glRotatef(renderManager.playerViewX, 1, 0, 0);
		GL11.glScalef(-scale, -scale, scale);

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDepthMask(false);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		Tessellator tessellator = Tessellator.instance;

		int yOffset = 0;
		int xOffset = fontrenderer.getStringWidth(label) / 2;
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA_F(0, 0, 0, 0);
		tessellator.addVertex(-xOffset - 1, -1 + yOffset, 0);
		tessellator.addVertex(-xOffset - 1, 8 + yOffset, 0);
		tessellator.addVertex(xOffset + 1, 8 + yOffset, 0);
		tessellator.addVertex(xOffset + 1, -1 + yOffset, 0);
		tessellator.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		fontrenderer.drawString(label, -fontrenderer.getStringWidth(label) / 2, yOffset, 553648127);

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(true);
		fontrenderer.drawString(label, -fontrenderer.getStringWidth(label) / 2, yOffset, -1);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_BLEND);
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
