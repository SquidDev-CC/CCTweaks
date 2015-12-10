package org.squiddev.cctweaks.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.core.registry.IClientModule;
import org.squiddev.cctweaks.core.utils.Helpers;

/**
 * Base class for all blocks
 */
public abstract class BlockBase<T extends TileBase> extends BlockContainer implements IClientModule {
	public final String name;
	public final Class<T> klass;

	public BlockBase(String blockName, Material material, Class<T> klass) {
		super(material);

		this.klass = klass;
		name = blockName;

		setHardness(2);
		setUnlocalizedName(CCTweaks.RESOURCE_DOMAIN + "." + blockName);
		setCreativeTab(CCTweaks.getCreativeTab());
	}

	public BlockBase(String name, Class<T> klass) {
		this(name, Material.rock, klass);
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

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		T tile = getTile(world, pos);

		super.breakBlock(world, pos, state);

		if (tile != null) tile.destroy();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileBase tile = getTile(world, pos);
		return tile != null && tile.onActivated(player, side);
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
		super.onNeighborBlockChange(world, pos, state, neighborBlock);

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
	public boolean canLoad() {
		return true;
	}

	@Override
	public void preInit() {
		GameRegistry.registerBlock(this, name);
		GameRegistry.registerTileEntity(klass, name);
	}

	@Override
	public void init() {
	}

	@Override
	public void postInit() {
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientInit() {
		Helpers.setupModel(Item.getItemFromBlock(this), 0, name);
	}
}
