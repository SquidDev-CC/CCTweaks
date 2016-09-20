package org.squiddev.cctweaks.integration.multipart.network;

import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.client.render.FixedRenderBlocks;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.IDataCard;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHost;
import org.squiddev.cctweaks.blocks.network.BlockNetworked;
import org.squiddev.cctweaks.blocks.network.TileNetworkedWirelessBridge;
import org.squiddev.cctweaks.core.FmlEvents;
import org.squiddev.cctweaks.core.network.NetworkHelpers;
import org.squiddev.cctweaks.core.network.bridge.NetworkBinding;
import org.squiddev.cctweaks.core.network.bridge.NetworkBindingWithModem;
import org.squiddev.cctweaks.core.registry.Registry;
import org.squiddev.cctweaks.integration.multipart.MultipartIntegration;

import java.util.Arrays;

/**
 * A multipart equivalent of {@link TileNetworkedWirelessBridge}
 */
public class PartWirelessBridge extends PartSidedNetwork implements IPeripheralHost {
	public static final String NAME = CCTweaks.NAME + ":wirelessBridge";

	@SideOnly(Side.CLIENT)
	public static BridgeRenderer renderBlocks;

	protected final NetworkBindingWithModem binding = new NetworkBindingWithModem(this) {
		private boolean dirty = false;

		@Override
		public void markDirty() {
			if (!dirty) {
				FmlEvents.schedule(new Runnable() {
					@Override
					public void run() {
						dirty = false;
						tile().markDirty();
					}
				});
				dirty = true;
			}
		}
	};

	public PartWirelessBridge(int direction) {
		this.direction = (byte) direction;
	}

	@Override
	public String getType() {
		return NAME;
	}

	@Override
	public IIcon getBrokenIcon(int i) {
		return Registry.blockNetworked.getIcon(0, 0);
	}

	@Override
	public void onWorldSeparate() {
		super.onWorldSeparate();
		if (world() == null || !world().isRemote) binding.destroy();
	}

	@Override
	public void loadLazy(NBTTagCompound tag) {
		binding.load(tag);
	}

	@Override
	public Iterable<String> getFields() {
		return Arrays.asList(NetworkBinding.LSB, NetworkBinding.MSB, NetworkBinding.ID);
	}

	@Override
	public void onWorldJoin() {
		super.onWorldJoin();
		NetworkHelpers.scheduleConnect(binding, this);
	}

	@Override
	public void save(NBTTagCompound tag) {
		binding.save(tag);
		super.save(tag);
	}

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack item) {
		if (world().isRemote) return true;

		ItemStack stack = player.getHeldItem();
		if (stack != null && stack.getItem() instanceof IDataCard) {
			IDataCard card = (IDataCard) stack.getItem();

			if (player.isSneaking()) {
				binding.save(stack, card);
				tile().markDirty();
				card.notifyPlayer(player, IDataCard.Messages.Stored);
				return true;
			} else if (binding.load(stack, card)) {
				tile().markDirty();
				card.notifyPlayer(player, IDataCard.Messages.Loaded);
				return true;
			}
		}

		return false;
	}

	@Override
	public ItemStack getItem() {
		return new ItemStack(MultipartIntegration.itemPart, 1, 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderStatic(Vector3 pos, int pass) {
		if (pass == 0) {
			TextureUtils.bindAtlas(0);

			BridgeRenderer render = getRenderer();
			Cuboid6 bounds = getBounds();
			render.renderAllFaces = true;
			render.setRenderBounds(bounds.min.x, bounds.min.y, bounds.min.z, bounds.max.x, bounds.max.y, bounds.max.z);
			render.setWorld(world());
			render.renderStandardBlock(Registry.blockNetworked, x(), y(), z());
			return true;
		}

		return false;
	}

	@SideOnly(Side.CLIENT)
	public static BridgeRenderer getRenderer() {
		BridgeRenderer render = renderBlocks;
		if (render == null) render = renderBlocks = new BridgeRenderer();
		return render;
	}

	@Override
	public IWorldNetworkNode getNode() {
		return binding;
	}

	@Override
	public IPeripheral getPeripheral(int side) {
		return binding.getModem().modem;
	}

	@SideOnly(Side.CLIENT)
	public static class BridgeRenderer extends FixedRenderBlocks {
		@Override
		public IIcon getBlockIcon(Block block, IBlockAccess world, int x, int y, int z, int side) {
			return getBlockIconFromSide(block, side);
		}

		@Override
		public IIcon getBlockIconFromSide(Block block, int side) {
			return getIconSafe(BlockNetworked.bridgeSmallIcon);
		}

		@Override
		public void setRenderBoundsFromBlock(Block block) {
		}
	}
}
