package org.squiddev.cctweaks.core.utils;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

/**
 * Helper methods for various things
 */
public class Helpers {
	/**
	 * Translate any variant of a string
	 *
	 * @param strings The strings to try to translate
	 * @return The first translateable string
	 */
	public static String translateAny(String... strings) {
		return translateOrDefault(strings[strings.length - 1], strings);
	}

	/**
	 * Translate any variant of a string
	 *
	 * @param def     The fallback string
	 * @param strings The strings to try to translate
	 * @return The first translateable string or the default
	 */
	public static String translateOrDefault(String def, String... strings) {
		for (String string : strings) {
			String translated = StatCollector.translateToLocal(string);
			if (!string.equals(translated)) return translated;
		}

		return def;
	}

	public static void twoWayCrafting(ItemStack a, ItemStack b) {
		GameRegistry.addShapelessRecipe(a, b);
		GameRegistry.addShapelessRecipe(b, a);
	}

	/**
	 * Add a series of crafting recipes with positions swapped.
	 *
	 * @param output The output stack
	 * @param a      The first item to swap
	 * @param b      The second item to swap
	 * @param args   Args as passed to {@link GameRegistry#addRecipe(net.minecraft.item.crafting.IRecipe)}
	 */
	public static void alternateCrafting(ItemStack output, char a, char b, Object... args) {
		GameRegistry.addRecipe(output, args);

		if (args[0] instanceof String[]) {
			String[] inputs = (String[]) args[0];

			for (int i = 0; i < inputs.length; i++) {
				inputs[i] = swapCharacters(inputs[i], a, b);
			}
		} else {
			int i = 0;
			while (args[i] instanceof String) {
				args[i] = swapCharacters((String) args[i], a, b);
				++i;
			}
		}

		GameRegistry.addRecipe(output, args);
	}

	/**
	 * Swap two characters in a string
	 *
	 * @param word The string to swap
	 * @param a    First character
	 * @param b    Second character
	 * @return Swapped string
	 */
	public static String swapCharacters(String word, char a, char b) {
		StringBuilder builder = new StringBuilder(word.length());

		for (int i = 0; i < word.length(); i++) {
			char c = word.charAt(i);
			if (c == a) {
				c = b;
			} else if (c == b) {
				c = a;
			}
			builder.append(c);
		}
		return builder.toString();
	}
}
