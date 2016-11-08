package org.squiddev.cctweaks.blocks.debug;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.squiddev.cctweaks.blocks.BlockBase;
import org.squiddev.cctweaks.blocks.IMultiBlock;
import org.squiddev.cctweaks.blocks.TileBase;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.utils.Helpers;
import org.squiddev.cctweaks.items.ItemMultiBlock;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;

/**
 * The base debug block that provides debug TileEntities
 */
public class BlockDebug extends BlockBase<TileBase> implements IMultiBlock {
	public enum BlockDebugType implements IStringSerializable {
		PERIPHERAL,
		NETWORKED_PERIPHERAL,
		NODE;

		private final String name;
		private static final HashMap<String, BlockDebugType> NAME_LOOKUP = new HashMap<String, BlockDebugType>();
		private static final BlockDebugType[] VALUES = values();

		BlockDebugType() {
			name = name().toLowerCase();
		}

		@Override
		@Nonnull
		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}

		public static BlockDebugType byName(String name) {
			return NAME_LOOKUP.get(name);
		}

		static {
			for (BlockDebugType e : VALUES) {
				NAME_LOOKUP.put(e.getName(), e);
			}
		}
	}

	public static final PropertyEnum<BlockDebugType> TYPE = PropertyEnum.create("type", BlockDebugType.class);

	public BlockDebug() {
		super("debugBlock", TileBase.class);
		setDefaultState(getBlockState().getBaseState().withProperty(TYPE, BlockDebugType.PERIPHERAL));
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> itemStacks) {
		for (BlockDebugType type : BlockDebugType.VALUES) {
			itemStacks.add(new ItemStack(this, 1, type.ordinal()));
		}
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
	@SuppressWarnings("deprecation")
	public IBlockState getStateFromMeta(int meta) {
		IBlockState state = super.getStateFromMeta(meta);
		return state.withProperty(TYPE, BlockDebugType.VALUES[meta < 0 || meta >= BlockDebugType.VALUES.length ? 0 : meta]);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TYPE).ordinal();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, TYPE);
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
		register(new ItemMultiBlock(this));
		GameRegistry.registerTileEntity(TileDebugPeripheral.class, "debugPeripheral");
		GameRegistry.registerTileEntity(TileDebugNetworkedPeripheral.class, "debugNetworkedPeripheral");
		GameRegistry.registerTileEntity(TileDebugNode.class, "debugNode");
	}

	@Override
	public boolean canLoad() {
		return Config.Testing.debugItems;
	}

	@Override
	public void clientInit() {
		for (BlockDebugType type : BlockDebugType.VALUES) {
			Helpers.setupModel(Item.getItemFromBlock(this), type.ordinal(), "debug_" + type.getName());
		}
	}
}
