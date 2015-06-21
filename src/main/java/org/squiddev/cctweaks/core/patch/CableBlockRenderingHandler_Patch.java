package org.squiddev.cctweaks.core.patch;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.client.render.FixedRenderBlocks;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.BlockCable;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.network.INetworkHelpers;
import org.squiddev.cctweaks.api.network.NetworkAPI;

public abstract class CableBlockRenderingHandler_Patch implements ISimpleBlockRenderingHandler {
	public static final double MIN = 0.375;
	public static final double MAX = 1 - MIN;

	private static FixedRenderBlocks fixedRenderBlocks;

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelID, RenderBlocks renderblocks) {
		if (modelID == ComputerCraft.Blocks.cable.blockRenderID) {
			if (fixedRenderBlocks == null) fixedRenderBlocks = new FixedRenderBlocks();

			BlockCable cable = (BlockCable) block;
			PeripheralType type = cable.getPeripheralType(world, x, y, z);

			if (type == PeripheralType.Cable || type == PeripheralType.WiredModemWithCable) {
				fixedRenderBlocks.setWorld(world);
				fixedRenderBlocks.setRenderBounds(MIN, MIN, MIN, MAX, MAX, MAX);
				fixedRenderBlocks.renderStandardBlock(block, x, y, z);
				int modemDir;
				if (type == PeripheralType.WiredModemWithCable) {
					modemDir = cable.getDirection(world, x, y, z);
				} else {
					modemDir = -1;
				}
				INetworkHelpers helpers = NetworkAPI.helpers();
				for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
					if (dir.ordinal() == modemDir || helpers.canConnect(world, x, y, z, dir)) {
						fixedRenderBlocks.setRenderBounds(
							dir.offsetX == -1 ? 0 : dir.offsetX == 1 ? MAX : MIN,
							dir.offsetY == -1 ? 0 : dir.offsetY == 1 ? MAX : MIN,
							dir.offsetZ == -1 ? 0 : dir.offsetZ == 1 ? MAX : MIN,
							dir.offsetX == -1 ? MIN : dir.offsetX == 1 ? 1 : MAX,
							dir.offsetY == -1 ? MIN : dir.offsetY == 1 ? 1 : MAX,
							dir.offsetZ == -1 ? MIN : dir.offsetZ == 1 ? 1 : MAX
						);
						fixedRenderBlocks.renderStandardBlock(block, x, y, z);
					}
				}

				block.setBlockBoundsBasedOnState(world, x, y, z);
			}
			if (type == PeripheralType.WiredModem || type == PeripheralType.WiredModemWithCable) {
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
