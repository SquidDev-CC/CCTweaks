package org.squiddev.cctweaks.blocks.network;

import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.squiddev.cctweaks.blocks.BlockBase;
import org.squiddev.cctweaks.blocks.IMultiBlock;
import org.squiddev.cctweaks.blocks.TileBase;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.utils.Helpers;
import org.squiddev.cctweaks.items.ItemMultiBlock;

import java.util.List;

/**
 * A bridge between two networks so they can communicate with each other
 */
public class BlockNetworked extends BlockBase<TileBase> implements IMultiBlock {
	public enum BlockNetworkedType {
		MODEM,
		WIRELESS_BRIDGE
	}

	public static final PropertyEnum TYPE = PropertyEnum.create("type", BlockNetworkedType.class);

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
	public String getUnlocalizedName(int meta) {
		switch (meta) {
			case 0:
				return getUnlocalizedName() + ".wirelessBridge";
			case 1:
				return getUnlocalizedName() + ".wiredModem";
		}
		return getUnlocalizedName();
	}

	// TODO: Update damage dropped. Which methods to I override?

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
