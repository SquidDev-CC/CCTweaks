package org.squiddev.cctweaks.integration.jei;

import com.google.common.collect.Lists;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import dan200.computercraft.shared.proxy.CCTurtleProxyCommon;
import mezz.jei.api.*;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.squiddev.cctweaks.api.pocket.IPocketUpgrade;
import org.squiddev.cctweaks.core.pocket.PocketRegistry;
import org.squiddev.cctweaks.core.registry.Registry;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.squiddev.cctweaks.integration.jei.JeiDescription.registerDescription;
import static org.squiddev.cctweaks.integration.jei.JeiDescription.registerGenericDescription;

@JEIPlugin
public class JeiCCTweaks extends BlankModPlugin {
	public static final String POCKET_ID = "pocket";
	public static final String TURTLE_ID = "turtle";

	@Override
	public void register(@Nonnull IModRegistry registry) {
		IGuiHelper helper = registry.getJeiHelpers().getGuiHelper();
		registerGenericDescription(registry, Registry.itemComputerUpgrade);
		registerGenericDescription(registry, Registry.itemDebugger);
		registerDescription(registry, Registry.itemToolHost);
		registerGenericDescription(registry, Registry.itemDataCard);

		registerGenericDescription(registry, Registry.blockDebug);
		registerDescription(registry, Registry.blockNetworked);

		UpgradeCategory pocketCat = new UpgradeCategory(POCKET_ID, helper);
		UpgradeCategory turtleCat = new UpgradeCategory(TURTLE_ID, helper);

		registry.addRecipeCategories(pocketCat, turtleCat);
		registry.addRecipeHandlers(
			new BasicRecipeHandler<PocketUpgradeWrapper>(pocketCat, PocketUpgradeWrapper.class),
			new BasicRecipeHandler<TurtleUpgradeWrapper>(turtleCat, TurtleUpgradeWrapper.class)
		);

		// Register all pocket upgrades
		{
			Collection<IPocketUpgrade> pocketUpgrades = PocketRegistry.instance.getUpgrades();
			List<PocketUpgradeWrapper> pocketWrappers = Lists.newArrayListWithExpectedSize((pocketUpgrades.size() + 1) * PocketUpgradeWrapper.FAMILIES.length);
			for (ComputerFamily family : PocketUpgradeWrapper.FAMILIES) {
				// Hack to ensure the default modem is added too
				pocketWrappers.add(new PocketUpgradeWrapper(
					PocketComputerItemFactory.create(-1, null, family, false),
					PeripheralItemFactory.create(PeripheralType.WirelessModem, null, 1),
					PocketComputerItemFactory.create(-1, null, family, true)
				));

				for (IPocketUpgrade upgrade : pocketUpgrades) {
					pocketWrappers.add(new PocketUpgradeWrapper(upgrade, family));
				}
			}

			registry.addRecipes(pocketWrappers);
		}

		// Register all turtle upgrades
		{
			Map<String, ITurtleUpgrade> turtleUpgrades = ReflectionHelper.getPrivateValue(CCTurtleProxyCommon.class, (CCTurtleProxyCommon) ComputerCraft.turtleProxy, "m_turtleUpgrades");
			List<TurtleUpgradeWrapper> turtleWrappers = Lists.newArrayListWithExpectedSize(turtleUpgrades.size() * PocketUpgradeWrapper.FAMILIES.length);
			for (ComputerFamily family : TurtleUpgradeWrapper.FAMILIES) {
				for (ITurtleUpgrade upgrade : turtleUpgrades.values()) {
					turtleWrappers.add(new TurtleUpgradeWrapper(upgrade, family));
				}
			}

			registry.addRecipes(turtleWrappers);
		}

		// Ignore a load of NBT
		INbtIgnoreList nbtIgnore = registry.getJeiHelpers().getNbtIgnoreList();
		nbtIgnore.ignoreNbtTagNames(ComputerCraft.Items.pocketComputer, "computerID", "instanceID");

		final String[] turtleIgnore = new String[]{"computerID", "fuelLevel", "colourIndex", "overlay_mod", "overlay_path"};
		nbtIgnore.ignoreNbtTagNames(Item.getItemFromBlock(ComputerCraft.Blocks.turtle), turtleIgnore);
		nbtIgnore.ignoreNbtTagNames(Item.getItemFromBlock(ComputerCraft.Blocks.turtleAdvanced), turtleIgnore);
		nbtIgnore.ignoreNbtTagNames(Item.getItemFromBlock(ComputerCraft.Blocks.turtleExpanded), turtleIgnore);
	}
}
