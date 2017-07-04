package org.squiddev.cctweaks.turtle;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.core.registry.IModule;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;

public abstract class TurtleUpgradeBase implements ITurtleUpgrade, IModule {
	@SideOnly(Side.CLIENT)
	private static ItemModelMesher mesher;

	protected final String name;
	private final int id;
	private final ResourceLocation location;

	@SideOnly(Side.CLIENT)
	@SuppressWarnings("deprecation")
	private IBakedModel model;

	public TurtleUpgradeBase(String name, int id) {
		this.name = name;
		this.id = id;

		location = new ResourceLocation(CCTweaks.ID, name);
	}

	@Nonnull
	@Override
	public ResourceLocation getUpgradeID() {
		return location;
	}

	@Override
	public int getLegacyUpgradeID() {
		return id;
	}

	@Nonnull
	@Override
	public String getUnlocalisedAdjective() {
		return "turtle." + CCTweaks.ID + "." + name + ".adjective";
	}

	@Nonnull
	protected abstract ItemStack getStack();

	@Nonnull
	@Override
	public ItemStack getCraftingItem() {
		return getStack();
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings("deprecation")
	public Pair<net.minecraft.client.renderer.block.model.IBakedModel, Matrix4f> getModel(ITurtleAccess access, @Nonnull TurtleSide side) {
		float xOffset = side == TurtleSide.Left ? -0.40625F : 0.40625F;
		Matrix4f transform = new Matrix4f(0.0F, 0.0F, -1.0F, 1.0F + xOffset, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F);

		if (model == null) model = getMesher().getItemModel(getStack());
		return Pair.of(model, transform);
	}

	@Override
	public void update(@Nonnull ITurtleAccess iTurtleAccess, @Nonnull TurtleSide turtleSide) {
	}

	@Override
	public IPeripheral createPeripheral(@Nonnull ITurtleAccess iTurtleAccess, @Nonnull TurtleSide turtleSide) {
		return null;
	}

	@Nonnull
	@Override
	public TurtleCommandResult useTool(@Nonnull ITurtleAccess iTurtleAccess, @Nonnull TurtleSide turtleSide, @Nonnull TurtleVerb turtleVerb, @Nonnull EnumFacing enumFacing) {
		return TurtleCommandResult.failure();
	}

	@SideOnly(Side.CLIENT)
	public static ItemModelMesher getMesher() {
		ItemModelMesher instance = mesher;
		if (instance == null) instance = mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		return instance;
	}
}
