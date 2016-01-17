package org.squiddev.cctweaks.turtle;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.core.registry.Module;

import javax.vecmath.Matrix4f;

public abstract class TurtleUpgradeBase extends Module implements ITurtleUpgrade {
	@SideOnly(Side.CLIENT)
	private static ItemModelMesher mesher;

	protected final String name;
	private final int id;
	private final ResourceLocation location;
	private final ItemStack stack;

	@SideOnly(Side.CLIENT)
	@SuppressWarnings("deprecation")
	private IBakedModel model;

	public TurtleUpgradeBase(String name, int id, Item item) {
		this(name, id, new ItemStack(item, 1, 0));
	}

	public TurtleUpgradeBase(String name, int id, ItemStack stack) {
		this.name = name;
		this.id = id;
		this.stack = stack;

		location = new ResourceLocation(CCTweaks.RESOURCE_DOMAIN, name);
	}

	@Override
	public ResourceLocation getUpgradeID() {
		return location;
	}

	@Override
	public int getLegacyUpgradeID() {
		return id;
	}

	@Override
	public String getUnlocalisedAdjective() {
		return "turtle." + CCTweaks.RESOURCE_DOMAIN + "." + name + ".adjective";
	}


	@Override
	public ItemStack getCraftingItem() {
		return stack;
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings("deprecation")
	public Pair<IBakedModel, Matrix4f> getModel(ITurtleAccess access, TurtleSide side) {
		float xOffset = side == TurtleSide.Left ? -0.40625F : 0.40625F;
		Matrix4f transform = new Matrix4f(0.0F, 0.0F, -1.0F, 1.0F + xOffset, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F);

		if (model == null) model = getMesher().getItemModel(stack);
		return Pair.of(model, transform);
	}

	@Override
	public void update(ITurtleAccess iTurtleAccess, TurtleSide turtleSide) {
	}

	@Override
	public IPeripheral createPeripheral(ITurtleAccess iTurtleAccess, TurtleSide turtleSide) {
		return null;
	}

	@Override
	public TurtleCommandResult useTool(ITurtleAccess iTurtleAccess, TurtleSide turtleSide, TurtleVerb turtleVerb, EnumFacing enumFacing) {
		return null;
	}

	@SideOnly(Side.CLIENT)
	public static ItemModelMesher getMesher() {
		ItemModelMesher instance = mesher;
		if (instance == null) instance = mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		return instance;
	}
}
