package org.squiddev.cctweaks.integration.jei;

import mezz.jei.api.recipe.IRecipeWrapper;

public interface IValidRecipeWrapper extends IRecipeWrapper {
	boolean isValid();
}
