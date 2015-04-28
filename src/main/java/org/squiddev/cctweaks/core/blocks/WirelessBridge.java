package org.squiddev.cctweaks.core.blocks;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.IDataCard;

/**
 * A bridge between two networks so they can communicate with each other
 */
public class WirelessBridge extends BlockBase<WirelessBridgeTile> implements ITileEntityProvider {
	public WirelessBridge() {
		super("wirelessBridge", WirelessBridgeTile.class);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new WirelessBridgeTile();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem();
		if (stack != null && stack.getItem() instanceof IDataCard) {
			WirelessBridgeTile tile = getTile(world, x, y, z);
			if (tile != null) return tile.onActivated(stack, (IDataCard) stack.getItem(), player);
		}

		return false;
	}
}
