package org.squiddev.cctweaks.core.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.core.reference.Config;

/**
 * Facade to the basic logger, which also forces debug calls
 * if {@link org.squiddev.cctweaks.core.reference.Config.ConfigData#debug} is enabled
 */
public class DebugLogger {
	private static final Logger logger = LogManager.getLogger(CCTweaks.ID);

	public static void debug(Marker marker, String message) {
		if (Config.config.debug) {
			logger.info(marker, message);
		}
	}

	public static void debug(Marker marker, String message, Object... params) {
		if (Config.config.debug) {
			logger.info(marker, message, params);
		}
	}

	public static void debug(Marker marker, String message, Throwable t) {
		if (Config.config.debug) {
			logger.info(marker, message, t);
		}
	}

	public static void debug(String message) {
		if (Config.config.debug) {
			logger.info(message);
		}
	}

	public static void debug(String message, Object... params) {
		if (Config.config.debug) {
			logger.info(message, params);
		}
	}

	public static void debug(String message, Throwable t) {
		if (Config.config.debug) {
			logger.info(message, t);
		}
	}

	public static void error(Marker marker, String message) {
		logger.error(marker, message);
	}

	public static void error(Marker marker, String message, Object... params) {
		logger.error(marker, message, params);
	}

	public static void error(Marker marker, String message, Throwable t) {
		logger.error(marker, message, t);
	}

	public static void error(String message) {
		logger.error(message);
	}

	public static void error(String message, Object... params) {
		logger.error(message, params);
	}

	public static void error(String message, Throwable t) {
		logger.error(message, t);
	}

	public static void info(Marker marker, String message) {
		logger.info(marker, message);
	}

	public static void info(Marker marker, String message, Object... params) {
		logger.info(marker, message, params);
	}

	public static void info(Marker marker, String message, Throwable t) {
		logger.info(marker, message, t);
	}

	public static void info(String message) {
		logger.info(message);
	}

	public static void info(String message, Object... params) {
		logger.info(message, params);
	}

	public static void info(String message, Throwable t) {
		logger.info(message, t);
	}

	public static void warn(Marker marker, String message) {
		logger.warn(marker, message);
	}

	public static void warn(Marker marker, String message, Object... params) {
		logger.warn(marker, message, params);
	}

	public static void warn(Marker marker, String message, Throwable t) {
		logger.warn(marker, message, t);
	}

	public static void warn(String message) {
		logger.warn(message);
	}

	public static void warn(String message, Object... params) {
		logger.warn(message, params);
	}

	public static void warn(String message, Throwable t) {
		logger.warn(message, t);
	}
}
