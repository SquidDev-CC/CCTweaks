package org.squiddev.cctweaks.integration.multipart;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import mcmultipart.multipart.*;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.CCTweaksAPI;
import org.squiddev.cctweaks.api.network.INetworkNodeProvider;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHost;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.integration.ModIntegration;
import org.squiddev.cctweaks.integration.multipart.network.PartCable;
import org.squiddev.cctweaks.integration.multipart.network.PartModem;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class MultipartIntegration extends ModIntegration {
	public static final String MOD_NAME = "mcmultipart";

	public MultipartIntegration() {
		super(MOD_NAME);
	}

	@Override
	public boolean canLoad() {
		return super.canLoad() && Config.Integration.mcMultipart;
	}

	@Override
	public void preInit() {
		// Register parts
		MultipartRegistry.registerPart(PartCable.class, CCTweaks.NAME + ":Cable");
		MultipartRegistry.registerPart(PartModem.class, CCTweaks.NAME + ":Modem");

		// All the providers!
		MultipartRegistry.registerPartConverter(new IPartConverter.IPartConverter2() {
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
						return Arrays.asList(new PartCable(), new PartModem(cable));
					default:
						return Collections.emptySet();
				}
			}
		});
		CCTweaksAPI.instance().networkRegistry().addNodeProvider(new INetworkNodeProvider() {
			@Override
			public IWorldNetworkNode getNode(TileEntity tile) {
				if (tile instanceof IMultipartContainer) {
					IMultipart part = ((IMultipartContainer) tile).getPartInSlot(PartSlot.CENTER);
					return part == null ? null : MultipartHelpers.getWorldNode(part);
				}

				return null;
			}

			@Override
			public boolean isNode(TileEntity tile) {
				return getNode(tile) != null;
			}
		});
		ComputerCraftAPI.registerPeripheralProvider(new IPeripheralProvider() {
			@Override
			public IPeripheral getPeripheral(World world, BlockPos position, EnumFacing enumFacing) {
				TileEntity tile = world.getTileEntity(position);
				if (tile instanceof IMultipartContainer) {
					IMultipart part = ((IMultipartContainer) tile).getPartInSlot(PartSlot.getFaceSlot(enumFacing));
					if (part instanceof IPeripheral) return (IPeripheral) part;
					if (part instanceof IPeripheralHost) return ((IPeripheralHost) part).getPeripheral(enumFacing);
				}
				return null;
			}
		});
	}
}
