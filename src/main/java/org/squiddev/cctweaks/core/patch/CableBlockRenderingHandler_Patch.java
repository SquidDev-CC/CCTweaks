package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.client.render.FixedRenderBlocks;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.BlockCable;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.NetworkAPI;

public class CableBlockRenderingHandler_Patch {
	private static FixedRenderBlocks fixedRenderBlocks;

	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelID, RenderBlocks renderblocks) {
		if (modelID == ComputerCraft.Blocks.cable.blockRenderID) {
			if (fixedRenderBlocks == null) fixedRenderBlocks = new FixedRenderBlocks();

			BlockCable cable = (BlockCable) block;
			PeripheralType type = cable.getPeripheralType(world, x, y, z);

			if ((type == PeripheralType.Cable) || (type == PeripheralType.WiredModemWithCable)) {
				fixedRenderBlocks.setWorld(world);
				fixedRenderBlocks.setRenderBounds(0.375D, 0.375D, 0.375D, 0.625D, 0.625D, 0.625D);
				fixedRenderBlocks.renderStandardBlock(block, x, y, z);
				int modemDir;
				if (type == PeripheralType.WiredModemWithCable) {
					modemDir = cable.getDirection(world, x, y, z);
				} else {
					modemDir = -1;
				}
				for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
					IWorldNetworkNode node;
					if (dir.ordinal() == modemDir || ((node = NetworkAPI.registry().getNode(
						world,
						x + dir.offsetX,
						y + dir.offsetY,
						z + dir.offsetZ
					)) != null && node.canConnect(dir.getOpposite()))) {
						fixedRenderBlocks.setRenderBounds(
							dir.offsetX == -1 ? 0 : dir.offsetX == 1 ? 0.625 : 0.375,
							dir.offsetY == -1 ? 0 : dir.offsetY == 1 ? 0.625 : 0.375,
							dir.offsetZ == -1 ? 0 : dir.offsetZ == 1 ? 0.625 : 0.375,
							dir.offsetX == -1 ? 0.375 : dir.offsetX == 1 ? 1 : 0.625,
							dir.offsetY == -1 ? 0.375 : dir.offsetY == 1 ? 1 : 0.625,
							dir.offsetZ == -1 ? 0.375 : dir.offsetZ == 1 ? 1 : 0.625
						);
						fixedRenderBlocks.renderStandardBlock(block, x, y, z);
					}
				}

				block.setBlockBoundsBasedOnState(world, x, y, z);
			}
			if ((type == PeripheralType.WiredModem) || (type == PeripheralType.WiredModemWithCable)) {
				BlockCable.renderAsModem = true;
				block.setBlockBoundsBasedOnState(world, x, y, z);
				fixedRenderBlocks.setWorld(world);
				fixedRenderBlocks.setRenderBoundsFromBlock(block);
				fixedRenderBlocks.renderStandardBlock(block, x, y, z);
				BlockCable.renderAsModem = false;
				block.setBlockBoundsBasedOnState(world, x, y, z);
			}
			return true;
		}
		return false;
	}
}
