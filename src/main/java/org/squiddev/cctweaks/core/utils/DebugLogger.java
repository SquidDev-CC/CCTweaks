package org.squiddev.cctweaks.core.utils;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.squiddev.cctweaks.core.reference.Config;

public class DebugLogger {
	private static Logger logger;

	public static void init(Logger log) {
		logger = log;
	}

	public static void debug(String message, Object... args) {
		if (Config.config.debug) {
			info(String.format(message, args));
		}
	}

	public static void info(String message) {
		log(Level.INFO, message);
	}

	public static void warning(String message) {
		log(Level.WARN, message);
	}

	public static void error(String message) {
		log(Level.ERROR, message);
	}

	private static void log(Level level, String message) {
		if (logger == null) {
			System.out.println("[" + level.toString() + "] " + message);
		} else {
			logger.log(level, message);
		}
	}
}
