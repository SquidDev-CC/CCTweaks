package org.squiddev.cctweaks.integration.jei;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;

import javax.annotation.Nonnull;

public abstract class BasicRecipeHandler<T> implements IRecipeHandler<T> {
	private final String id;
	private final Class<T> klass;

	public BasicRecipeHandler(IRecipeCategory category, Class<T> klass) {
		this.id = category.getUid();
		this.klass = klass;
	}

	@Nonnull
	@Override
	public Class<T> getRecipeClass() {
		return klass;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid() {
		return id;
	}
}
