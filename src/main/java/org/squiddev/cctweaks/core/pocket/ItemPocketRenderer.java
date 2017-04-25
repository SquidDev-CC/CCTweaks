package org.squiddev.cctweaks.core.pocket;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.client.gui.FixedWidthFontRenderer;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.terminal.TextBuffer;
import dan200.computercraft.shared.computer.core.ClientComputer;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.registry.IClientModule;
import org.squiddev.cctweaks.core.registry.Module;

import java.util.List;

import static dan200.computercraft.client.gui.FixedWidthFontRenderer.FONT_HEIGHT;
import static dan200.computercraft.client.gui.FixedWidthFontRenderer.FONT_WIDTH;

/**
 * Emulates map rendering for pocket computers
 *
 * @see net.minecraft.client.renderer.ItemRenderer
 */
public class ItemPocketRenderer extends Module implements IClientModule {
	@Override
	public void clientPreInit() {
	}

	@Override
	public void clientInit() {
		MinecraftForge.EVENT_BUS.register(this);
	}


	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderItem(RenderSpecificHandEvent event) {
		ItemStack stack = event.getItemStack();
		if (stack == null || !(stack.getItem() instanceof ItemPocketComputer)) return;
		if (!Config.Misc.pocketMapRender) return;

		event.setCanceled(true);

		EntityPlayer player = Minecraft.getMinecraft().thePlayer;

		GlStateManager.pushMatrix();
		if (event.getHand() == EnumHand.MAIN_HAND && player.getHeldItemOffhand() == null) {
			renderMapFirstPerson(
				player,
				event.getInterpolatedPitch(),
				event.getEquipProgress(),
				event.getSwingProgress(),
				stack
			);
		} else {
			renderMapFirstPersonSide(
				event.getHand() == EnumHand.MAIN_HAND ? player.getPrimaryHand() : player.getPrimaryHand().opposite(),
				event.getEquipProgress(),
				event.getSwingProgress(),
				stack
			);
		}
		GlStateManager.popMatrix();
	}

	private void renderMapFirstPerson(ItemStack stack) {
		GlStateManager.disableLighting();

		GlStateManager.rotate(180f, 0f, 1f, 0f);
		GlStateManager.rotate(180f, 0f, 0f, 1f);
		GlStateManager.scale(0.5, 0.5, 0.5);

		ItemPocketComputer pocketComputer = ComputerCraft.Items.pocketComputer;
		ClientComputer computer = pocketComputer.createClientComputer(stack);

		{
			GlStateManager.pushMatrix();

			GlStateManager.scale(1.0f, -1.0f, 1.0f);

			Minecraft minecraft = Minecraft.getMinecraft();
			TextureManager textureManager = minecraft.getTextureManager();
			RenderItem renderItem = minecraft.getRenderItem();

			// Copy of RenderItem#renderItemModelIntoGUI but without the translation or scaling
			textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);

			GlStateManager.enableRescaleNormal();
			GlStateManager.enableAlpha();
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

			IBakedModel bakedmodel = renderItem.getItemModelWithOverrides(stack, null, null);
			bakedmodel = ForgeHooksClient.handleCameraTransforms(bakedmodel, ItemCameraTransforms.TransformType.GUI, false);
			renderItem.renderItem(stack, bakedmodel);

			GlStateManager.disableAlpha();
			GlStateManager.disableRescaleNormal();

			GlStateManager.popMatrix();
		}

		if (computer != null) {
			Terminal terminal = computer.getTerminal();
			if (terminal != null) {
				synchronized (terminal) {
					GlStateManager.pushMatrix();
					GlStateManager.disableDepth();

					// Reset the position to be at the top left corner of the pocket computer
					// Note we translate towards the screen slightly too.
					GlStateManager.translate(-8 / 16.0, -8 / 16.0, 0.5 / 16.0);
					// Translate to the top left of the screen.
					GlStateManager.translate(4 / 16.0, 3 / 16.0, 0);

					// Work out the scaling required to resize the terminal in order to fit on the computer
					final int margin = 2;
					int tw = terminal.getWidth();
					int th = terminal.getHeight();
					int width = tw * FONT_WIDTH + margin * 2;
					int height = th * FONT_HEIGHT + margin * 2;
					int max = Math.max(height, width);

					// The grid is 8 * 8 wide, so we start with a base of 1/2 (8 / 16).
					double scale = 1.0 / 2.0 / max;
					GlStateManager.scale(scale, scale, scale);

					// The margin/start positions are determined in order for the terminal to be centred.
					int startX = (max - width) / 2;
					int startY = (max - height) / 2;
					int marginH = startX + margin;
					int marginV = startY + margin;

					FixedWidthFontRenderer fontRenderer = (FixedWidthFontRenderer) ComputerCraft.getFixedWidthFontRenderer();
					boolean greyscale = !computer.isColour();

					// Render the margin above/below
					if (marginV > 0) {
						TextBuffer emptyLine = new TextBuffer(' ', tw);
						fontRenderer.drawString(emptyLine, marginH, startY, terminal.getTextColourLine(0), terminal.getBackgroundColourLine(0), marginH, marginH, greyscale);
						fontRenderer.drawString(emptyLine, marginH, startY + 2 * marginV + (th - 1) * FixedWidthFontRenderer.FONT_HEIGHT, terminal.getTextColourLine(th - 1), terminal.getBackgroundColourLine(th - 1), marginH, marginH, greyscale);
					}

					// Render the actual text
					int y = marginV;
					for (int line = 0; line < th; ++line) {
						TextBuffer text = terminal.getLine(line);
						TextBuffer colour = terminal.getTextColourLine(line);
						TextBuffer backgroundColour = terminal.getBackgroundColourLine(line);
						fontRenderer.drawString(text, marginH, y, colour, backgroundColour, marginH, marginH, greyscale);

						y += FONT_HEIGHT;
					}

					GlStateManager.enableDepth();
					GlStateManager.popMatrix();
				}
			}
		}

		GlStateManager.enableLighting();
	}

	//region Map functions
	private void renderMapFirstPersonSide(EnumHandSide side, float equipProgress, float swingProgress, ItemStack stack) {
		float offset = side == EnumHandSide.RIGHT ? 1f : -1f;
		GlStateManager.translate(offset * 0.125f, -0.125f, 0f);

		if (!Minecraft.getMinecraft().thePlayer.isInvisible()) {
			GlStateManager.pushMatrix();
			GlStateManager.rotate(offset * 10f, 0f, 0f, 1f);
			renderArmFirstPerson(equipProgress, swingProgress, side);
			GlStateManager.popMatrix();
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(offset * 0.51f, -0.08f + equipProgress * -1.2f, -0.75f);
		float f1 = MathHelper.sqrt_float(swingProgress);
		float f2 = MathHelper.sin(f1 * (float) Math.PI);
		float f3 = -0.5f * f2;
		float f4 = 0.4f * MathHelper.sin(f1 * ((float) Math.PI * 2f));
		float f5 = -0.3f * MathHelper.sin(swingProgress * (float) Math.PI);
		GlStateManager.translate(offset * f3, f4 - 0.3f * f2, f5);
		GlStateManager.rotate(f2 * -45f, 1f, 0f, 0f);
		GlStateManager.rotate(offset * f2 * -30f, 0f, 1f, 0f);
		renderMapFirstPerson(stack);

		GlStateManager.popMatrix();
	}

	private void renderMapFirstPerson(EntityPlayer player, float pitch, float equipProgress, float swingProgress, ItemStack stack) {
		float f = MathHelper.sqrt_float(swingProgress);
		float f1 = -0.2f * MathHelper.sin(swingProgress * (float) Math.PI);
		float f2 = -0.4f * MathHelper.sin(f * (float) Math.PI);
		GlStateManager.translate(0f, -f1 / 2f, f2);
		float f3 = getMapAngleFromPitch(pitch);
		GlStateManager.translate(0f, 0.04f + equipProgress * -1.2f + f3 * -0.5f, -0.72f);
		GlStateManager.rotate(f3 * -85f, 1f, 0f, 0f);
		renderArms(player);
		float f4 = MathHelper.sin(f * (float) Math.PI);
		GlStateManager.rotate(f4 * 20f, 1f, 0f, 0f);
		GlStateManager.scale(2f, 2f, 2f);
		renderMapFirstPerson(stack);
	}

	private float getMapAngleFromPitch(float pitch) {
		float f = 1f - pitch / 45f + 0.1f;
		f = MathHelper.clamp_float(f, 0f, 1f);
		f = -MathHelper.cos(f * (float) Math.PI) * 0.5f + 0.5f;
		return f;
	}
	//endregion

	//region Arm Rendering
	private void renderArms(Entity player) {
		if (!player.isInvisible()) {
			GlStateManager.disableCull();
			GlStateManager.pushMatrix();
			GlStateManager.rotate(90f, 0f, 1f, 0f);
			renderArm(EnumHandSide.RIGHT);
			renderArm(EnumHandSide.LEFT);
			GlStateManager.popMatrix();
			GlStateManager.enableCull();
		}
	}

	private void renderArmFirstPerson(float equipProgress, float swingProgress, EnumHandSide side) {
		Minecraft minecraft = Minecraft.getMinecraft();
		RenderManager manager = minecraft.getRenderManager();

		float offset = side == EnumHandSide.LEFT ? -1f : 1f;
		float swingSqrt = MathHelper.sqrt_float(swingProgress);
		float xTrans = -0.3f * MathHelper.sin(swingSqrt * (float) Math.PI);
		float yTrans = 0.4f * MathHelper.sin(swingSqrt * ((float) Math.PI * 2f));
		float zTrans = -0.4f * MathHelper.sin(swingProgress * (float) Math.PI);
		GlStateManager.translate(offset * (xTrans + 0.64000005f), yTrans + -0.6f + equipProgress * -0.6f, zTrans + -0.71999997f);
		GlStateManager.rotate(offset * 45f, 0f, 1f, 0f);

		float zRot = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
		float yRot = MathHelper.sin(swingSqrt * (float) Math.PI);
		GlStateManager.rotate(offset * yRot * 70f, 0f, 1f, 0f);
		GlStateManager.rotate(offset * zRot * -20f, 0f, 0f, 1f);

		AbstractClientPlayer clientPlayer = minecraft.thePlayer;
		minecraft.getTextureManager().bindTexture(clientPlayer.getLocationSkin());

		GlStateManager.translate(offset * -1f, 3.6f, 3.5f);
		GlStateManager.rotate(offset * 120f, 0f, 0f, 1f);
		GlStateManager.rotate(200f, 1f, 0f, 0f);
		GlStateManager.rotate(offset * -135f, 0f, 1f, 0f);
		GlStateManager.translate(offset * 5.6f, 0f, 0f);

		RenderPlayer renderPlayer = (RenderPlayer) manager.getEntityRenderObject(clientPlayer);
		GlStateManager.disableCull();

		if (side == EnumHandSide.LEFT) {
			renderPlayer.renderLeftArm(clientPlayer);
		} else {
			renderPlayer.renderRightArm(clientPlayer);
		}

		GlStateManager.enableCull();
	}

	private void renderArm(EnumHandSide side) {
		Minecraft minecraft = Minecraft.getMinecraft();
		RenderManager manager = minecraft.getRenderManager();

		minecraft.getTextureManager().bindTexture(minecraft.thePlayer.getLocationSkin());
		Render<AbstractClientPlayer> render = manager.<AbstractClientPlayer>getEntityRenderObject(minecraft.thePlayer);
		RenderPlayer renderPlayer = (RenderPlayer) render;

		GlStateManager.pushMatrix();
		float offset = side == EnumHandSide.RIGHT ? 1f : -1f;
		GlStateManager.rotate(92f, 0f, 1f, 0f);
		GlStateManager.rotate(45f, 1f, 0f, 0f);
		GlStateManager.rotate(offset * -41f, 0f, 0f, 1f);
		GlStateManager.translate(offset * 0.3f, -1.1f, 0.45f);

		if (side == EnumHandSide.RIGHT) {
			renderPlayer.renderRightArm(minecraft.thePlayer);
		} else {
			renderPlayer.renderLeftArm(minecraft.thePlayer);
		}

		GlStateManager.popMatrix();
	}
	//endregion

	//region Render helpers
	private void renderModel(IBakedModel model) {
		Minecraft mc = Minecraft.getMinecraft();
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer renderer = tessellator.getBuffer();
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
		for (EnumFacing facing : EnumFacing.VALUES) {
			renderQuads(renderer, model.getQuads(null, facing, 0));
		}

		renderQuads(renderer, model.getQuads(null, null, 0));
		tessellator.draw();
	}

	private void renderQuads(VertexBuffer renderer, List<BakedQuad> quads) {
		for (BakedQuad quad : quads) {
			LightUtil.renderQuadColor(renderer, quad, -1);
		}
	}
	//endregion
}
