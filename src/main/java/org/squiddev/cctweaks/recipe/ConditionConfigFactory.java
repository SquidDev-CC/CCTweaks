package org.squiddev.cctweaks.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import org.squiddev.cctweaks.core.Config;

import java.util.function.BooleanSupplier;

public class ConditionConfigFactory implements IConditionFactory {
	@Override
	public BooleanSupplier parse(JsonContext context, JsonObject json) {
		String key = JsonUtils.getString(json, "prop");
		int index = key.lastIndexOf('.');

		if (index < 0) throw new JsonSyntaxException("Expected <category>.<config>, got '" + key + "'");

		String categoryName = key.substring(0, index);
		String propertyName = key.substring(index + 1);

		if (!Config.configuration.hasCategory(categoryName)) {
			throw new JsonSyntaxException("No such category '" + categoryName + "'");
		}

		ConfigCategory category = Config.configuration.getCategory(categoryName);

		final Property property = category.get(propertyName);
		if (property == null) {
			throw new JsonSyntaxException("No such property '" + propertyName + "' in category '" + categoryName + "'");
		}

		if (property.getType() != Property.Type.BOOLEAN) {
			throw new JsonSyntaxException("Type is " + property.getType() + ", not boolean");
		}

		return property::getBoolean;
	}
}
