package org.squiddev.cctweaks.blocks.network;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.blocks.BlockBase;
import org.squiddev.cctweaks.blocks.IMultiBlock;
import org.squiddev.cctweaks.blocks.TileBase;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.cctweaks.core.utils.Helpers;
import org.squiddev.cctweaks.items.ItemMultiBlock;

import java.lang.reflect.Field;
import java.util.List;

/**
 * A bridge between two networks so they can communicate with each other
 */
public class BlockNetworked extends BlockBase<TileBase> implements IMultiBlock {
	@SideOnly(Side.CLIENT)
	public static IIcon bridgeIcon;
	@SideOnly(Side.CLIENT)
	public static IIcon bridgeSmallIcon;
	@SideOnly(Side.CLIENT)
	public static IIcon[] modemIcons;

	public BlockNetworked() {
		super("networkedBlock", TileBase.class);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		switch (meta) {
			case 0:
				return new TileNetworkedWirelessBridge();
			case 1:
				return new TileNetworkedModem();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List itemStacks) {
		// Wireless bridge
		itemStacks.add(new ItemStack(this, 1, 0));
		itemStacks.add(new ItemStack(this, 1, 1));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		bridgeIcon = blockIcon = register.registerIcon(CCTweaks.RESOURCE_DOMAIN + ":wirelessBridge");
		bridgeSmallIcon = register.registerIcon(CCTweaks.RESOURCE_DOMAIN + ":wirelessBridgeSmall");

		// Clear the cache to ensure we have the latest texture
		modemIcons = null;
	}

	@SideOnly(Side.CLIENT)
	private IIcon getModemIcon(int id) {
		IIcon[] icons;
		if ((icons = modemIcons) == null) {
			icons = BlockNetworked.modemIcons = new IIcon[4];
			try {
				Field field = TileCable.class.getDeclaredField("s_modemIcons");
				field.setAccessible(true);
				IIcon[] modemIcons = (IIcon[]) field.get(null);
				for (int i = 0; i < 4; i++) {
					icons[i] = modemIcons[i * 2];
				}
			} catch (IllegalAccessException e) {
				DebugLogger.error("Cannot find TileCable texture", e);
			} catch (NoSuchFieldException e) {
				DebugLogger.error("Cannot find TileCable texture", e);
			}
		}

		return icons[id];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		switch (meta) {
			case 0:
				return bridgeIcon;
			case 1:
				return getModemIcon(0);
		}

		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
		int meta = world.getBlockMetadata(x, y, z);
		switch (meta) {
			case 1: {
				TileBase tile = getTile(world, x, y, z);
				if (tile != null && tile instanceof TileNetworkedModem) {
					return getModemIcon(((TileNetworkedModem) tile).modem.state);
				}
			}

		}
		return getIcon(side, meta);
	}

	@Override
	public String getUnlocalizedName(int meta) {
		switch (meta) {
			case 0:
				return getUnlocalizedName() + ".wirelessBridge";
			case 1:
				return getUnlocalizedName() + ".wiredModem";
		}
		return getUnlocalizedName();
	}

	@Override
	public int damageDropped(int damage) {
		return damage;
	}

	@Override
	public void preInit() {
		GameRegistry.registerBlock(this, ItemMultiBlock.class, name);
		GameRegistry.registerTileEntity(TileNetworkedWirelessBridge.class, "wirelessBridge");
		GameRegistry.registerTileEntity(TileNetworkedModem.class, "wiredModem");
	}

	@Override
	public void init() {
		super.init();

		if (Config.Network.WirelessBridge.crafting) {
			Helpers.alternateCrafting(new ItemStack(this, 1, 0), 'C', 'M',
				"GMG",
				"CDC",
				"GMG",

				'G', Items.gold_ingot,
				'D', Items.diamond,
				'C', PeripheralItemFactory.create(PeripheralType.Cable, null, 1),
				'M', PeripheralItemFactory.create(PeripheralType.WirelessModem, null, 1)
			);
		}

		if (Config.Network.fullBlockModemCrafting) {
			Helpers.twoWayCrafting(new ItemStack(this, 1, 1), PeripheralItemFactory.create(PeripheralType.WiredModem, null, 1));
		}
	}
}
