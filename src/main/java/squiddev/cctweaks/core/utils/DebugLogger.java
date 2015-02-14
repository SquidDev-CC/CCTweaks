package squiddev.cctweaks.core.utils;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.luaj.vm2.lib.DebugLib;
import squiddev.cctweaks.core.reference.Config;

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
		logger.log(Level.INFO, message);
	}

	public static void warning(String message) {
		logger.log(Level.WARN, message);
	}

	public static void error(String message) {
		logger.log(Level.ERROR, message);
	}

	public static void traceback(String cls) {
		info(cls + ": " + DebugLib.traceback(1).replace('\n', ',').replace('\t', ' '));
	}

	public static void line(int line) {
		info("\t " + line);
	}
}
