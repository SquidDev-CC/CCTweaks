package squiddev.cctweaks.core.utils;

import dan200.computercraft.shared.computer.blocks.TileComputerBase;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Reflection for stuff Dan200 doesn't let us do.
 */
public final class ComputerAccessor {
	public static Class<?> tileComputerClass;
	public static Method tileCopy;

	public static Class<?> turtleTileClass;
	public static Field turtleTileMoved;

	static {
		try {
			tileComputerClass = Class.forName("dan200.computercraft.shared.computer.blocks.TileComputerBase");
			tileCopy = tileComputerClass.getDeclaredMethod("transferStateFrom", TileComputerBase.class);
			tileCopy.setAccessible(true);

			turtleTileClass = Class.forName("dan200.computercraft.shared.turtle.blocks.TileTurtle");
			turtleTileMoved = turtleTileClass.getDeclaredField("m_moved");
			turtleTileMoved.setAccessible(true);
		} catch (Exception e) {
			System.out.println("CCTweaks: ComputerCraft not found.");
			e.printStackTrace();
		}
	}
}
