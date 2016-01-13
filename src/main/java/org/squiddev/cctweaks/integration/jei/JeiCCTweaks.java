package org.squiddev.cctweaks.integration.jei;

import mezz.jei.api.*;
import org.squiddev.cctweaks.core.registry.Registry;

import static org.squiddev.cctweaks.integration.jei.JeiDescription.registerDescription;
import static org.squiddev.cctweaks.integration.jei.JeiDescription.registerGenericDescription;

@JEIPlugin
public class JeiCCTweaks implements IModPlugin {
	@Override
	public void onJeiHelpersAvailable(IJeiHelpers jeiHelpers) {
	}

	@Override
	public void onItemRegistryAvailable(IItemRegistry itemRegistry) {
	}

	@Override
	public void register(IModRegistry registry) {
		registerGenericDescription(registry, Registry.itemComputerUpgrade);
		registerGenericDescription(registry, Registry.itemDebugger);
		registerGenericDescription(registry, Registry.itemToolHost);
		registerGenericDescription(registry, Registry.itemDataCard);

		registerGenericDescription(registry, Registry.blockDebug);
		registerDescription(registry, Registry.blockNetworked);
	}

	@Override
	public void onRecipeRegistryAvailable(IRecipeRegistry recipeRegistry) {
	}
}
