package org.squiddev.cctweaks.core.pocket;

import com.google.common.base.Preconditions;
import dan200.computercraft.shared.util.InventoryUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.cctweaks.api.pocket.IPocketRegistry;
import org.squiddev.cctweaks.api.pocket.IPocketUpgrade;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.cctweaks.pocket.PocketModem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class PocketRegistry implements IPocketRegistry {
	public static final short FLAG = 0xFF;
	public static final short MODEM = 1;

	public static final PocketRegistry instance = new PocketRegistry();

	private final Map<Integer, IPocketUpgrade> legacyUpgrades = new HashMap<Integer, IPocketUpgrade>();
	private final Map<String, IPocketUpgrade> upgrades = new HashMap<String, IPocketUpgrade>();

	@Override
	public void addUpgrade(@Nonnull IPocketUpgrade upgrade) {
		Preconditions.checkNotNull(upgrade, "upgrade cannot be null");

		String location = upgrade.getUpgradeID().toString();
		if (upgrades.containsKey(location)) throw new IllegalArgumentException(location + " is already registered");

		upgrades.put(location, upgrade);
	}

	@Override
	public void addLegacyUpgrade(int id, @Nonnull IPocketUpgrade upgrade) {
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
		return getUpgradeInternal(tag, id, inventory);
	}

	public String getUpgradeAdjective(ItemStack stack, IInventory inventory) {
		if (!stack.hasTagCompound()) return null;

		NBTTagCompound tag = stack.getTagCompound();
		if (!tag.hasKey("upgrade")) return null;

		int id = tag.getShort("upgrade");
		IPocketUpgrade upgrade = getUpgradeInternal(tag, id, inventory);
		return upgrade == null ? null : upgrade.getUnlocalisedAdjective();
	}

	private IPocketUpgrade getUpgradeInternal(NBTTagCompound tag, int id, IInventory inventory) {
		IPocketUpgrade upgrade;
		switch (id) {
			case 0:
				return null;
			case MODEM:
				return PocketModem.INSTANCE;
			case FLAG:
				upgrade = upgrades.get(tag.getString("upgrade_name"));
				break;
			default:
				upgrade = legacyUpgrades.get(id);
				if (upgrade != null) {
					tag.setInteger("upgrade", FLAG);
					tag.setString("upgrade_name", upgrade.getUpgradeID().toString());
				}
				break;
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

	@Nullable
	public IPocketUpgrade getFromItemStack(@Nonnull ItemStack stack) {
		for (IPocketUpgrade entry : upgrades.values()) {
			ItemStack craftingStack = entry.getCraftingItem();
			if (craftingStack != null && InventoryUtil.areItemsStackable(stack, craftingStack)) {
				return entry;
			}
		}

		return null;
	}

	public void setToItemStack(@Nonnull ItemStack stack, @Nullable IPocketUpgrade upgrade) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag == null) stack.setTagCompound(tag = new NBTTagCompound());

		tag.removeTag("upgrade_info");
		if (upgrade == null) {
			tag.setInteger("upgrade", 0);
			tag.removeTag("upgrade_name");
		} else if (upgrade == PocketModem.INSTANCE) {
			tag.setInteger("upgrade", 1);
			tag.removeTag("upgrade_name");
		} else {
			tag.setInteger("upgrade", FLAG);
			tag.setString("upgrade_name", upgrade.getUpgradeID().toString());
		}
	}

	public Collection<IPocketUpgrade> getUpgrades() {
		return upgrades.values();
	}
}
