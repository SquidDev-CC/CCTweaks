package org.squiddev.cctweaks.integration.multipart;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.IPartConverter;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.squiddev.cctweaks.core.network.modem.SinglePeripheralModem;
import org.squiddev.cctweaks.core.utils.ComputerAccessor;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.cctweaks.integration.multipart.network.PartCable;
import org.squiddev.cctweaks.integration.multipart.network.PartModem;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class MultipartConverter implements IPartConverter.IPartConverter2, IPartConverter.IReversePartConverter {
	@Override
	public Collection<Block> getConvertableBlocks() {
		return Collections.<Block>singleton(ComputerCraft.Blocks.cable);
	}

	@Override
	public Collection<? extends IMultipart> convertBlock(IBlockAccess world, BlockPos position, boolean simulate) {
		TileEntity te = world.getTileEntity(position);

		if (te == null || !(te instanceof TileCable)) return Collections.emptySet();

		TileCable cable = (TileCable) te;
		switch (cable.getPeripheralType()) {
			case Cable:
				return Collections.singleton(new PartCable());
			case WiredModem:
				return Collections.singleton(new PartModem(cable));
			case WiredModemWithCable:
				return Arrays.<IMultipart>asList(new PartCable(), new PartModem(cable));
			default:
				return Collections.emptySet();
		}
	}

	@Override
	public boolean convertToBlock(IMultipartContainer container) {
		Collection<? extends IMultipart> parts = container.getParts();
		if (parts.size() == 0 || parts.size() > 2) return false;

		boolean cable = false;
		PartModem modem = null;

		for (IMultipart part : parts) {
			if (part instanceof PartCable) {
				if (cable) {
					return false;
				} else {
					cable = true;
				}
			} else if (part instanceof PartModem) {
				if (modem != null) {
					return false;
				} else {
					modem = (PartModem) part;
				}
			} else {
				return false;
			}
		}

		EnumFacing facing = modem == null ? EnumFacing.DOWN : modem.getSide();
		PeripheralType type = cable ? PeripheralType.Cable : PeripheralType.WiredModem;
		if (cable && modem != null) type = PeripheralType.WiredModemWithCable;

		World world = container.getWorldIn();
		BlockPos pos = container.getPosIn();

		world.setBlockState(pos, ComputerCraft.Blocks.cable.getDefaultBlockState(type, facing.getOpposite()));

		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileCable) {
			TileCable tCable = (TileCable) tile;
			if (modem != null) {

				try {
					SinglePeripheralModem peripheralModem = (SinglePeripheralModem) ComputerAccessor.cableModem.get(tCable);

					peripheralModem.id = modem.modem.id;
					peripheralModem.setPeripheralEnabled(modem.modem.isEnabled());
				} catch (Exception e) {
					DebugLogger.debug("Cannot get modem from tile", e);
				}
			}
		}

		return true;
	}
}
