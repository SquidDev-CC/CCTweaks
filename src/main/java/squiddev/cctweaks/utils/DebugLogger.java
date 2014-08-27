package squiddev.cctweaks.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import squiddev.cctweaks.reference.Config;
import squiddev.cctweaks.reference.ModInfo;
import cpw.mods.fml.common.FMLLog;

public class DebugLogger {
	private static Logger logger;

	static{
		logger = Logger.getLogger(ModInfo.NAME);
		logger.setParent(FMLLog.getLogger());
	}

	public static void debug(String message, Object... args){
		if(Config.debug){
			info(String.format(message, args));
		}
	}

	public static void info(String message){
		logger.log(Level.INFO, message);
	}

	public static void warning(String message){
		logger.log(Level.WARNING, message);
	}

	public static void error(String message){
		logger.log(Level.SEVERE, message);
	}
}
