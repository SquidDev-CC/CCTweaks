package org.squiddev.cctweaks.integration.jei;

import com.google.common.collect.Lists;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.shared.proxy.CCTurtleProxyCommon;
import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.squiddev.cctweaks.api.pocket.IPocketUpgrade;
import org.squiddev.cctweaks.core.pocket.PocketRegistry;
import org.squiddev.cctweaks.core.registry.Registry;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

import static org.squiddev.cctweaks.integration.jei.JeiDescription.registerDescription;
import static org.squiddev.cctweaks.integration.jei.JeiDescription.registerGenericDescription;

@JEIPlugin
public class JeiCCTweaks extends BlankModPlugin {
	private static final String POCKET_ID = "pocket";
	private static final String TURTLE_ID = "turtle";

	@Override
	public void register(@Nonnull IModRegistry registry) {
		IGuiHelper helper = registry.getJeiHelpers().getGuiHelper();
		registerGenericDescription(registry, Registry.itemComputerUpgrade);
		registerGenericDescription(registry, Registry.itemDebugger);
		registerDescription(registry, Registry.itemToolHost);
		registerGenericDescription(registry, Registry.itemDataCard);

		registerGenericDescription(registry, Registry.blockDebug);
		registerDescription(registry, Registry.blockNetworked);

		UpgradeCategory pocketCat = new UpgradeCategory("pocket", helper);
		UpgradeCategory turtleCat = new UpgradeCategory("turtle", helper);

		registry.addRecipeCategories(pocketCat, turtleCat);
		registry.addRecipeHandlers(
			new SimpleRecipeHandler<PocketModemUpgradeWrapper>(pocketCat, PocketModemUpgradeWrapper.class),
			new BasicRecipeHandler<IPocketUpgrade>(pocketCat, IPocketUpgrade.class) {
				@Nonnull
				@Override
				public IRecipeWrapper getRecipeWrapper(@Nonnull IPocketUpgrade recipe) {
					return new PocketUpgradeWrapper(recipe);
				}

				@Override
				public boolean isRecipeValid(@Nonnull IPocketUpgrade recipe) {
					return recipe.getCraftingItem() != null;
				}
			},
			new BasicRecipeHandler<ITurtleUpgrade>(turtleCat, ITurtleUpgrade.class) {
				@Nonnull
				@Override
				public IRecipeWrapper getRecipeWrapper(@Nonnull ITurtleUpgrade recipe) {
					return new TurtleUpgradeWrapper(recipe);
				}

				@Override
				public boolean isRecipeValid(@Nonnull ITurtleUpgrade recipe) {
					return recipe.getCraftingItem() != null;
				}
			}
		);

		registry.addRecipes(Collections.singletonList(new PocketModemUpgradeWrapper()));
		registry.addRecipes(Lists.newArrayList(PocketRegistry.instance.getUpgrades()));

		Map<String, ITurtleUpgrade> turtleUpgradeMap = ReflectionHelper.getPrivateValue(CCTurtleProxyCommon.class, (CCTurtleProxyCommon) ComputerCraft.turtleProxy, "m_turtleUpgrades");
		registry.addRecipes(Lists.newArrayList(turtleUpgradeMap.values()));
	}
}
