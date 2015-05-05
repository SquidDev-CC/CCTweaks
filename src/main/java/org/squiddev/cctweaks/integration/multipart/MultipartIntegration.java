package org.squiddev.cctweaks.integration.multipart;

import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.shared.peripheral.common.IPeripheralTile;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.INetworkNodeProvider;
import org.squiddev.cctweaks.api.network.NetworkAPI;
import org.squiddev.cctweaks.core.registry.IClientModule;
import org.squiddev.cctweaks.core.registry.Registry;
import org.squiddev.cctweaks.core.utils.Helpers;
import org.squiddev.cctweaks.integration.ModIntegration;

/**
 * Adds various multipart support constructs
 */
public class MultipartIntegration extends ModIntegration implements IClientModule {
	public static final String NAME = "ForgeMultipart";

	public static ItemPart itemPart;

	public MultipartIntegration() {
		super(NAME);
	}

	@Override
	public void preInit() {
		new RegisterBlockPart().init();
		itemPart = new ItemPart();
		itemPart.preInit();
	}

	@Override
	public void init() {
		itemPart.init();

		Helpers.twoWayCrafting(new ItemStack(Registry.blockNetworked), new ItemStack(itemPart));

		NetworkAPI.registry().addNodeProvider(new INetworkNodeProvider() {
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

	@Override
	@SideOnly(Side.CLIENT)
	public void clientInit() {
		MinecraftForgeClient.registerItemRenderer(itemPart, new ItemPart.Renderer());
	}
}
