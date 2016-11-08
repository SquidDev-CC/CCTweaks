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
		loadModel(event, "wireless_bridge_turtle_left");
		loadModel(event, "wireless_bridge_turtle_right");
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitchEvent(TextureStitchEvent.Pre event) {
		// I didn't think I had to do this. Odd.
		event.getMap().registerSprite(new ResourceLocation(CCTweaks.ID, "blocks/wirelessBridgeSmall"));
	}

	@SideOnly(Side.CLIENT)
	private void loadModel(ModelBakeEvent event, String name) {
		// IBakedModel model = event.getModelManager().getModel(new ModelResourceLocation(new ResourceLocation(CCTweaks.ID, name), "inventory"));
		// TODO: 1.10.2 Fix me
//		IBakedModel model = event.getModelRegistry().getObject(new ModelResourceLocation(new ResourceLocation(CCTweaks.ID, name), "inventory"));
//		event.getModelRegistry().putObject(new ModelResourceLocation(CCTweaks.ID + ":" + name, "inventory"), model);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientInit() {
		MinecraftForge.EVENT_BUS.register(this);
	}
}
