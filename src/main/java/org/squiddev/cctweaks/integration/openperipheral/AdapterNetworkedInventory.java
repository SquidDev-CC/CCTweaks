package org.squiddev.cctweaks.integration.openperipheral;

import com.google.common.base.Preconditions;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.common.util.ForgeDirection;
import openperipheral.api.adapter.Asynchronous;
import openperipheral.api.adapter.IPeripheralAdapter;
import openperipheral.api.adapter.method.*;
import org.squiddev.cctweaks.api.CCTweaksAPI;
import org.squiddev.cctweaks.api.network.INetworkAccess;
import org.squiddev.cctweaks.api.peripheral.IPeripheralEnvironments;
import org.squiddev.cctweaks.core.utils.InventoryUtils;

/**
 * An adapter that allows you to push to a networked peripheral instead
 */
@Asynchronous
public class AdapterNetworkedInventory implements IPeripheralAdapter {
	private static final int ANY_SLOT = -1;

	@Override
	public Class<?> getTargetClass() {
		return IInventory.class;
	}

	@Override
	public String getSourceId() {
		return "inventory-world-networked";
	}

	private static void checkSlotId(IInventory inventory, int slot, String name) {
		if (slot != ANY_SLOT) Preconditions.checkElementIndex(slot, inventory.getSizeInventory(), name + " slot id");
	}

	public static IInventory getInventory(INetworkAccess network, String name) {
		Object object = CCTweaksAPI.instance().peripheralHelpers().getTarget(network.getPeripheralsOnNetwork().get(name));
		if (object != null && object instanceof IInventory) return InventoryUtils.getInventory((IInventory) object);
		return null;
	}

	@Alias("pullItemIntoSlotRemote")
	@ScriptCallable(returnTypes = ReturnType.NUMBER, description = "Pull an item from a slot in another inventory into a slot in this one. Returns the amount of items moved")
	public int pullItemRemote(
		IInventory target, @Env(IPeripheralEnvironments.ARG_NETWORK) INetworkAccess network,
		@Arg(name = "remoteName", description = "The name of the remote inventory") String remote,
		@Arg(name = "slot", description = "The slot in the OTHER inventory that you're pulling from") int fromSlot,
		@Optionals @Arg(name = "maxAmount", description = "The maximum amount of items you want to pull") Integer maxAmount,
		@Arg(name = "direction", description = "The direction to pull from the OTHER inventory") ForgeDirection fromDirection,
		@Arg(name = "intoSlot", description = "The slot in the current inventory that you want to pull into") Integer intoSlot,
		@Arg(name = "direction", description = "The direction to push into the current inventory") ForgeDirection intoDirection
	) {
		Preconditions.checkNotNull(network, "Cannot find the network");

		final IInventory otherInventory = Preconditions.checkNotNull(getInventory(network, remote), "Other inventory not found");
		final IInventory thisInventory = Preconditions.checkNotNull(InventoryUtils.getInventory(target), "Inventory not found");

		if (otherInventory == target) return 0;

		if (maxAmount == null) maxAmount = 64;
		if (intoSlot == null) intoSlot = 0;
		if (fromDirection == null) fromDirection = ForgeDirection.UNKNOWN;
		if (intoDirection == null) intoDirection = ForgeDirection.UNKNOWN;

		fromSlot -= 1;
		intoSlot -= 1;

		checkSlotId(otherInventory, fromSlot, "input");
		checkSlotId(thisInventory, intoSlot, "output");

		final int amount = InventoryUtils.moveItemInto(otherInventory, fromSlot, fromDirection, thisInventory, intoSlot, intoDirection, maxAmount);
		if (amount > 0) {
			thisInventory.markDirty();
			otherInventory.markDirty();
		}

		return amount;
	}

	@Alias("pushItemIntoSlotRemote")
	@ScriptCallable(returnTypes = ReturnType.NUMBER, description = "Push an item from the current inventory into slot on the other one. Returns the amount of items moved")
	public int pushItemRemote(
		IInventory target, @Env(IPeripheralEnvironments.ARG_NETWORK) INetworkAccess network,
		@Arg(name = "remoteName", description = "The name of the remote inventory") String remote,
		@Arg(name = "slot", description = "The slot in the current inventory that you're pushing from") int fromSlot,
		@Optionals @Arg(name = "maxAmount", description = "The maximum amount of items you want to push") Integer maxAmount,
		@Arg(name = "direction", description = "The direction to push into the current inventory") ForgeDirection fromDirection,
		@Arg(name = "intoSlot", description = "The slot in the other inventory that you want to push into") Integer intoSlot,
		@Arg(name = "slot", description = "The slot in the other inventory that you're pulling from") ForgeDirection intoDirection
	) {
		Preconditions.checkNotNull(network, "Cannot find the network");

		final IInventory otherInventory = Preconditions.checkNotNull(getInventory(network, remote), "Other inventory not found");
		final IInventory thisInventory = Preconditions.checkNotNull(InventoryUtils.getInventory(target), "Inventory not found");

		if (otherInventory == target) return 0;

		if (maxAmount == null) maxAmount = 64;
		if (intoSlot == null) intoSlot = 0;
		if (fromDirection == null) fromDirection = ForgeDirection.UNKNOWN;
		if (intoDirection == null) intoDirection = ForgeDirection.UNKNOWN;

		fromSlot -= 1;
		intoSlot -= 1;

		checkSlotId(thisInventory, fromSlot, "input");
		checkSlotId(otherInventory, intoSlot, "output");

		int amount = InventoryUtils.moveItemInto(thisInventory, fromSlot, fromDirection, otherInventory, intoSlot, intoDirection, maxAmount);
		if (amount > 0) {
			thisInventory.markDirty();
			otherInventory.markDirty();
		}

		return amount;
	}
}
