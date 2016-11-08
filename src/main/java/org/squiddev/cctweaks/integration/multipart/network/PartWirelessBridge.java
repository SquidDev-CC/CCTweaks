package org.squiddev.cctweaks.integration.multipart.network;

import dan200.computercraft.api.peripheral.IPeripheral;
import mcmultipart.multipart.INormallyOccludingPart;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.IDataCard;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNodeHost;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHost;
import org.squiddev.cctweaks.core.McEvents;
import org.squiddev.cctweaks.core.network.NetworkHelpers;
import org.squiddev.cctweaks.core.network.bridge.NetworkBinding;
import org.squiddev.cctweaks.core.network.bridge.NetworkBindingWithModem;
import org.squiddev.cctweaks.core.registry.Registry;
import org.squiddev.cctweaks.integration.multipart.MultipartIntegration;
import org.squiddev.cctweaks.integration.multipart.PartSided;

import java.util.Arrays;
import java.util.List;

public class PartWirelessBridge extends PartSided implements IWorldNetworkNodeHost, IPeripheralHost, INormallyOccludingPart {
	public static final AxisAlignedBB[] BOUNDS = new AxisAlignedBB[]{
		new AxisAlignedBB(0.125, 0.0, 0.125, 0.875, 0.125, 0.875D),
		new AxisAlignedBB(0.125, 0.875, 0.125, 0.875, 1.0, 0.875D),
		new AxisAlignedBB(0.125, 0.125, 0.0, 0.875, 0.875, 0.125D),
		new AxisAlignedBB(0.125, 0.125, 0.875, 0.875, 0.875, 1.0D),
		new AxisAlignedBB(0.0, 0.125, 0.125, 0.125, 0.875, 0.875D),
		new AxisAlignedBB(0.875, 0.125, 0.125, 1.0, 0.875, 0.875D),
	};

	private final NetworkBindingWithModem binding = new NetworkBindingWithModem(this) {
		private boolean dirty = false;

		@Override
		public void markDirty() {
			if (!dirty) {
				McEvents.schedule(new Runnable() {
					@Override
					public void run() {
						dirty = false;
						PartWirelessBridge.this.markDirty();
					}
				});
				dirty = true;
			}
		}
	};

	public PartWirelessBridge() {
	}

	public PartWirelessBridge(EnumFacing facing) {
		super(facing);
	}

	//region Basic Getters
	@Override
	public Block getBlock() {
		return Registry.blockNetworked;
	}

	@Override
	public ItemStack getStack() {
		return new ItemStack(MultipartIntegration.itemPart, 1, 0);
	}

	@Override
	public ResourceLocation getModelPath() {
		return new ResourceLocation(CCTweaks.ID, "wirelessBridgeSmall");
	}

	@Override
	public void addSelectionBoxes(List<AxisAlignedBB> list) {
		list.add(BOUNDS[getSide().ordinal()]);
	}

	@Override
	public void addCollisionBoxes(net.minecraft.util.math.AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
		AxisAlignedBB bounds = BOUNDS[getSide().ordinal()];
		if (bounds.intersectsWith(mask)) list.add(bounds);
	}

	@Override
	public void addOcclusionBoxes(List<AxisAlignedBB> list) {
		list.add(BOUNDS[getSide().ordinal()]);
	}

	@Override
	public IPeripheral getPeripheral(EnumFacing side) {
		return binding.getModem().modem;
	}

	@Override
	public IWorldNetworkNode getNode() {
		return binding;
	}
	//endregion

	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack stack, PartMOP hit) {
		if (getWorld().isRemote) return true;

		if (stack != null && stack.getItem() instanceof IDataCard) {
			IDataCard card = (IDataCard) stack.getItem();

			if (player.isSneaking()) {
				binding.save(stack, card);
				markDirty();
				card.notifyPlayer(player, IDataCard.Messages.Stored);
				return true;
			} else if (binding.load(stack, card)) {
				markDirty();
				card.notifyPlayer(player, IDataCard.Messages.Loaded);
				return true;
			}
		}

		return false;
	}

	@Override
	public void onAdded() {
		super.onAdded();
		if (!getWorld().isRemote) binding.connect();
	}

	@Override
	public void onLoaded() {
		super.onLoaded();
		if (!getWorld().isRemote) NetworkHelpers.scheduleConnect(binding, this);
	}

	@Override
	public void readLazyNBT(NBTTagCompound tag) {
		binding.load(tag);
	}

	@Override
	public Iterable<String> getFields() {
		return Arrays.asList(NetworkBinding.LSB, NetworkBinding.MSB, NetworkBinding.ID);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		binding.save(tag);
		super.writeToNBT(tag);
		return tag;
	}

	@Override
	public void onUnloaded() {
		if (!getWorld().isRemote) binding.destroy();
		super.onUnloaded();
	}

	@Override
	public void onRemoved() {
		if (!getWorld().isRemote) binding.destroy();
		super.onRemoved();
	}
}
