package org.squiddev.cctweaks.core.network;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
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
import org.squiddev.cctweaks.core.blocks.IMultiBlock;
import org.squiddev.cctweaks.core.integration.multipart.MultipartIntegration;
import org.squiddev.cctweaks.core.items.MultiBlockItem;
import org.squiddev.cctweaks.core.network.bridge.WirelessBridgeTile;
import org.squiddev.cctweaks.core.utils.Helpers;

/**
 * A bridge between two networks so they can communicate with each other
 */
public class NetworkedBlock extends BaseBlock<NetworkedTile> implements INetworkNodeBlock, IMultiBlock {
	public static IIcon bridgeIcon;
	public static IIcon bridgeSmallIcon;

	public NetworkedBlock() {
		super("networkedBlock", NetworkedTile.class);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		switch (meta) {
			case 0:
				return new WirelessBridgeTile();
		}
		return null;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		NetworkedTile tile = getTile(world, x, y, z);
		return tile != null && tile.onActivated(player, side);
	}

	@Override
	public void registerBlockIcons(IIconRegister register) {
		bridgeIcon = blockIcon = register.registerIcon(CCTweaks.RESOURCE_DOMAIN + ":wirelessBridge");

		// We need to find a better way to handle this as we are only checking if CBMP is installed
		// However, we have to register under blocks, otherwise rendering gets borked
		if (Loader.isModLoaded(MultipartIntegration.NAME)) {
			bridgeSmallIcon = register.registerIcon(CCTweaks.RESOURCE_DOMAIN + ":wirelessBridgeSmall");
		}
	}


	@Override
	public String getUnlocalizedName(int meta) {
		switch (meta) {
			case 0:
				return getUnlocalizedName() + ".wirelessBridge";
		}
		return getUnlocalizedName();
	}

	@Override
	public int damageDropped(int damage) {
		return damage;
	}

	@Override
	public void preInit() {
		GameRegistry.registerBlock(this, MultiBlockItem.class, name);
		GameRegistry.registerTileEntity(WirelessBridgeTile.class, "wirelessBridge");
	}

	@Override
	public void init() {
		super.init();

		Helpers.alternateCrafting(new ItemStack(this), 'C', 'M',
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
