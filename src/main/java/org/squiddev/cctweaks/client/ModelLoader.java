package org.squiddev.cctweaks.client;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.core.registry.IClientModule;
import org.squiddev.cctweaks.core.registry.Module;

/**
 * Loader for models that aren't associated with a block
 *
 * Mostly for
 */
public class ModelLoader extends Module implements IClientModule {
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onModelBakeEvent(ModelBakeEvent event) {
//		loadModel(event, "wireless_bridge_turtle_left");
//		loadModel(event, "wireless_bridge_turtle_right");
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitchEvent(TextureStitchEvent.Pre event) {
		// I didn't think I had to do this. Odd.
		event.getMap().registerSprite(new ResourceLocation(CCTweaks.RESOURCE_DOMAIN, "blocks/wirelessBridgeSmall"));
	}

//	@SideOnly(Side.CLIENT)
//	private void loadModel(ModelBakeEvent event, String name) {
//		try {
//			IModel e = event.getModelLoader().getModel(new ResourceLocation(CCTweaks.RESOURCE_DOMAIN, "block/" + name));
//			IBakedModel bakedModel = e.bake(e.getDefaultState(), DefaultVertexFormats.ITEM, new Function<ResourceLocation, TextureAtlasSprite>() {
//				@Override
//				public TextureAtlasSprite apply(ResourceLocation location) {
//					return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
//				}
//			});
//			event.getModelRegistry().putObject(new ModelResourceLocation(CCTweaks.RESOURCE_DOMAIN + ":" + name, "inventory"), bakedModel);
//		} catch (IOException e) {
//			DebugLogger.error("Could not load model: " + name, e);
//		}
//	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientInit() {
		MinecraftForge.EVENT_BUS.register(this);
	}
}
