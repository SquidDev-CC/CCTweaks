package org.squiddev.cctweaks.core;

import net.minecraftforge.common.config.Configuration;
import org.squiddev.configgen.*;

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
			disabledActions.add(action.toLowerCase());
		}

		globalWhitelist = new HashSet<String>(Arrays.asList(Computer.globalWhitelist));

		Computer.computerUpgradeCrafting &= Computer.computerUpgradeEnabled;

		Network.WirelessBridge.crafting &= Network.WirelessBridge.enabled;
		Network.WirelessBridge.turtleEnabled &= Network.WirelessBridge.enabled;
	}

	/**
	 * Computer tweaks and items.
	 */
	public static final class Computer {
		/**
		 * Enable upgrading computers.
		 */
		@DefaultBoolean(true)
		public static boolean computerUpgradeEnabled;

		/**
		 * Enable crafting the computer upgrade.
		 * Requires computerUpgradeEnabled.
		 */
		@DefaultBoolean(true)
		@RequiresRestart
		public static boolean computerUpgradeCrafting;

		/**
		 * Enable using the debug wand.
		 */
		@DefaultBoolean(true)
		public static boolean debugWandEnabled;

		/**
		 * Globals to whitelist (are not set to nil).
		 * This is NOT recommended for servers, use at your own risk.
		 */
		@RequiresRestart(mc = false, world = true)
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
		@RequiresRestart(mc = false, world = true)
		public static boolean luaJC;

		/**
		 * Verify LuaJC sources on generation.
		 * This will slow down compilation.
		 * If you have errors, please turn this and debug on and
		 * send it with the bug report.
		 * TODO: Get this working again
		 */
		// @DefaultBoolean(false)
		// public static boolean luaJCVerify;
	}

	/**
	 * Turtle tweaks and items.
	 */
	public static final class Turtle {
		/**
		 * Amount of RF required for one refuel point
		 * Set to 0 to disable.
		 */
		@DefaultInt(100)
		@Range(min = 0)
		public static int fluxRefuelAmount;

		/**
		 * Amount of Eu required for one refuel point.
		 * Set to 0 to disable.
		 */
		@DefaultInt(25)
		@Range(min = 0)
		public static int euRefuelAmount;

		/**
		 * Disabled turtle actions:
		 * (compare, compareTo, craft, detect, dig,
		 * drop, equip, inspect, move, place,
		 * refuel, select, suck, tool, turn).
		 */
		@RequiresRestart(mc = false, world = true)
		public static String[] disabledActions;

		/**
		 * Various tool host options
		 */
		public static class ToolHost {
			/**
			 * Enable the Tool Host
			 */
			@DefaultBoolean(true)
			public static boolean enabled;

			/**
			 * Enable crafting the Tool Host
			 */
			@DefaultBoolean(true)
			@RequiresRestart
			public static boolean crafting;

			/**
			 * Upgrade Id
			 */
			@DefaultInt(332)
			@RequiresRestart
			@Range(min = 0)
			public static int upgradeId;

			/**
			 * The dig speed factor for tool hosts.
			 * 20 is about normal player speed.
			 */
			@DefaultInt(10)
			@Range(min = 1)
			public static int digFactor;
		}
	}

	/**
	 * Additional network functionality.
	 */
	public static final class Network {
		/**
		 * The wireless bridge allows you to connect
		 * wired networks across dimensions.
		 */
		public static class WirelessBridge {
			/**
			 * Enable the wireless bridge
			 */
			@DefaultBoolean(true)
			@RequiresRestart(mc = false, world = true)
			public static boolean enabled;

			/**
			 * Enable the crafting of Wireless Bridges.
			 */
			@DefaultBoolean(true)
			@RequiresRestart
			public static boolean crafting;

			/**
			 * Enable the Wireless Bridge upgrade for turtles.
			 */
			@DefaultBoolean(true)
			@RequiresRestart
			public static boolean turtleEnabled;

			// TODO: Register on the wiki: http://www.computercraft.info/wiki/Turtle_Upgrade_IDs
			/**
			 * The turtle upgrade Id
			 */
			@DefaultInt(331)
			@Range(min = 1)
			@RequiresRestart
			public static int turtleId;
		}

		/**
		 * Enable the crafting of full block modems.
		 *
		 * If you disable, existing ones will still function,
		 * and you can obtain them from creative.
		 */
		@DefaultBoolean(true)
		@RequiresRestart
		public static boolean fullBlockModemCrafting;
	}

	/**
	 * Integration with other mods.
	 */
	@RequiresRestart
	public static final class Integration {
		/**
		 * Allows pushing items from one inventory
		 * to another inventory on the network.
		 */
		@DefaultBoolean(true)
		public static boolean openPeripheralInventories;

		/**
		 * Enable ChickenBones Multipart
		 * (aka ForgeMultipart) integration.
		 */
		@DefaultBoolean(true)
		public static boolean cbMultipart;
	}

	/**
	 * Only used when testing and developing the mod.
	 * Nothing to see here, move along...
	 */
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
