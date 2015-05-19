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
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.IDataCard;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.cctweaks.blocks.network.BlockNetworked;
import org.squiddev.cctweaks.blocks.network.TileNetworkedWirelessBridge;
import org.squiddev.cctweaks.core.network.bridge.NetworkBinding;
import org.squiddev.cctweaks.core.registry.Registry;
import org.squiddev.cctweaks.integration.multipart.MultipartIntegration;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * A multipart equivalent of {@link TileNetworkedWirelessBridge}
 */
public class PartWirelessBridge extends PartSidedNetwork implements IWorldNetworkNode {
	public static final String NAME = CCTweaks.NAME + ":wirelessBridge";

	@SideOnly(Side.CLIENT)
	public static BridgeRenderer renderBlocks;

	protected final NetworkBinding binding = new NetworkBinding(this);
	private INetworkController networkController;

	public PartWirelessBridge(int direction) {
		this.direction = (byte) direction;
	}

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
		binding.remove();
	}

	@Override
	public void onWorldJoin() {
		super.onWorldJoin();
		binding.add();
	}

	@Override
	public void save(NBTTagCompound tag) {
		super.save(tag);
		binding.save(tag);
	}

	@Override
	public void load(NBTTagCompound tag) {
		super.load(tag);
		binding.load(tag);
	}

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack item) {
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
	public IWorldPosition getPosition() {
		return this;
	}

	@Override
	public boolean canConnect(ForgeDirection direction) {
		return true;
	}

	@Override
	public Map<String, IPeripheral> getConnectedPeripherals() {
		return Collections.emptyMap();
	}

	@Override
	public void receivePacket(INetworkController network, Packet packet, double distanceTravelled) {
	}

	@Override
	public void networkInvalidated(Map<String, IPeripheral> oldPeripherals) {
	}

	@Override
	public Set<INetworkNode> getConnectedNodes() {
		// TODO: Change bind getter
		return binding.getPositions();
	}

	@Override
	public void detachFromNetwork() {
		networkController = null;
	}

	@Override
	public void attachToNetwork(INetworkController networkController) {
		this.networkController = networkController;
	}

	@Override
	public INetworkController getAttachedNetwork() {
		return networkController;
	}

	@Override
	public IWorldNetworkNode getNode() {
		return this;
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
