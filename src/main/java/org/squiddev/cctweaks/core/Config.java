package org.squiddev.cctweaks.core;

import net.minecraftforge.common.config.Configuration;
import org.squiddev.configgen.DefaultBoolean;
import org.squiddev.configgen.DefaultInt;
import org.squiddev.configgen.OnSync;
import org.squiddev.configgen.Range;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@org.squiddev.configgen.Config(languagePrefix = "gui.config.cctweaks.")
public final class Config {
	public static Configuration configuration;
	public static Set<String> turtleDisabledActions;
	public static Set<String> globalWhitelist;

	public static void init(File file) {
		ConfigLoader.init(file);
	}

	public static void sync() {
		ConfigLoader.sync();
	}

	@OnSync
	public static void onSync() {
		configuration = ConfigLoader.getConfiguration();

		// Handle generation of HashSets, etc...
		Set<String> disabledActions = turtleDisabledActions = new HashSet<String>();
		for (String action : Turtle.disabledActions) {
			disabledActions.add("dan200.computercraft.shared.turtle.core.turtle" + action.toLowerCase() + "command");
		}

		globalWhitelist = new HashSet<String>(Arrays.asList(Computer.globalWhitelist));
	}

	public static final class Computer {
		/**
		 * Enable upgrading computers
		 */
		@DefaultBoolean(true)
		public static boolean computerUpgradeEnabled;

		/**
		 * Enable crafting the computer upgrade
		 *
		 * Requires computerUpgradeEnabled
		 */
		@DefaultBoolean(true)
		public static boolean computerUpgradeCrafting;

		/**
		 * Enable using the debug wand
		 */
		@DefaultBoolean(true)
		public static boolean debugWandEnabled;

		/**
		 * Globals to whitelist (are not set to nil).
		 * This is NOT recommended for servers, use at your own risk
		 */
		public static String[] globalWhitelist;

		/**
		 * Time in milliseconds before 'Too long without yielding' errors.
		 * You cannot shutdown/reboot the computer during this time.
		 * Use carefully.
		 */
		@DefaultInt(5000)
		@Range(min = 0)
		public static int computerThreadTimeout;

		/**
		 * Compile Lua bytecode to Java bytecode
		 */
		@DefaultBoolean(false)
		public static boolean luaJC;

		/**
		 * Verify LuaJC sources on generation.
		 * This will slow down compilation.
		 * If you have errors, please turn this and debug on and
		 * send it with the bug report.
		 */
		@DefaultBoolean(false)
		public static boolean luaJCVerify;
	}

	public static final class Turtle {
		/**
		 * Amount of RF required for one refuel point
		 * Set to 0 to disable"
		 */
		@DefaultInt(100)
		@Range(min = 0)
		public static int fluxRefuelAmount;

		/**
		 * Amount of Eu required for one refuel point
		 * Set to 0 to disable
		 */
		@DefaultInt(25)
		@Range(min = 0)
		public static int euRefuelAmount;

		/**
		 * Disabled turtle actions:
		 * (compare, compareTo, craft, detect, dig,
		 * drop, equip, inspect, move, place,
		 * refuel, select, suck, tool, turn)
		 */
		public static String[] disabledActions;

		/**
		 * TODO: Register on the wiki: http://www.computercraft.info/wiki/Turtle_Upgrade_IDs
		 */
		public static final int UPGRADE_START = 331;

		/**
		 * Enable the wireless bridge upgrade
		 */
		@DefaultBoolean(true)
		public static boolean wirelessBridgeEnabled;

		/**
		 * The id for the network bridge upgrade.
		 */
		@DefaultInt(UPGRADE_START)
		@Range(min = 1)
		public static int wirelessBridgeId;
	}

	public static final class Testing {
		/**
		 * Show debug messages.
		 * If you hit a bug, enable this, rerun and send the log
		 */
		@DefaultBoolean(false)
		public static boolean debug;

		/**
		 * Enable debug blocks/items.
		 * Only use for testing.
		 */
		@DefaultBoolean(false)
		public static boolean debugItems;
	}
}
