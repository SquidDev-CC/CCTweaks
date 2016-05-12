package org.squiddev.cctweaks.core.pocket;

import com.google.common.base.Preconditions;
import dan200.computercraft.shared.util.InventoryUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.cctweaks.api.pocket.IPocketRegistry;
import org.squiddev.cctweaks.api.pocket.IPocketUpgrade;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.util.HashMap;
import java.util.Map;

public final class PocketRegistry implements IPocketRegistry {
	public static final short FLAG = 0xFF;

	public static final PocketRegistry instance = new PocketRegistry();

	protected final Map<Integer, IPocketUpgrade> legacyUpgrades = new HashMap<Integer, IPocketUpgrade>();
	protected final Map<String, IPocketUpgrade> upgrades = new HashMap<String, IPocketUpgrade>();

	@Override
	public void addUpgrade(IPocketUpgrade upgrade) {
		Preconditions.checkNotNull(upgrade, "upgrade cannot be null");

		String location = upgrade.getUpgradeID().toString();
		if (upgrades.containsKey(location)) throw new IllegalArgumentException(location + " is already registered");

		upgrades.put(location, upgrade);
	}

	@Override
	public void addLegacyUpgrade(int id, IPocketUpgrade upgrade) {
		Preconditions.checkNotNull(upgrade, "upgrade cannot be null");

		if (legacyUpgrades.containsKey(id)) {
			throw new IllegalArgumentException("Legacy upgrade with id " + id + " is already registered");
		}

		legacyUpgrades.put(id, upgrade);
	}

	public IPocketUpgrade getUpgrade(ItemStack stack, IInventory inventory) {
		if (!stack.hasTagCompound()) return null;

		NBTTagCompound tag = stack.getTagCompound();
		if (!tag.hasKey("upgrade")) return null;

		int id = tag.getShort("upgrade");
		if (id == 0 || id == 1) return null;

		return getUpgradeInternal(tag, id, inventory);
	}

	public String getUpgradeAdjective(ItemStack stack, IInventory inventory) {
		if (!stack.hasTagCompound()) return null;

		NBTTagCompound tag = stack.getTagCompound();
		if (!tag.hasKey("upgrade")) return null;

		int id = tag.getShort("upgrade");
		if (id == 0) return null;
		if (id == 1) return "upgrade.computercraft:wireless_modem.adjective";

		IPocketUpgrade upgrade = getUpgradeInternal(tag, id, inventory);
		return upgrade == null ? null : upgrade.getUnlocalisedAdjective();
	}

	private IPocketUpgrade getUpgradeInternal(NBTTagCompound tag, int id, IInventory inventory) {
		IPocketUpgrade upgrade;
		if (id == FLAG) {
			upgrade = upgrades.get(tag.getString("upgrade_name"));
		} else {
			upgrade = legacyUpgrades.get(id);
			if (upgrade != null) {
				tag.setInteger("upgrade", FLAG);
				tag.setString("upgrade_name", upgrade.getUpgradeID().toString());
			}
		}

		if (upgrade == null) {
			DebugLogger.warn("Unknown upgrade with id=%s and name=\"%s\"", id, tag.getString("upgrade_name"));
			tag.setInteger("upgrade", 0);
			tag.removeTag("upgrade_name");

			if (inventory != null) inventory.markDirty();

			return null;
		}

		return upgrade;
	}

	public IPocketUpgrade getFromItemStack(ItemStack stack) {
		for (IPocketUpgrade entry : upgrades.values()) {
			ItemStack craftingStack = entry.getCraftingItem();
			if (craftingStack != null && InventoryUtil.areItemsStackable(stack, craftingStack)) {
				return entry;
			}
		}

		return null;
	}
}
