package squiddev.cctweaks.client;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import squiddev.cctweaks.core.proxy.IProxy;

public class ProxyClient implements IProxy {
	private final Minecraft mc;

	public ProxyClient() {
		mc = Minecraft.getMinecraft();
	}

	@Override
	public World getClientWorld(int dimId) {
		return mc.theWorld;
	}

	@Override
	public boolean isClient() {
		return FMLCommonHandler.instance().getEffectiveSide().isClient();
	}

	@Override
	public void registerRenderInfo() {
		// Nothing here yet...
	}
}
