package org.squiddev.cctweaks.integration.jei;

import com.google.common.collect.Lists;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import dan200.computercraft.shared.proxy.CCTurtleProxyCommon;
import dan200.computercraft.shared.turtle.items.ITurtleItem;
import dan200.computercraft.shared.turtle.items.TurtleItemFactory;
import mezz.jei.api.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.squiddev.cctweaks.core.registry.Registry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static mezz.jei.api.ISubtypeRegistry.ISubtypeInterpreter;
import static org.squiddev.cctweaks.integration.jei.JeiDescription.registerDescription;

@JEIPlugin
public class JeiCCTweaks implements IModPlugin {
	@Override
	public void register(@Nonnull IModRegistry registry) {
		IGuiHelper helper = registry.getJeiHelpers().getGuiHelper();

		registerDescription(registry, Registry.itemComputerUpgrade);
		registerDescription(registry, Registry.itemDebugger);
		registerDescription(registry, Registry.itemToolHost);
		registerDescription(registry, Registry.itemDataCard);

		registerDescription(registry, Registry.blockDebug);
		registerDescription(registry, Registry.blockNetworked);

		UpgradeCategory pocketCat = new UpgradeCategory("pocket", helper);
		UpgradeCategory turtleCat = new UpgradeCategory("turtle", helper);

		registry.addRecipeCategories(pocketCat, turtleCat);
		registry.addRecipeHandlers(
			new BasicRecipeHandler<>(pocketCat, PocketUpgradeWrapper.class),
			new BasicRecipeHandler<>(turtleCat, TurtleUpgradeWrapper.class)
		);

		// Register all pocket upgrades
		{
			Map<String, IPocketUpgrade> pocketUpgrades = ReflectionHelper.getPrivateValue(ComputerCraft.class, null, "pocketUpgrades");
			List<PocketUpgradeWrapper> pocketWrappers = Lists.newArrayListWithExpectedSize((pocketUpgrades.size() + 1) * PocketUpgradeWrapper.FAMILIES.length);
			for (ComputerFamily family : PocketUpgradeWrapper.FAMILIES) {
				registry.addRecipeCategoryCraftingItem(PocketComputerItemFactory.create(-1, null, -1, family, null), pocketCat.getUid());

				for (IPocketUpgrade upgrade : pocketUpgrades.values()) {
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
				registry.addRecipeCatalyst(TurtleItemFactory.create(-1, null, -1, family, null, null, 0, null), turtleCat.getUid());

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

			IPocketUpgrade upgrade = ComputerCraft.Items.pocketComputer.getUpgrade(stack);
			if (upgrade == null) {
				return family.toString();
			} else {
				return family + "-" + upgrade.getUpgradeID();
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
			if (left != null) name += "-" + left.getUpgradeID();
			if (right != null) name += "-" + right.getUpgradeID();

			return name;
		}
	};
}
