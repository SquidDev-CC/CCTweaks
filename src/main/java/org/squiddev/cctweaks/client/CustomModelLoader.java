package org.squiddev.cctweaks.client;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.core.registry.IClientModule;

/**
 * Loader for models that aren't associated with a block
 *
 * Mostly for
 */
public class CustomModelLoader implements IClientModule {
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onModelBakeEvent(ModelBakeEvent event) {
		loadModel(event, "wireless_bridge_turtle_left");
		loadModel(event, "wireless_bridge_turtle_right");
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitchEvent(TextureStitchEvent.Pre event) {
		// I didn't think I had to do this. Odd.
		event.getMap().registerSprite(new ResourceLocation(CCTweaks.ID, "blocks/wireless_bridge_small"));
	}

	@SideOnly(Side.CLIENT)
	private void loadModel(ModelBakeEvent event, String name) {
		IBakedModel model;
		try {
			model = ModelLoaderRegistry
				.getModel(new ResourceLocation(CCTweaks.ID, "block/" + name))
				.bake(TRSRTransformation.identity(), DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter());
		} catch (Exception e) {
			model = event.getModelManager().getMissingModel();
		}

		event.getModelRegistry().putObject(new ModelResourceLocation(new ResourceLocation(CCTweaks.ID, name), "inventory"), model);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPreInit() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientInit() {
	}
}
