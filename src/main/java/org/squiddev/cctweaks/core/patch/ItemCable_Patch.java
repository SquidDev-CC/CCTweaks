package org.squiddev.cctweaks.core.patch;

import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TItemMultiPart;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.ItemCable;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.network.NetworkHelpers;
import org.squiddev.cctweaks.core.asm.patch.Visitors;
import org.squiddev.cctweaks.core.integration.multipart.CablePart;
import org.squiddev.cctweaks.core.integration.multipart.ModemPart;

/**
 * Patch class for adding cables into multipart blocks
 */
public class ItemCable_Patch extends ItemCable implements TItemMultiPart {
	public ItemCable_Patch(Block block) {
		super(block);
	}

	@Override
	public double getHitDepth(Vector3 hit, int side) {
		return hit.copy().scalarProject(Rotation.axes[side]) + (side % 2 ^ 1);
	}

	@Override
	public TMultiPart newPart(ItemStack stack, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 hit) {
		switch (getPeripheralType(stack)) {
			case Cable:
				return MultiPartRegistry.createPart(CablePart.NAME, false);
			case WiredModem:
				return new ModemPart(Facing.oppositeSide[side]);
		}

		return null;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		Block block = world.getBlock(x, y, z);

		// We can always place in an air block or a cable block that doesn't have a modem already.
		if (block.isAir(world, x, y, z) && nativePlace(stack, player, world, x, y, z, side, hitX, hitY, hitZ)) {
			// Fire a network invalidate event on placement as the block is air, so the event isn't fired
			if (!world.isRemote) NetworkHelpers.fireNetworkInvalidate(world, x, y, z);

			return true;
		}
		if (block == ComputerCraft.Blocks.cable) {
			TileCable cable = (TileCable) world.getTileEntity(x, y, z);
			PeripheralType type = cable.getPeripheralType();
			PeripheralType stackType = getPeripheralType(stack);
			if ((type == PeripheralType.Cable && stackType == PeripheralType.WiredModem)
				&& nativePlace(stack, player, world, x, y, z, side, hitX, hitY, hitZ)) {
				return true;
			} else if ((type == PeripheralType.WiredModem && stackType == PeripheralType.Cable)
				&& nativePlace(stack, player, world, x, y, z, side, hitX, hitY, hitZ)) {
				return true;
			}
		}

		BlockCoord pos = new BlockCoord(x, y, z);
		Vector3 hit = new Vector3(hitX, hitY, hitZ);
		double d = getHitDepth(hit, side);

		if (
			(d < 1 && place(stack, player, world, pos, side, hit))
				|| nativePlace(stack, player, world, x, y, z, side, hitX, hitY, hitZ)
				|| place(stack, player, world, pos.offset(side), side, hit)
			) {

			// We can't tell if nativePlace placed it in this block or the adjacent block
			// so just invalidate everything
			if (!world.isRemote) NetworkHelpers.fireNetworkInvalidateAdjacent(world, x, y, z);

			return true;
		}

		return false;
	}

	/**
	 * The original {@link ItemCable#onItemUse(ItemStack, EntityPlayer, World, int, int, int, int, float, float, float)} method
	 * We keep this to ensure cables can be placed in existing modem blocks
	 *
	 * @return Success at placing the cable
	 */
	@Visitors.Stub
	@Visitors.Name("onItemUse")
	public boolean nativePlace(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		return false;
	}

	/**
	 * Place a multipart into the world
	 *
	 * @return Success at placing the part
	 */
	public boolean place(ItemStack item, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 hit) {
		TMultiPart part = newPart(item, player, world, pos, side, hit);

		if (part == null || !TileMultipart.canPlacePart(world, pos, part)) return false;
		if (!world.isRemote) TileMultipart.addPart(world, pos, part);
		if (!player.capabilities.isCreativeMode) item.stackSize--;

		return true;
	}
}
