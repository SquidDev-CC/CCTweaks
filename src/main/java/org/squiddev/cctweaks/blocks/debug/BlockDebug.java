package org.squiddev.cctweaks.blocks.debug;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
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
	public enum BlockDebugType {
		PERIPHERAL,
		NETWORKED_PERIPHERAL,
		NODE,
	}

	public static final PropertyEnum TYPE = PropertyEnum.create("type", BlockDebugType.class);

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
	public IBlockState getStateFromMeta(int meta) {
		IBlockState state = super.getStateFromMeta(meta);

		BlockDebugType[] values = BlockDebugType.values();
		return state.withProperty(TYPE, values[meta >= values.length || meta < 0 ? 0 : meta]);
	}

	@Override
	public int damageDropped(IBlockState state) {
		return ((BlockDebugType) state.getValue(TYPE)).ordinal();
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
