package org.squiddev.cctweaks.core.integration.multipart;

import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.Arrays;

public class RegisterBlockPart implements MultiPartRegistry.IPartFactory, MultiPartRegistry.IPartConverter {
	public TMultiPart createPart(String name, boolean client) {
		if (name.equals(CablePart.NAME)) return new CablePart();
		if (name.equals(ModemPart.NAME)) return new ModemPart();
		if (name.equals(ModemWithCableIntermediatePart.NAME)) return new ModemWithCableIntermediatePart();
		if (name.equals(WirelessBridgePart.NAME)) return new WirelessBridgePart(0);

		return null;
	}

	public void init() {
		MultiPartRegistry.registerConverter(this);
		MultiPartRegistry.registerParts(this, new String[]{CablePart.NAME, ModemPart.NAME, ModemWithCableIntermediatePart.NAME, WirelessBridgePart.NAME});
	}

	public Iterable<Block> blockTypes() {
		return Arrays.asList(new Block[]{ComputerCraft.Blocks.cable});
	}

	public TMultiPart convert(World world, BlockCoord pos) {
		TileEntity tile = world.getTileEntity(pos.x, pos.y, pos.z);
		if (tile != null && tile instanceof TileCable) {
			switch (((TileCable) tile).getPeripheralType()) {
				case Cable:
					return new CablePart();
				case WiredModem:
					return new ModemPart((TileCable) tile);
				case WiredModemWithCable:
					return new ModemWithCableIntermediatePart((TileCable) tile);
			}
		}

		return null;
	}
}
