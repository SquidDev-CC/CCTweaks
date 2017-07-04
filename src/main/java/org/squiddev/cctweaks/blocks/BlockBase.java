package org.squiddev.cctweaks.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.core.registry.IModule;
import org.squiddev.cctweaks.core.utils.Helpers;

import javax.annotation.Nonnull;

/**
 * Base class for all blocks
 */
public abstract class BlockBase<T extends TileBase> extends BlockContainer implements IModule {
	public final String name;
	public final Class<T> klass;

	public BlockBase(String blockName, Material material, Class<T> klass) {
		super(material);

		this.klass = klass;
		name = blockName;

		setHardness(2);
		setUnlocalizedName(CCTweaks.ID + "." + blockName);
		setCreativeTab(CCTweaks.getCreativeTab());
	}

	public BlockBase(String name, Class<T> klass) {
		this(name, Material.ROCK, klass);
	}

	@SuppressWarnings("unchecked")
	public T getTile(IBlockAccess world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile != null && klass.isInstance(tile)) {
			return (T) tile;
		}

		return null;
	}

	@Override
	public int damageDropped(IBlockState state) {
		return getMetaFromState(state);
	}

	@Nonnull
	@Override
	public EnumBlockRenderType getRenderType(IBlockState p_getRenderType_1_) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public void breakBlock(World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		T tile = getTile(world, pos);

		super.breakBlock(world, pos, state);

		if (tile != null) tile.destroy();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileBase tile = getTile(world, pos);
		return tile != null && tile.onActivated(player, side, hand);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		super.neighborChanged(state, world, pos, blockIn, fromPos);

		if (world.isRemote) return;

		TileBase tile = getTile(world, pos);
		if (tile != null) tile.onNeighborChanged();
	}

	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		super.onNeighborChange(world, pos, neighbor);

		if (world instanceof World && ((World) world).isRemote) return;

		TileBase tile = getTile(world, pos);
		if (tile != null) tile.onNeighborChanged();
	}

	@Override
	public void preInit() {
		MinecraftForge.EVENT_BUS.register(this);
		registerTileEntity(klass, name);
	}

	protected static void registerTileEntity(Class<? extends TileEntity> klass, String name) {
		GameRegistry.registerTileEntity(klass, CCTweaks.ID + ":" + name);
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().register(new ItemBlock(this).setRegistryName(new ResourceLocation(CCTweaks.ID, name)));
	}

	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event) {
		event.getRegistry().register(this.setRegistryName(new ResourceLocation(CCTweaks.ID, name)));
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		Helpers.setupModel(Item.getItemFromBlock(this), 0, name);
	}
}
