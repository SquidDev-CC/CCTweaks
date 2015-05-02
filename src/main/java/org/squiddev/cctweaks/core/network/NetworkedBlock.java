package org.squiddev.cctweaks.core.network;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.INetworkNodeBlock;
import org.squiddev.cctweaks.core.blocks.BaseBlock;
import org.squiddev.cctweaks.core.integration.multipart.MultipartIntegration;
import org.squiddev.cctweaks.core.network.bridge.WirelessBridgeTile;

/**
 * A bridge between two networks so they can communicate with each other
 */
public class NetworkedBlock extends BaseBlock<NetworkedTile> implements ITileEntityProvider, INetworkNodeBlock {
	public static IIcon smallIcon;

	public NetworkedBlock() {
		super("networkedBlock", NetworkedTile.class);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new WirelessBridgeTile();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		NetworkedTile tile = getTile(world, x, y, z);
		return tile != null && tile.onActivated(player, side);
	}

	@Override
	public void registerBlockIcons(IIconRegister register) {
		blockIcon = register.registerIcon(CCTweaks.RESOURCE_DOMAIN + ":wirelessBridge");

		// We need to find a better way to handle this as we are only checking if FMP is installed
		// However, we have to register under blocks, otherwise rendering gets borked
		if (Loader.isModLoaded(MultipartIntegration.NAME)) {
			smallIcon = register.registerIcon(CCTweaks.RESOURCE_DOMAIN + ":wirelessBridgeSmall");
		}
	}

	@Override
	public void preInit() {
		GameRegistry.registerBlock(this, name);
		GameRegistry.registerTileEntity(WirelessBridgeTile.class, "wirelessBridge");
	}

	@Override
	public void init() {
		super.init();

		GameRegistry.addRecipe(new ItemStack(this),
			"GMG",
			"CDC",
			"GMG",

			'G', Items.gold_ingot,
			'D', Items.diamond,
			'C', PeripheralItemFactory.create(PeripheralType.Cable, null, 1),
			'M', PeripheralItemFactory.create(PeripheralType.WirelessModem, null, 1)
		);
	}

	@Override
	public INetworkNode getNode(IBlockAccess world, int x, int y, int z, int meta) {
		return getTile(world, x, y, z);
	}
}
