package org.squiddev.cctweaks.pocket;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
import dan200.computercraft.shared.pocket.peripherals.PocketModemPeripheral;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.pocket.IPocketAccess;
import org.squiddev.cctweaks.api.pocket.IPocketUpgrade;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PocketModem implements IPocketUpgrade {
	public static final PocketModem INSTANCE = new PocketModem();

	private PocketModem() {
	}

	@Nonnull
	@Override
	public ResourceLocation getUpgradeID() {
		return new ResourceLocation(CCTweaks.ID, "modem");
	}

	@Nonnull
	@Override
	public String getUnlocalisedAdjective() {
		return "upgrade.computercraft:wireless_modem.adjective";
	}

	@Nullable
	@Override
	public ItemStack getCraftingItem() {
		return PeripheralItemFactory.create(PeripheralType.WirelessModem, null, 1);
	}

	@Nullable
	@Override
	public IPeripheral createPeripheral(@Nonnull IPocketAccess access) {
		return new PocketModemPeripheral(false);
	}

	@Override
	public void update(@Nonnull IPocketAccess access, @Nullable IPeripheral peripheral) {
		if (peripheral instanceof PocketModemPeripheral) {
			Entity entity = access.getEntity();

			PocketModemPeripheral modem = (PocketModemPeripheral) peripheral;
			if (entity instanceof EntityLivingBase) {
				EntityLivingBase player = (EntityLivingBase) entity;
				modem.setLocation(entity.getEntityWorld(), player.posX, player.posY + player.getEyeHeight(), player.posZ);
			} else if (entity != null) {
				modem.setLocation(entity.getEntityWorld(), entity.posX, entity.posY, entity.posZ);
			}

			access.setModemLight(modem.isActive());
		}
	}

	@Override
	public boolean onRightClick(@Nonnull World world, @Nonnull IPocketAccess access, @Nullable IPeripheral peripheral) {
		return false;
	}
}
