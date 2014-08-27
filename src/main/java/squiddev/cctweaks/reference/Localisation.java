package squiddev.cctweaks.reference;

import net.minecraft.util.StatCollector;

/**
 * Stores language strings
 */
public class Localisation {
	public static final class Tooltips {
		public static final LocalisationItem ShiftInfo = new LocalisationItem("cctweaks.tooltip.generic.information");
	}

	public static final class Upgrades {
		public static final LocalisationItem Normal = new LocalisationItem("cctweaks.tooltip.items.computerUpgrade.normal");
	}

	/**
	 * Fetches an item from the language file
	 */
	public static final class LocalisationItem {

		private final String name;

		private LocalisationItem(String name) {
			this.name = name;
		}

		public String getLocalised() {
			return StatCollector.translateToLocal(name);
		}

		public String getFormattedLocalised(Object... args) {
			return StatCollector.translateToLocalFormatted(name, args);
		}

	}
}
