package org.squiddev.cctweaks.integration.jei;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.util.ResourceLocation;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.core.utils.Helpers;

import javax.annotation.Nonnull;

public class UpgradeCategory implements IRecipeCategory<IRecipeWrapper> {
	private final String id;
	private final IDrawable background;

	public UpgradeCategory(String id, IGuiHelper helper) {
		this.id = id;
		ResourceLocation location = new ResourceLocation(CCTweaks.ID, "textures/gui/jei_upgrade.png");
		background = helper.createDrawable(location, 0, 0, 160, 50);
	}

	@Nonnull
	@Override
	public String getUid() {
		return CCTweaks.ID + ":" + id;
	}

	@Nonnull
	@Override
	public String getTitle() {
		return Helpers.translateToLocal("gui.jei.cctweaks." + id);
	}

	@Nonnull
	@Override
	public String getModName() {
		return CCTweaks.NAME;
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull IRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.init(0, true, 18, 16);
		guiItemStacks.init(1, true, 67, 16);
		guiItemStacks.init(2, false, 125, 16);

		guiItemStacks.set(ingredients);
	}
}
