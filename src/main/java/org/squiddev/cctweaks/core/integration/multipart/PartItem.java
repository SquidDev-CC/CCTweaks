package org.squiddev.cctweaks.core.integration.multipart;

import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;
import org.squiddev.cctweaks.core.blocks.BaseBlock;
import org.squiddev.cctweaks.core.integration.multipart.network.SidedNetworkPart;
import org.squiddev.cctweaks.core.integration.multipart.network.WirelessBridgePart;
import org.squiddev.cctweaks.core.items.BaseItem;
import org.squiddev.cctweaks.core.registry.Registry;

import java.util.List;

/**
 * A part that can be used to place multiparts as well
 */
public class PartItem extends BaseItem {
	public PartItem() {
		super("multipart");
		setHasSubtypes(true);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		BlockCoord pos = new BlockCoord(x, y, z);
		double hitDepth = new Vector3(hitX, hitY, hitZ).scalarProject(Rotation.axes[side]) + (side % 2 ^ 1);

		if (hitDepth < 1 && MultipartHelpers.place(world, pos, getPart(stack.getItemDamage(), side))
			|| MultipartHelpers.place(world, pos.offset(side), getPart(stack.getItemDamage(), Facing.oppositeSide[side]))) {
			if (!player.capabilities.isCreativeMode) stack.stackSize--;
			return true;
		}

		return false;
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int damage) {
		return getIcon(damage, 0);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		BaseBlock block = getBlock(stack.getItemDamage());
		return block == null ? super.getItemStackDisplayName(stack) : block.getLocalizedName();
	}

	public static BaseBlock getBlock(int damage) {
		switch (damage) {
			case 0:
				return Registry.blockNetworkedBlock;
			default:
				return null;
		}
	}

	public static SidedNetworkPart getPart(int damage, int side) {
		switch (damage) {
			case 0:
				return new WirelessBridgePart(side);
			default:
				return null;
		}
	}

	public static IIcon getIcon(int damage, int side) {
		switch (damage) {
			case 0:
				return WirelessBridgePart.getRenderer().getBlockIconFromSide(Registry.blockNetworkedBlock, side);
			default:
				return null;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void getSubItems(Item item, CreativeTabs tab, List itemStacks) {
		// Wireless bridge
		itemStacks.add(new ItemStack(this, 1, 0));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
	}

	@Override
	public int getSpriteNumber() {
		return 0;
	}

	@SideOnly(Side.CLIENT)
	public static class Renderer implements IItemRenderer {
		protected static final RenderBlocks renderer = new RenderBlocks();

		@Override
		public boolean handleRenderType(ItemStack item, ItemRenderType type) {
			return true;
		}

		@Override
		public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
			return true;
		}

		@Override
		public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
			int damage = item.getItemDamage();
			BaseBlock block = getBlock(damage);
			if (block == null) return;

			GL11.glPushMatrix();

			// Reverse previous translate
			GL11.glTranslatef(0.5f, 0.5f, 0.5f);

			// Scale so it fills the entire screen
			GL11.glScalef(1.333f, 1.333f, 1.333f);

			// Translate back again
			GL11.glTranslatef(-0.5f, -0.5f, -0.09375f);

			renderer.setRenderBounds(0.125, 0.125, 0.0, 0.875, 0.875, 0.1875);

			Tessellator tessellator = Tessellator.instance;

			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0f, -1.0f, 0.0f);
			renderer.renderFaceYNeg(block, 0.0, 0.0, 0.0, getIcon(damage, 0));
			tessellator.draw();

			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0f, 1.0f, 0.0f);
			renderer.renderFaceYPos(block, 0.0, 0.0, 0.0, getIcon(damage, 1));
			tessellator.draw();

			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0f, 0.0f, -1.0f);
			renderer.renderFaceZNeg(block, 0.0, 0.0, 0.0, getIcon(damage, 2));
			tessellator.draw();

			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0f, 0.0f, 1.0f);
			renderer.renderFaceZPos(block, 0.0, 0.0, 0.0, getIcon(damage, 3));
			tessellator.draw();

			tessellator.startDrawingQuads();
			tessellator.setNormal(-1.0f, 0.0f, 0.0f);
			renderer.renderFaceXNeg(block, 0.0, 0.0, 0.0, getIcon(damage, 4));
			tessellator.draw();

			tessellator.startDrawingQuads();
			tessellator.setNormal(1.0f, 0.0f, 0.0f);
			renderer.renderFaceXPos(block, 0.0, 0.0, 0.0, getIcon(damage, 5));
			tessellator.draw();

			GL11.glPopMatrix();
		}
	}
}
