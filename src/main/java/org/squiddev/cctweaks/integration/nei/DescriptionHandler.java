package org.squiddev.cctweaks.integration.nei;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IRecipeOverlayRenderer;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.ICraftingHandler;
import codechicken.nei.recipe.IUsageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;

/**
 * Renders helpful description messages
 */
public class DescriptionHandler implements ICraftingHandler, IUsageHandler {
	public final int WIDTH = 166;

	protected List<String> pages;
	protected ItemStack stack;

	public DescriptionHandler() {
	}

	public DescriptionHandler(ItemStack stack, List<String> pages) {
		this.stack = stack;
		this.pages = pages;
	}

	public DescriptionHandler getHandler(Object... ingredients) {
		for (Object ingredient : ingredients) {
			if (ingredient instanceof ItemStack) {
				ItemStack item = (ItemStack) ingredient;
				List<String> pages = DescriptionHelpers.getTranslate(item);
				if (pages != null) return new DescriptionHandler(item, pages);
			}
		}

		return this;
	}

	@Override
	public ICraftingHandler getRecipeHandler(String input, Object... ingredients) {
		return input.equals("item") ? getHandler(ingredients) : this;
	}

	@Override
	public IUsageHandler getUsageHandler(String output, Object... ingredients) {
		return output.equals("item") ? getHandler(ingredients) : this;
	}

	@Override
	public String getRecipeName() {
		return stack == null ? "Documentation" : stack.getDisplayName().trim();
	}

	@Override
	public int numRecipes() {
		return pages == null ? 0 : pages.size();
	}

	@Override
	public void drawBackground(int i) {
	}

	@Override
	public void drawForeground(int recipe) {
		FontRenderer renderer = Minecraft.getMinecraft().getRenderManager().getFontRenderer();
		List text = renderer.listFormattedStringToWidth(this.pages.get(recipe), WIDTH - 8);

		for (int i = 0; i < text.size(); i++) {
			String t = (String) text.get(i);
			renderer.drawString(t, WIDTH / 2 - renderer.getStringWidth(t) / 2, 18 + i * 8, -12566464, false);
		}
	}

	@Override
	public List<PositionedStack> getIngredientStacks(int i) {
		return Collections.emptyList();
	}

	@Override
	public List<PositionedStack> getOtherStacks(int i) {
		return Collections.emptyList();
	}

	@Override
	public PositionedStack getResultStack(int i) {
		return new PositionedStack(stack, WIDTH / 2 - 9, 0, false);
	}

	@Override
	public void onUpdate() {
	}

	@Override
	public boolean hasOverlay(GuiContainer guiContainer, Container container, int i) {
		return false;
	}

	@Override
	public IRecipeOverlayRenderer getOverlayRenderer(GuiContainer guiContainer, int i) {
		return null;
	}

	@Override
	public IOverlayHandler getOverlayHandler(GuiContainer guiContainer, int i) {
		return null;
	}

	@Override
	public int recipiesPerPage() {
		return 1;
	}

	@Override
	public List<String> handleTooltip(GuiRecipe guiRecipe, List<String> current, int i) {
		return current;
	}

	@Override
	public List<String> handleItemTooltip(GuiRecipe guiRecipe, ItemStack itemStack, List<String> current, int i) {
		return current;
	}

	@Override
	public boolean keyTyped(GuiRecipe guiRecipe, char c, int i, int i1) {
		return false;
	}

	@Override
	public boolean mouseClicked(GuiRecipe guiRecipe, int i, int i1) {
		return false;
	}
}
