package org.squiddev.cctweaks.integration.multipart;

import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.squiddev.cctweaks.integration.multipart.network.PartCable;
import org.squiddev.cctweaks.integration.multipart.network.PartModem;
import org.squiddev.cctweaks.integration.multipart.network.PartModemWithCableIntermediate;
import org.squiddev.cctweaks.integration.multipart.network.PartWirelessBridge;

import java.util.Arrays;

public class RegisterBlockPart implements MultiPartRegistry.IPartFactory, MultiPartRegistry.IPartConverter {
	@Override
	public TMultiPart createPart(String name, boolean client) {
		if (name.equals(PartCable.NAME)) return new PartCable();
		if (name.equals(PartModem.NAME)) return new PartModem();
		if (name.equals(PartModemWithCableIntermediate.NAME)) return new PartModemWithCableIntermediate();
		if (name.equals(PartWirelessBridge.NAME)) return new PartWirelessBridge(0);

		return null;
	}

	public void init() {
		MultiPartRegistry.registerConverter(this);
		MultiPartRegistry.registerParts(this, new String[]{PartCable.NAME, PartModem.NAME, PartModemWithCableIntermediate.NAME, PartWirelessBridge.NAME});
	}

	@Override
	public Iterable<Block> blockTypes() {
		return Arrays.asList(new Block[]{ComputerCraft.Blocks.cable});
	}

	@Override
	public TMultiPart convert(World world, BlockCoord pos) {
		TileEntity tile = world.getTileEntity(pos.x, pos.y, pos.z);
		if (tile != null && tile instanceof TileCable) {
			switch (((TileCable) tile).getPeripheralType()) {
				case Cable:
					return new PartCable();
				case WiredModem:
					return new PartModem((TileCable) tile);
				case WiredModemWithCable:
					return new PartModemWithCableIntermediate((TileCable) tile);
			}
		}

		return null;
	}
}
