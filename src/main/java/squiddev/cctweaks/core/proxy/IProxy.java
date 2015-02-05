package squiddev.cctweaks.core.proxy;

import net.minecraft.world.World;

public interface IProxy {
	public World getClientWorld(int dimId);

	public boolean isClient();

	public void registerRenderInfo();
}
