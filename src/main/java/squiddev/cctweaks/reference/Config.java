package squiddev.cctweaks.reference;

import java.io.File;

import net.minecraftforge.common.Configuration;

public final class Config {
	// Item ID
	private static int startItemID = 4490;
	public static int itemIdComputerUpgrade = startItemID++;

	// Feature enabled
	public static boolean enableItemComputerUpgrades;

	// Debugging variables
	public static boolean debug;

	public static final class ConfigHandler{
		private static Configuration config;

		// Sections
		private static final String ITEMID = "itemId";
		private static final String BLOCKID = "blockId";
		private static final String ENABLED = "enabled";

		// Description
		private static final String ENABLE_FORMAT = "Enable the %s";
		private static final String ITEMID_FORMAT = "The ID for the %s Item";
		private static final String BLOCKID_FORMAT = "The Block ID of the %s Block";


		public static void init(File file) {
			config = new Configuration(file);
			config.load();

			sync();
		}
		public static void sync(){
			// Item ids
			itemIdComputerUpgrade = getItemId("Computer Upgrade", itemIdComputerUpgrade);

			// Enabled
			enableItemComputerUpgrades = getEnabled("Computer Upgrade");

			// Is debugging
			debug = getBoolean("misc", "debugging", false, "Is debugging");

			if (config.hasChanged()) {
				config.save();
			}
		}

		private static boolean getBoolean(String cat, String key, boolean defaultBool, String desc) {
			return config.get(cat, key, defaultBool, desc).getBoolean(defaultBool);
		}

		private static int getInt(String cat, String key, int defaultInt, String desc) {
			return config.get(cat, key, defaultInt, desc).getInt();
		}

		private static boolean getEnabled(String key) {
			return getBoolean(ENABLED, key, true, String.format(ENABLE_FORMAT, key));
		}

		private static int getBlockId(String key, int defId) {
			return getInt(BLOCKID, key, defId, String.format(BLOCKID_FORMAT, key));
		}

		private static int getItemId(String key, int defId) {
			return getInt(ITEMID, key, defId, String.format(ITEMID_FORMAT, key));
		}


	}
}