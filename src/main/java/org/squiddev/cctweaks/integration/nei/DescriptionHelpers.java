package org.squiddev.cctweaks.integration.nei;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DescriptionHelpers {
	public interface IDocProvider {
		List<String> getPages(ItemStack stack);
	}

	private static final List<IDocProvider> providers = new ArrayList<IDocProvider>();

	static {
		add(new LanguageDocProvider());
	}

	public static List<String> getTranslate(ItemStack stack) {
		for (IDocProvider provider : providers) {
			List<String> lines = provider.getPages(stack);
			if (lines != null && lines.size() > 0) return lines;
		}

		return null;
	}

	public static void add(IDocProvider provider) {
		providers.add(provider);
	}
}
