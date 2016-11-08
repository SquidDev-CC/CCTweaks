package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.ItemCable;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.MultipartHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.squiddev.cctweaks.integration.multipart.network.PartCable;
import org.squiddev.cctweaks.integration.multipart.network.PartModem;
import org.squiddev.patcher.visitors.MergeVisitor;

public class ItemCable_Patch extends ItemCable {
	public ItemCable_Patch(Block block) {
		super(block);
	}

	public IMultipart newPart(ItemStack stack, EnumFacing facing) {
		switch (getPeripheralType(stack)) {
			case Cable:
				return new PartCable();
			case WiredModem:
				return new PartModem(facing.getOpposite());
		}

		return null;
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		// We can always place in an air block or a cable block that doesn't have a modem already.
		if (block.isAir(state, world, pos) && nativePlace(stack, player, world, pos, hand, side, hitX, hitY, hitZ) == EnumActionResult.SUCCESS) {
			return EnumActionResult.SUCCESS;
		}
		if (block == ComputerCraft.Blocks.cable) {
			TileCable cable = (TileCable) world.getTileEntity(pos);
			PeripheralType type = cable.getPeripheralType();
			PeripheralType stackType = getPeripheralType(stack);
			if ((type == PeripheralType.Cable && stackType == PeripheralType.WiredModem)
				&& nativePlace(stack, player, world, pos, hand, side, hitX, hitY, hitZ) == EnumActionResult.SUCCESS) {
				return EnumActionResult.SUCCESS;
			} else if ((type == PeripheralType.WiredModem && stackType == PeripheralType.Cable)
				&& nativePlace(stack, player, world, pos, hand, side, hitX, hitY, hitZ) == EnumActionResult.SUCCESS) {
				return EnumActionResult.SUCCESS;
			}
		}

		Vec3d hit = new Vec3d(hitX, hitY, hitZ);
		double depth = (hit.xCoord * 2.0D - 1.0D) * (double) side.getFrontOffsetX() + (hit.yCoord * 2.0D - 1.0D) * (double) side.getFrontOffsetY() + (hit.zCoord * 2.0D - 1.0D) * (double) side.getFrontOffsetZ();
		return (depth < 1.0D && place(stack, player, world, pos, side))
			|| nativePlace(stack, player, world, pos, hand, side, hitX, hitY, hitZ) == EnumActionResult.SUCCESS
			|| place(stack, player, world, pos.offset(side), side) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
	}

	/**
	 * The original {@link ItemCable#onItemUse(ItemStack, EntityPlayer, World, BlockPos, EnumHand, EnumFacing, float, float, float)} method
	 * We keep this to ensure cables can be placed in existing modem blocks
	 *
	 * @return Success at placing the cable
	 */
	@MergeVisitor.Stub
	@MergeVisitor.Rename(from = {"onItemUse", "func_180614_a"})
	public EnumActionResult nativePlace(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		return EnumActionResult.SUCCESS;
	}

	/**
	 * Place a multipart into the world
	 *
	 * @return Success at placing the part
	 */
	public boolean place(ItemStack item, EntityPlayer player, World world, BlockPos pos, EnumFacing side) {
		IMultipart part = newPart(item, side);

		if (part == null || !MultipartHelper.canAddPart(world, pos, part)) return false;
		if (!world.isRemote) MultipartHelper.addPart(world, pos, part);
		if (!player.capabilities.isCreativeMode) item.stackSize--;

		return true;
	}
}
