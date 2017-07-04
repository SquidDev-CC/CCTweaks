package org.squiddev.cctweaks.blocks.network;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.blocks.BlockBase;
import org.squiddev.cctweaks.blocks.IMultiBlock;
import org.squiddev.cctweaks.blocks.TileBase;
import org.squiddev.cctweaks.core.network.modem.BasicModem;
import org.squiddev.cctweaks.core.utils.Helpers;
import org.squiddev.cctweaks.items.ItemMultiBlock;

import javax.annotation.Nonnull;
import java.util.HashMap;

/**
 * A bridge between two networks so they can communicate with each other
 */
public class BlockNetworked extends BlockBase<TileBase> implements IMultiBlock {
	private enum BlockNetworkedType implements IStringSerializable {
		WIRELESS_BRIDGE,
		MODEM;

		private final String name;
		private static final HashMap<String, BlockNetworkedType> NAME_LOOKUP = new HashMap<String, BlockNetworkedType>();
		private static final BlockNetworkedType[] VALUES = values();

		BlockNetworkedType() {
			name = name().toLowerCase();
		}

		@Nonnull
		@Override
		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}

		public static BlockNetworkedType byName(String name) {
			return NAME_LOOKUP.get(name);
		}

		static {
			for (BlockNetworkedType e : VALUES) {
				NAME_LOOKUP.put(e.getName(), e);
			}
		}
	}

	private static final PropertyEnum<BlockNetworkedType> TYPE = PropertyEnum.create("type", BlockNetworkedType.class);
	private static final PropertyBool MODEM_ON = PropertyBool.create("modem_on");
	private static final PropertyBool PERIPHERAL_ON = PropertyBool.create("peripheral_on");

	public BlockNetworked() {
		super("networkedBlock", TileBase.class);
		setDefaultState(getBlockState().getBaseState()
			.withProperty(TYPE, BlockNetworkedType.MODEM)
			.withProperty(MODEM_ON, false)
			.withProperty(PERIPHERAL_ON, false)
		);
	}

	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull World world, int meta) {
		switch (meta) {
			case 0:
				return new TileNetworkedWirelessBridge();
			case 1:
				return new TileNetworkedModem();
		}
		return null;
	}

	@Override
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> itemStacks) {
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


	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public IBlockState getStateFromMeta(int meta) {
		IBlockState state = super.getStateFromMeta(meta);
		return state.withProperty(TYPE, BlockNetworkedType.VALUES[meta < 0 || meta >= BlockNetworkedType.VALUES.length ? 0 : meta]);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TYPE).ordinal();
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, TYPE, MODEM_ON, PERIPHERAL_ON);
	}

	@Override
	public void preInit() {
		MinecraftForge.EVENT_BUS.register(this);
		registerTileEntity(TileNetworkedWirelessBridge.class, "wirelessBridge");
		registerTileEntity(TileNetworkedModem.class, "wiredModem");
	}

	@Override
	@Nonnull
	@SuppressWarnings("deprecation")
	public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		state = super.getActualState(state, worldIn, pos);
		if (state.getValue(TYPE) == BlockNetworkedType.MODEM) {
			TileBase te = getTile(worldIn, pos);
			if (te instanceof TileNetworkedModem) {
				int modemState = ((TileNetworkedModem) te).modem.state;
				state = state
					.withProperty(MODEM_ON, (modemState & BasicModem.MODEM_ON) == BasicModem.MODEM_ON)
					.withProperty(PERIPHERAL_ON, (modemState & BasicModem.MODEM_PERIPHERAL) == BasicModem.MODEM_PERIPHERAL);
			}
		}
		return state;
	}

	@Override
	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().register(new ItemMultiBlock(this).setRegistryName(new ResourceLocation(CCTweaks.ID, name)));
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		for (BlockNetworkedType type : BlockNetworkedType.VALUES) {
			Helpers.setupModel(Item.getItemFromBlock(this), type.ordinal(), type.getName());
		}
	}
}
