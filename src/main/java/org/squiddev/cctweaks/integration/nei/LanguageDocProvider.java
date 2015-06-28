package org.squiddev.cctweaks.integration.nei;

import net.minecraft.item.ItemStack;
import org.squiddev.cctweaks.core.utils.Helpers;

import java.util.ArrayList;
import java.util.List;

public class LanguageDocProvider implements DescriptionHelpers.IDocProvider {
	@Override
	public List<String> getPages(ItemStack stack) {
		String name = stack.getUnlocalizedName() + ".information.", genericName = stack.getItem().getUnlocalizedName() + ".information.";

		int index = 0;
		List<String> lines = new ArrayList<String>();
		while (true) {
			String translated = Helpers.translateOrDefault(null, name + index, genericName + index);
			if (translated == null) break;

			lines.add(translated.replace("\\n", "\n\n"));
			index++;
		}

		return lines;
	}
}
