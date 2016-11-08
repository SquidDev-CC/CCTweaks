package org.squiddev.cctweaks.integration.multipart;

import mcmultipart.item.ItemMultiPart;
import mcmultipart.multipart.IMultipart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.integration.multipart.network.PartWirelessBridge;

public class ItemCustomPart extends ItemMultiPart {
	public ItemCustomPart() {
		setCreativeTab(CCTweaks.getCreativeTab());
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		switch (stack.getItemDamage()) {
			case 0:
				return "tile." + CCTweaks.ID + ".networkedBlock.wirelessBridge";
		}
		return super.getUnlocalizedName(stack);
	}

	@Override
	public IMultipart createPart(World world, BlockPos pos, EnumFacing side, Vec3d vec3, ItemStack stack, EntityPlayer player) {
		switch (stack.getItemDamage()) {
			case 0:
				return new PartWirelessBridge(side);
		}
		return null;
	}
}
