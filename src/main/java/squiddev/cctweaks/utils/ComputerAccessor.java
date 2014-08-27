package squiddev.cctweaks.utils;

import java.lang.reflect.Method;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import dan200.computercraft.shared.computer.blocks.BlockComputerBase;
import dan200.computercraft.shared.computer.blocks.TileComputerBase;

/**
 * Reflection for stuff Dan200 doens't let us do.
 */
public final class ComputerAccessor {
	public static Class tileComputerClass;
	public static Method tileCopy;

	static {
		try{
			tileComputerClass = Class.forName("dan200.computercraft.shared.computer.blocks.TileComputerBase");
			tileCopy = tileComputerClass.getDeclaredMethod("transferStateFrom", TileComputerBase.class);
			tileCopy.setAccessible(true);
		} catch( Exception e ) {
			System.out.println( "CCTweaks: ComputerCraft not found." );
			e.printStackTrace();
		}
	}

	public static boolean isTileComputer(TileEntity tile) {
		return tile != null && (tile instanceof TileComputerBase);
	}

	public static boolean isBlockComputer(Block block) {
		return block != null && (block instanceof BlockComputerBase);
	}
}
