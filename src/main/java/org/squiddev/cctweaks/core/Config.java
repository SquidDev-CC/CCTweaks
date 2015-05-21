package org.squiddev.cctweaks.core;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.Configuration;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.turtle.TurtleUpgradeWirelessBridge;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class Config {
	public static final String LANGUAGE_ROOT = "config.cctweaks.";
	private static final String MISC = "misc";
	private static final String COMPUTER = "computer";
	private static final String TURTLE = "turtle";
	public static final String[] CATEGORIES = {TURTLE, COMPUTER, MISC};

	public static Configuration configuration;
	public static final ConfigData defaults = new ConfigData();
	public static ConfigData config = new ConfigData();

	/**
	 * List of disabled turtle actions
	 */
	public static Set<String> turtleDisabledActions;

	/**
	 * Allowed global variables
	 */
	public static Set<String> globalWhitelist;

	public static void init(File file) {
		configuration = new Configuration(file);
		configuration.load();

		sync();

		FMLCommonHandler.instance().bus().register(new Object() {
			@SubscribeEvent
			public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
				if (eventArgs.modID.equals(CCTweaks.ID)) {
					Config.sync();
				}
			}
		});
	}

	public static void sync() {
		ConfigData data = config = new ConfigData();
		ConfigData defaults = Config.defaults;

		// Turtle changes
		data.turtleFluxRefuelEnable = configuration.getBoolean(
			"Flux refuel",
			TURTLE,
			defaults.turtleFluxRefuelEnable,
			"Enable refuel from Redstone Flux items"
		);
		data.turtleFluxRefuelAmount = configuration.getInt(
			"Flux refuel amount",
			TURTLE,
			data.turtleFluxRefuelAmount,
			1, Integer.MAX_VALUE,
			"Amount of RF required for one refuel point"
		);

		data.turtleEuRefuelEnable = configuration.getBoolean(
			"EU refuel",
			TURTLE,
			defaults.turtleEuRefuelEnable,
			"Enable refuel from IC2 batteries"
		);
		data.turtleEuRefuelAmount = configuration.getInt(
			"EU refuel amount",
			TURTLE,
			data.turtleEuRefuelAmount,
			1, Integer.MAX_VALUE,
			"Amount of Eu required for one refuel point"
		);

		data.turtleDisabledActions = configuration.getStringList(
			"Disabled turtle actions",
			TURTLE,
			defaults.turtleDisabledActions,
			"Disabled turtle actions:\n(compare, compareTo, craft, detect, dig,\ndrop, equip, inspect, move, place,\nrefuel, select, suck, tool, turn)"
		);

		data.turtleWirelessBridgeId = configuration.getInt(
			"Wireless Bridge Id",
			TURTLE,
			defaults.turtleWirelessBridgeId,
			-1, Integer.MAX_VALUE,
			"The id for the network bridge upgrade. Set to -1 to disable"
		);

		// Computer changes
		data.enableComputerUpgrades = configuration.getBoolean(
			"Computer upgrade",
			COMPUTER,
			defaults.enableComputerUpgrades,
			"Enable upgrading computers"
		);

		data.enableDebugWand = configuration.getBoolean(
			"Debug wand",
			COMPUTER,
			defaults.enableDebugWand,
			"Enable the debug wand"
		);

		data.globalWhitelist = configuration.getStringList(
			"Whitelisted Globals",
			COMPUTER,
			defaults.globalWhitelist,
			"Globals to whitelist (are not set to nil).\nThis is NOT recommended for servers, use at your own risk"
		);

		data.computerThreadTimeout = configuration.getInt(
			"Computer timeout",
			COMPUTER,
			(int) defaults.computerThreadTimeout,
			1, Integer.MAX_VALUE,
			"Time in milliseconds before 'Too long without yielding' error\nYou cannot shutdown/reboot the computer during this time.\nUse "
		);

		data.luaJC = configuration.getBoolean(
			"LuaJC",
			COMPUTER,
			defaults.luaJC,
			"Compile Lua bytecode to Java bytecode"
		);

		data.luaJCVerify = configuration.getBoolean(
			"Verify LuaJC",
			COMPUTER,
			defaults.luaJCVerify,
			"Verify LuaJC sources on generation.\nThis will slow down compilation.\n" +
				"If you have errors, please turn this and debug and\nsend it with the bug report."
		);

		// Is debugging
		data.debug = configuration.getBoolean(
			"debug",
			MISC,
			defaults.debug,
			"Show debug messages"
		);

		// Is debugging
		data.debugItems = configuration.getBoolean(
			"debugItems",
			MISC,
			defaults.debugItems,
			"Enable debug blocks/items - requires debug"
		);

		data.deprecatedWarnings = configuration.getBoolean(
			"deprecatedWarnings",
			MISC,
			defaults.deprecatedWarnings,
			"Print a stacktrace to the console when a deprecated method is called"
		);

		// Handle generation of HashSets, etc...
		Set<String> disabledActions = turtleDisabledActions = new HashSet<String>();
		for (String action : data.turtleDisabledActions) {
			disabledActions.add("dan200.computercraft.shared.turtle.core.turtle" + action.toLowerCase() + "command");
		}

		globalWhitelist = new HashSet<String>(Arrays.asList(data.globalWhitelist));

		// Setup categories
		configuration.setCategoryComment(COMPUTER, "Computer changes");
		configuration.setCategoryRequiresMcRestart(COMPUTER, true);

		configuration.setCategoryComment(TURTLE, "Turtle tweaks");
		configuration.setCategoryRequiresMcRestart(TURTLE, true);

		configuration.setCategoryComment(MISC, "The lonely config category");

		for (String categoryName : CATEGORIES) {
			configuration.setCategoryLanguageKey(categoryName, LANGUAGE_ROOT + categoryName);
		}

		configuration.save();
	}

	public static final class ConfigData {
		/**
		 * Enable RF refueling
		 */
		public boolean turtleFluxRefuelEnable = true;

		/**
		 * RF refuel amount
		 */
		public int turtleFluxRefuelAmount = 100;

		/**
		 * Enable EU refueling
		 */
		public boolean turtleEuRefuelEnable = true;

		/**
		 * EU refuel amount
		 */
		public int turtleEuRefuelAmount = turtleFluxRefuelAmount / 4;

		/**
		 * Disabled turtle verbs
		 */
		public String[] turtleDisabledActions = new String[0];

		/**
		 * Id for {@link TurtleUpgradeWirelessBridge}
		 * TODO: Register on the wiki: http://www.computercraft.info/wiki/Turtle_Upgrade_IDs
		 */
		public int turtleWirelessBridgeId = 331;

		/**
		 * Enable computer upgrade
		 */
		public boolean enableComputerUpgrades = true;

		/**
		 * Enable debug want
		 */
		public boolean enableDebugWand = false;

		/**
		 * Global whitelist
		 */
		public String[] globalWhitelist = new String[0];

		/**
		 * Thread timeout for computers
		 */
		public long computerThreadTimeout = 5000L;

		/**
		 * Use LuaJC compiler
		 */
		public boolean luaJC = false;

		/**
		 * Verify classes on generation
		 */
		public boolean luaJCVerify = false;

		/**
		 * Print debug information
		 */
		public boolean debug = false;

		/**
		 * Enable the debug blocks/items
		 */
		public boolean debugItems = false;

		/**
		 * Throw exceptions on calling deprecated methods
		 */
		public boolean deprecatedWarnings = false;
	}
}
