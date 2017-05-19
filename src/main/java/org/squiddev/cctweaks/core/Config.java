package org.squiddev.cctweaks.core;

import net.minecraftforge.common.config.Configuration;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.configgen.*;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * The main config class
 */
@org.squiddev.configgen.Config(languagePrefix = "gui.config.cctweaks.")
public final class Config {
	public static Configuration configuration;
	public static Set<String> turtleDisabledActions;

	public static void init(File file) {
		org.squiddev.cctweaks.lua.ConfigForgeLoader.init(file);
		org.squiddev.cctweaks.core.ConfigForgeLoader.init(org.squiddev.cctweaks.lua.ConfigForgeLoader.getConfiguration());
	}

	public static void sync() {
		org.squiddev.cctweaks.core.ConfigForgeLoader.doSync();
		org.squiddev.cctweaks.lua.ConfigForgeLoader.sync();
	}

	@OnSync
	public static void onSync() {
		configuration = org.squiddev.cctweaks.lua.ConfigForgeLoader.getConfiguration();

		// Handle generation of HashSets, etc...
		Set<String> disabledActions = turtleDisabledActions = new HashSet<String>();
		for (String action : Turtle.disabledActions) {
			disabledActions.add(action.toLowerCase());
		}

		Computer.computerUpgradeCrafting &= Computer.computerUpgradeEnabled;

		Network.WirelessBridge.crafting &= Network.WirelessBridge.enabled;
		Network.WirelessBridge.turtleEnabled &= Network.WirelessBridge.enabled;

		if (Config.Computer.suspendInactive && !org.squiddev.cctweaks.lua.Config.Computer.MultiThreading.enabled) {
			Config.Computer.suspendInactive = false;
			DebugLogger.warn("Computer.suspendInactive requires multi-threading to be enabled. Falling back to default.");
		}
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
		 * Suspend computers and turtles which timeout, rather than shutting them down.
		 *
		 * Requires multi-threading to be on, though threads can be set to 1.
		 */
		@DefaultBoolean(false)
		@RequiresRestart
		public static boolean suspendInactive;

		/**
		 * Config options about creating custom ROMs.
		 */
		public static class CustomRom {
			/**
			 * Whether custom ROMs are enabled.
			 */
			@DefaultBoolean(true)
			public static boolean enabled;

			/**
			 * Whether crafting of custom ROMs is enabled.
			 */
			@DefaultBoolean(true)
			public static boolean crafting;
		}
	}

	/**
	 * Turtle tweaks and items.
	 */
	public static final class Turtle {
		/**
		 * Amount of RF/FE/Tesla required for one refuel point
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
		public static String[] disabledActions;

		/**
		 * Whether turtles should use MinecraftServer.isBlockProtected
		 * to check if a block can be dug.
		 */
		@DefaultBoolean(true)
		public static boolean useServerProtected;

		/**
		 * Whether turtles should use Forge events
		 * to check if a block can be dug.
		 */
		@DefaultBoolean(true)
		public static boolean useBlockEvent;

		/**
		 * Various tool host options
		 */
		public static class ToolHost {
			/**
			 * Enable the Tool Host
			 */
			@DefaultBoolean(true)
			@RequiresRestart
			public static boolean enabled;

			/**
			 * Enable the Tool Manipulator
			 */
			@DefaultBoolean(true)
			@RequiresRestart
			public static boolean advanced;

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
			 * Upgrade Id for Tool Manipulator
			 */
			@DefaultInt(333)
			@RequiresRestart
			@Range(min = 0)
			public static int advancedUpgradeId;

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
			@RequiresRestart(mc = false, world = true)
			public static boolean turtleEnabled;

			/**
			 * The turtle upgrade Id
			 */
			@DefaultInt(331)
			@Range(min = 1)
			@RequiresRestart
			public static int turtleId;

			/**
			 * Enable the Wireless Bridge upgrade for pocket computers.
			 */
			@DefaultBoolean(true)
			@RequiresRestart(mc = false, world = true)
			public static boolean pocketEnabled;
		}

		/**
		 * Various configuration options for network visualisation (provided by the debug wand).
		 */
		public static class Visualisation {
			/**
			 * Whether network visualisation is enabled
			 */
			@DefaultBoolean(true)
			public static boolean enabled;

			/**
			 * The maximum distance for which the network is sent to the client.
			 * Further distances may be rendered on the client.
			 */
			@DefaultInt(3)
			@Range(min = 1)
			public static int renderDistance;

			/**
			 * The cooldown between sending visualisation packets to the client.
			 *
			 * Prevents load on larget networks.
			 */
			@DefaultInt(5)
			@Range(min = 0)
			public static int cooldown;
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
		 * MC Multipart integration
		 */
		@DefaultBoolean(true)
		@RequiresRestart
		public static boolean mcMultipart;
	}

	/**
	 * Various tweaks that don't belong to anything
	 */
	public static final class Misc {
		/**
		 * Render pocket computers like maps.
		 *
		 * This means the terminal is visible when you hold a pocket computer,
		 * and can be interacted with as a map.
		 */
		@DefaultBoolean(true)
		public static boolean pocketMapRender;

		/**
		 * Fun rendering overlay for various objects.
		 * Basically I'm slightly vain.
		 */
		@DefaultBoolean(true)
		public static boolean funRender;
	}

	/**
	 * Controls over the packets sent between the server and client.
	 */
	public static final class Packets {
		/**
		 * Only broadcast computer state to those in the current dimension and in range or to those interacting with it.
		 */
		@DefaultBoolean(true)
		public static boolean updateLimiting;

		/**
		 * Only broadcast terminal state to those in interacting with the computer.
		 */
		@DefaultBoolean(true)
		public static boolean terminalLimiting;
	}

	/**
	 * Only used when testing and developing the mod.
	 * Nothing to see here, move along...
	 */
	public static final class Testing {
		/**
		 * Enable debug blocks/items.
		 * Only use for testing.
		 */
		@DefaultBoolean(false)
		public static boolean debugItems;

		/**
		 * Controller validation occurs by default as a
		 * way of ensuring that your network has been
		 * correctly created.
		 *
		 * By enabling this it is easier to trace
		 * faults, though it willl slow things down
		 * slightly
		 */
		@DefaultBoolean(false)
		public static boolean controllerValidation;
	}
}
