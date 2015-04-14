package org.squiddev.cctweaks.core.patch;

import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TItemMultiPart;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import dan200.computercraft.shared.peripheral.common.ItemCable;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.squiddev.cctweaks.core.asm.patch.Visitors;
import org.squiddev.cctweaks.core.integration.multipart.CablePart;

/**
 * Patch class for adding cables into multipart blocks
 */
public class ItemCable_Patch extends ItemCable implements TItemMultiPart {
	public ItemCable_Patch(Block block) {
		super(block);
	}

	@Override
	public double getHitDepth(Vector3 vhit, int side) {
		return vhit.copy().scalarProject(Rotation.axes[side]) + (side % 2 ^ 1);
	}

	@Override
	public TMultiPart newPart(ItemStack itemStack, EntityPlayer entityPlayer, World world, BlockCoord blockCoord, int i, Vector3 vector3) {
		return MultiPartRegistry.createPart(CablePart.NAME, false);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		BlockCoord pos = new BlockCoord(x, y, z);
		Vector3 vhit = new Vector3(hitX, hitY, hitZ);
		double d = getHitDepth(vhit, side);

		if (d < 1 && place(stack, player, world, pos, side, vhit)) return true;

		return place(stack, player, world, pos.offset(side), side, vhit) || nativePlace(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
	}

	/**
	 * The original {@link ItemCable#onItemUse(ItemStack, EntityPlayer, World, int, int, int, int, float, float, float)} method
	 * We keep this to ensure cables can be placed in existing modem blocks
	 *
	 * @return Sucess at placing the cable
	 */
	@Visitors.Stub
	@Visitors.Name("onItemUse")
	public boolean nativePlace(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		return false;
	}

	/**
	 * Place a multipart into the world
	 *
	 * @return Sucess at placing the part
	 */
	public boolean place(ItemStack item, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 vhit) {
		TMultiPart part = newPart(item, player, world, pos, side, vhit);

		if (part == null || !TileMultipart.canPlacePart(world, pos, part)) return false;
		if (!world.isRemote) TileMultipart.addPart(world, pos, part);
		if (!player.capabilities.isCreativeMode) item.stackSize--;
		return true;
	}
}
