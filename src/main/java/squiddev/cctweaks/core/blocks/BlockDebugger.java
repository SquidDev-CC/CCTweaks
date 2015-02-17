package squiddev.cctweaks.core.blocks;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * A debugger for Computers
 */
public class BlockDebugger extends BlockBase implements ITileEntityProvider {

	protected BlockDebugger(String name) {
		super("debugger");
	}

	/**
	 * Returns a new instance of a block's tile entity class. Called on placing the block.
	 *
	 * @param world    The world to place it in
	 * @param metadata The metadata of the block
	 */
	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileDebugger();
	}
}
