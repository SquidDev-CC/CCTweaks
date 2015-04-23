package org.squiddev.cctweaks.core.integration.multipart;

import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.shared.peripheral.common.IPeripheralTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.INetworkNodeProvider;
import org.squiddev.cctweaks.api.network.NetworkRegistry;
import org.squiddev.cctweaks.core.integration.IntegrationRegistry;

/**
 * Adds various multipart support constructs
 */
public class MultipartIntegration extends IntegrationRegistry.ModIntegrationModule {
	public static final String NAME = "ForgeMultipart";

	public MultipartIntegration() {
		super(NAME);
	}

	@Override
	public void load() {
		new RegisterBlockPart().init();

		NetworkRegistry.addNodeProvider(new INetworkNodeProvider() {
			@Override
			public INetworkNode getNode(TileEntity tile) {
				if (tile instanceof TileMultipart) {
					// Cables reside in the central slot so we can just fetch that
					// instead and use the cable to delegate to other nodes in the multipart
					TMultiPart part = ((TileMultipart) tile).partMap(6);
					if (part != null && part instanceof INetworkNode) {
						return (INetworkNode) part;
					}
				}

				return null;
			}

			@Override
			public boolean isNode(TileEntity tile) {
				return getNode(tile) != null;
			}
		});

		ComputerCraft.registerPeripheralProvider(new IPeripheralProvider() {
			@Override
			public IPeripheral getPeripheral(World world, int x, int y, int z, int side) {
				TileEntity tile = world.getTileEntity(x, y, z);

				if (tile != null && tile instanceof TileMultipart) {
					TMultiPart part = ((TileMultipart) tile).partMap(side);

					if (part != null) {
						if (part instanceof IPeripheral) {
							return (IPeripheral) part;
						} else if (part instanceof IPeripheralTile) {
							return ((IPeripheralTile) part).getPeripheral(side);
						}
					}
				}

				return null;
			}
		});
	}
}
