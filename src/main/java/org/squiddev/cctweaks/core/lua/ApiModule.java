package org.squiddev.cctweaks.core.lua;

import dan200.computercraft.api.peripheral.IComputerAccess;
import org.squiddev.cctweaks.api.CCTweaksAPI;
import org.squiddev.cctweaks.api.lua.ILuaAPI;
import org.squiddev.cctweaks.api.lua.ILuaAPIFactory;
import org.squiddev.cctweaks.api.lua.ILuaEnvironment;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.lua.socket.SocketAPI;
import org.squiddev.cctweaks.core.registry.Module;

public class ApiModule extends Module {
	@Override
	public void init() {
		ILuaEnvironment environment = CCTweaksAPI.instance().luaEnvironment();

		environment.registerAPI(new ILuaAPIFactory() {
			@Override
			public ILuaAPI create(IComputerAccess computer) {
				return Config.APIs.Socket.enabled ? new SocketAPI() : null;
			}

			@Override
			public String[] getNames() {
				return new String[]{"socket"};
			}
		});
	}
}
