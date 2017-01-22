package org.squiddev.cctweaks.integration.jei;

import com.google.common.collect.Lists;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import dan200.computercraft.shared.proxy.CCTurtleProxyCommon;
import dan200.computercraft.shared.turtle.items.ITurtleItem;
import dan200.computercraft.shared.turtle.items.TurtleItemFactory;
import mezz.jei.api.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.squiddev.cctweaks.api.pocket.IPocketUpgrade;
import org.squiddev.cctweaks.core.pocket.PocketRegistry;
import org.squiddev.cctweaks.core.registry.Registry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static mezz.jei.api.ISubtypeRegistry.ISubtypeInterpreter;
import static org.squiddev.cctweaks.integration.jei.JeiDescription.registerDescription;
import static org.squiddev.cctweaks.integration.jei.JeiDescription.registerGenericDescription;

@JEIPlugin
public class JeiCCTweaks extends BlankModPlugin {
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
			new BasicRecipeHandler<PocketUpgradeWrapper>(pocketCat, PocketUpgradeWrapper.class),
			new BasicRecipeHandler<TurtleUpgradeWrapper>(turtleCat, TurtleUpgradeWrapper.class)
		);

		// Register all pocket upgrades
		{
			Collection<IPocketUpgrade> pocketUpgrades = PocketRegistry.instance.getUpgrades();
			List<PocketUpgradeWrapper> pocketWrappers = Lists.newArrayListWithExpectedSize((pocketUpgrades.size() + 1) * PocketUpgradeWrapper.FAMILIES.length);
			for (ComputerFamily family : PocketUpgradeWrapper.FAMILIES) {
				registry.addRecipeCategoryCraftingItem(PocketComputerItemFactory.create(-1, null, family, false), pocketCat.getUid());

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
				registry.addRecipeCategoryCraftingItem(TurtleItemFactory.create(-1, null, null, family, null, null, 0, null), turtleCat.getUid());

				for (ITurtleUpgrade upgrade : turtleUpgrades.values()) {
					turtleWrappers.add(new TurtleUpgradeWrapper(upgrade, family));
				}
			}

			registry.addRecipes(turtleWrappers);
		}
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
		subtypeRegistry.registerSubtypeInterpreter(ComputerCraft.Items.pocketComputer, pocketSubtype);
		subtypeRegistry.registerSubtypeInterpreter(Item.getItemFromBlock(ComputerCraft.Blocks.turtle), turtleSubtype);
		subtypeRegistry.registerSubtypeInterpreter(Item.getItemFromBlock(ComputerCraft.Blocks.turtleAdvanced), turtleSubtype);
		subtypeRegistry.registerSubtypeInterpreter(Item.getItemFromBlock(ComputerCraft.Blocks.turtleExpanded), turtleSubtype);
	}

	private static final ISubtypeInterpreter pocketSubtype = new ISubtypeInterpreter() {
		@Nullable
		@Override
		public String getSubtypeInfo(@Nonnull ItemStack stack) {
			ComputerFamily family = ComputerCraft.Items.pocketComputer.getFamily(stack);

			String adjective = PocketRegistry.instance.getUpgradeAdjective(stack, null);
			if (adjective == null) {
				return family.toString();
			} else {
				return family + ":" + adjective;
			}
		}
	};

	private static final ISubtypeInterpreter turtleSubtype = new ISubtypeInterpreter() {
		@Nullable
		@Override
		public String getSubtypeInfo(@Nonnull ItemStack stack) {
			Item item = stack.getItem();
			if (!(item instanceof ITurtleItem)) return "";

			ITurtleItem turtle = (ITurtleItem) item;

			String name = turtle.getFamily(stack).toString();

			ITurtleUpgrade left = turtle.getUpgrade(stack, TurtleSide.Left);
			ITurtleUpgrade right = turtle.getUpgrade(stack, TurtleSide.Right);

			// We do it like this so we can appear to be "neutral" with respect to side.
			if (left != null) name += ":" + left.getUnlocalisedAdjective();
			if (right != null) name += ":" + right.getUnlocalisedAdjective();

			return name;
		}
	};
}
