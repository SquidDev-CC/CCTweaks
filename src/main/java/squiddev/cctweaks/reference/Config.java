package squiddev.cctweaks.reference;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.config.Configuration;

public final class Config {
	// Feature enabled
	public static boolean enableItemComputerUpgrades = true;
	public static boolean enableTurtleToolHost = true;

	// Turtle Upgrade Ids
	private static int turtleStartId = 360;
	public static int turtleToolHostId = ++turtleStartId;

	public static Set<String> globalWhitelist = new HashSet<String>();
	public static int computerThreadTimeout = 5000;

	// Debugging variables
	public static boolean debug;

	public static final class ConfigHandler{
		public static Configuration config;

		private static final String ITEMS = "items";
		private static final String MISC = "misc";
		private static final String COMPUTER = "computer";

		// Description
		private static final String ENABLE_FORMAT = "Enable the %s";
		private static final String TURTLE_ID_FORMAT = "The turtle upgrade id of '%s'";

		public static final String[] CATEGORIES = {ITEMS, COMPUTER, MISC };
		public static final String LANGUAGE_ROOT = "config.cctweaks.";

		public static void init(File file) {
			config = new Configuration(file);
			config.load();

			sync();
		}

		public static void sync(){
			// Enabled
			enableItemComputerUpgrades = getEnabled("Computer Upgrade");
			enableTurtleToolHost = getEnabled("Tool Host");

			// Turtle Ids
			turtleToolHostId = getTurtleUpgradeId("Tool Host ID", turtleToolHostId);

			// Tweak regions
			globalWhitelist = new HashSet<String>(Arrays.asList(config.getStringList("Whitelisted Globals", COMPUTER, new String[0], "Globals to whitelist (are not set to nil)")));
			computerThreadTimeout = config.getInt("Computer timeout", COMPUTER, computerThreadTimeout, 1, Integer.MAX_VALUE, "Time in milliseconds before 'Too long without yielding' error");

			// Is debugging
			debug = config.getBoolean("debugging", MISC, false, "Is debugging");

			// Pretty comments
			config.setCategoryComment(ITEMS, "Items enabled and settings");
			config.setCategoryRequiresMcRestart(ITEMS, true);

			config.setCategoryComment(COMPUTER, "Tweaks to computer classes");
			config.setCategoryRequiresMcRestart(COMPUTER, true);

			config.setCategoryComment(MISC, "Random things (doesn't do much really)");

			for(String categoryName : CATEGORIES) {
				config.setCategoryLanguageKey(categoryName, LANGUAGE_ROOT + categoryName);
			}

			config.save();
		}
		private static boolean getEnabled(String key) {
			return config.getBoolean(key, ITEMS, true, String.format(ENABLE_FORMAT, key));
		}

		private static int getTurtleUpgradeId(String key, int defaultValue)
		{
			return config.getInt(key, ITEMS, defaultValue, 64, 32767, String.format(TURTLE_ID_FORMAT, key));
		}
	}
}