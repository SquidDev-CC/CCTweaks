package squiddev.cctweaks.proxy;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import squiddev.cctweaks.interfaces.IProxy;

public class ProxyServer implements IProxy{

	@Override
    public World getClientWorld(int dimId) {
        return MinecraftServer.getServer().worldServerForDimension(dimId);
    }

    @Override
    public boolean isClient() {
        return false;
    }

    @Override
    public void registerRenderInfo() { }
}