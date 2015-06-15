package org.squiddev.cctweaks.blocks.debug;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.blocks.BlockBase;
import org.squiddev.cctweaks.blocks.IMultiBlock;
import org.squiddev.cctweaks.blocks.TileBase;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.items.ItemMultiBlock;

import java.util.List;

/**
 * The base debug block that provides debug TileEntities
 */
public class BlockDebug extends BlockBase<TileBase> implements IMultiBlock {
	private IIcon peripheralIcon;
	private IIcon networkedPeripheralIcon;
	private IIcon nodeIcon;

	public BlockDebug() {
		super("debugBlock", TileBase.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List itemStacks) {
		itemStacks.add(new ItemStack(this, 1, 0));
		itemStacks.add(new ItemStack(this, 1, 1));
		itemStacks.add(new ItemStack(this, 1, 2));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		blockIcon = register.registerIcon(CCTweaks.RESOURCE_DOMAIN + ":debugTemplate");
		peripheralIcon = register.registerIcon(CCTweaks.RESOURCE_DOMAIN + ":debugPeripheral");
		networkedPeripheralIcon = register.registerIcon(CCTweaks.RESOURCE_DOMAIN + ":debugNetworkedPeripheral");
		nodeIcon = register.registerIcon(CCTweaks.RESOURCE_DOMAIN + ":debugNode");
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		switch (meta) {
			case 0:
				return new TileDebugPeripheral();
			case 1:
				return new TileDebugNetworkedPeripheral();
			case 2:
				return new TileDebugNode();
		}

		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		switch (meta) {
			case 0:
				return peripheralIcon;
			case 1:
				return networkedPeripheralIcon;
			case 2:
				return nodeIcon;
		}

		return null;
	}

	@Override
	public int damageDropped(int damage) {
		return damage;
	}

	@Override
	public String getUnlocalizedName(int meta) {
		switch (meta) {
			case 0:
				return getUnlocalizedName() + ".peripheral";
			case 1:
				return getUnlocalizedName() + ".networkedPeripheral";
			case 2:
				return getUnlocalizedName() + ".node";
		}
		return getUnlocalizedName();
	}

	@Override
	public void preInit() {
		GameRegistry.registerBlock(this, ItemMultiBlock.class, name);
		GameRegistry.registerTileEntity(TileDebugPeripheral.class, "debugPeripheral");
		GameRegistry.registerTileEntity(TileDebugNetworkedPeripheral.class, "debugNetworkedPeripheral");
		GameRegistry.registerTileEntity(TileDebugNode.class, "debugNode");
	}

	@Override
	public boolean canLoad() {
		return Config.Testing.debugItems;
	}
}
