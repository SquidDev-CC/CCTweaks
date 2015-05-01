package org.squiddev.cctweaks.core.blocks;

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
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.IDataCard;
import org.squiddev.cctweaks.core.integration.multipart.MultipartIntegration;

/**
 * A bridge between two networks so they can communicate with each other
 */
public class WirelessBridge extends BlockBase<WirelessBridgeTile> implements ITileEntityProvider {
	public static IIcon smallIcon;

	public WirelessBridge() {
		super("wirelessBridge", WirelessBridgeTile.class);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new WirelessBridgeTile();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem();
		if (stack != null && stack.getItem() instanceof IDataCard) {
			WirelessBridgeTile tile = getTile(world, x, y, z);
			if (tile != null) return tile.onActivated(stack, (IDataCard) stack.getItem(), player);
		}

		return false;
	}

	@Override
	public void registerBlockIcons(IIconRegister register) {
		super.registerBlockIcons(register);

		// We need to find a better way to handle this as we are only checking if FMP is installed
		// However, we have to register under blocks, otherwise rendering gets borked
		if (Loader.isModLoaded(MultipartIntegration.NAME)) {
			smallIcon = register.registerIcon(this.getTextureName() + "Small");
		}
	}

	@Override
	public void init() {
		super.init();

		GameRegistry.addRecipe(new ItemStack(this),
			"GCG",
			"CMC",
			"GCG",

			'G', Items.gold_ingot,
			'C', PeripheralItemFactory.create(PeripheralType.Cable, null, 1),
			'M', PeripheralItemFactory.create(PeripheralType.WirelessModem, null, 1)
		);
	}
}
