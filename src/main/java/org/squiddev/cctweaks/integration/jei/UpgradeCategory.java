package org.squiddev.cctweaks.integration.jei;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.BlankRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.squiddev.cctweaks.CCTweaks;

import javax.annotation.Nonnull;
import java.util.List;

public class UpgradeCategory extends BlankRecipeCategory {
	private final String id;
	private final IDrawable background;

	public UpgradeCategory(String id, IGuiHelper helper) {
		this.id = id;
		ResourceLocation location = new ResourceLocation(CCTweaks.RESOURCE_DOMAIN, "textures/gui/jei_upgrade.png");
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
		return StatCollector.translateToLocal("gui.jei.cctweaks." + id);
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull IRecipeWrapper recipeWrapper) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.init(0, true, 18, 16);
		guiItemStacks.init(1, true, 67, 16);
		guiItemStacks.init(2, false, 125, 16);

		List<?> inputs = recipeWrapper.getInputs();
		guiItemStacks.setFromRecipe(0, inputs.get(0));
		guiItemStacks.setFromRecipe(1, inputs.get(1));

		guiItemStacks.setFromRecipe(2, recipeWrapper.getOutputs());
	}
}
