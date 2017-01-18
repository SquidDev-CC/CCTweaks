package org.squiddev.cctweaks.integration.jei;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;

import javax.annotation.Nonnull;


public class SimpleRecipeHandler<T extends IRecipeWrapper> extends BasicRecipeHandler<T> {
	public SimpleRecipeHandler(IRecipeCategory category, Class<T> klass) {
		super(category, klass);
	}

	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull T recipe) {
		return recipe;
	}

	@Override
	public boolean isRecipeValid(@Nonnull T recipe) {
		return true;
	}
}
