package org.squiddev.cctweaks.integration.multipart.network;

import dan200.computercraft.api.peripheral.IPeripheral;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import org.squiddev.cctweaks.api.IDataCard;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNodeHost;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHost;
import org.squiddev.cctweaks.core.network.bridge.NetworkBindingWithModem;
import org.squiddev.cctweaks.core.registry.Registry;
import org.squiddev.cctweaks.integration.multipart.PartSided;

import java.util.List;

public class PartWirelessBridge extends PartSided implements IWorldNetworkNodeHost, IPeripheralHost {
	public static final AxisAlignedBB[] BOUNDS = new AxisAlignedBB[]{
		new AxisAlignedBB(0.125, 0.0, 0.125, 0.875, 0.1875, 0.875),
		new AxisAlignedBB(0.125, 0.8125, 0.125, 0.875, 1.0, 0.875),
		new AxisAlignedBB(0.125, 0.125, 0.0, 0.875, 0.875, 0.1875),
		new AxisAlignedBB(0.125, 0.125, 0.8125, 0.875, 0.875, 1.0),
		new AxisAlignedBB(0.0, 0.125, 0.125, 0.1875, 0.875, 0.875),
		new AxisAlignedBB(0.8125, 0.125, 0.125, 1.0, 0.875, 0.875),
	};

	private final NetworkBindingWithModem binding = new NetworkBindingWithModem(this);

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
		return new ItemStack(Registry.blockNetworked, 1, 0);
	}

	@Override
	public String getModelPath() {
		return "cctweaks:wirelessBridgeSmall";
	}

	@Override
	public void addSelectionBoxes(List<AxisAlignedBB> list) {
		list.add(BOUNDS[getSide().ordinal()]);
	}

	@Override
	public void addCollisionBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
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
	public boolean onActivated(EntityPlayer player, ItemStack stack, PartMOP hit) {
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
		if (!getWorld().isRemote) binding.connect();
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		binding.save(tag);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		binding.load(tag);
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
