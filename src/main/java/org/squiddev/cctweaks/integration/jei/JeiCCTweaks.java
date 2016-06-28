package org.squiddev.cctweaks.integration.jei;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import org.squiddev.cctweaks.core.registry.Registry;

import javax.annotation.Nonnull;

import static org.squiddev.cctweaks.integration.jei.JeiDescription.registerDescription;
import static org.squiddev.cctweaks.integration.jei.JeiDescription.registerGenericDescription;

@JEIPlugin
public class JeiCCTweaks implements IModPlugin {
	@Override
	public void register(@Nonnull IModRegistry registry) {
		registerGenericDescription(registry, Registry.itemComputerUpgrade);
		registerGenericDescription(registry, Registry.itemDebugger);
		registerDescription(registry, Registry.itemToolHost);
		registerGenericDescription(registry, Registry.itemDataCard);

		registerGenericDescription(registry, Registry.blockDebug);
		registerDescription(registry, Registry.blockNetworked);
	}

	@Override
	public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {
	}
}
