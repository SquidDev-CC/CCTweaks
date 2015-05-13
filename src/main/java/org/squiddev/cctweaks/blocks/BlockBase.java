package org.squiddev.cctweaks.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.core.registry.IModule;

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
		setBlockName(CCTweaks.RESOURCE_DOMAIN + "." + blockName);
		setBlockTextureName(CCTweaks.RESOURCE_DOMAIN + ":" + blockName);
		setCreativeTab(CCTweaks.getCreativeTab());
	}

	public BlockBase(String name, Class<T> klass) {
		this(name, Material.rock, klass);
	}

	@SuppressWarnings("unchecked")
	public T getTile(IBlockAccess world, int x, int y, int z) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile != null && klass.isInstance(tile)) {
			return (T) tile;
		}

		return null;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int damage) {
		T tile = getTile(world, x, y, z);
		if (tile != null) tile.preRemove();

		super.breakBlock(world, x, y, z, block, damage);

		if (tile != null) tile.postRemove();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		TileBase tile = getTile(world, x, y, z);
		return tile != null && tile.onActivated(player, side);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		super.onNeighborBlockChange(world, x, y, z, block);
		if (world.isRemote) return;

		TileBase tile = getTile(world, x, y, z);
		if (tile != null) tile.onNeighborChanged();
	}

	@Override
	public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ) {
		super.onNeighborChange(world, x, y, z, tileX, tileY, tileZ);
		if (world instanceof World && ((World) world).isRemote) return;

		TileBase tile = getTile(world, x, y, z);
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
}
