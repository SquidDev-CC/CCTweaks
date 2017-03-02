package org.squiddev.cctweaks.pocket;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
import dan200.computercraft.shared.peripheral.modem.WirelessModemPeripheral;
import dan200.computercraft.shared.peripheral.modem.WirelessNetwork;
import dan200.computercraft.shared.pocket.peripherals.PocketModemPeripheral;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.CCTweaksAPI;
import org.squiddev.cctweaks.api.pocket.IPocketAccess;
import org.squiddev.cctweaks.api.pocket.IPocketRegistry;
import org.squiddev.cctweaks.api.pocket.IPocketUpgrade;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.registry.Module;

import javax.annotation.Nonnull;

public class PocketEnderModem extends Module implements IPocketUpgrade {
	@Nonnull
	@Override
	public ResourceLocation getUpgradeID() {
		return new ResourceLocation(CCTweaks.RESOURCE_DOMAIN, "advancedModem");
	}

	@Nonnull
	@Override
	public String getUnlocalisedAdjective() {
		return "pocket." + CCTweaks.RESOURCE_DOMAIN + ".advancedModem.adjective";
	}

	@Override
	public ItemStack getCraftingItem() {
		return Config.Misc.pocketEnderModem ? PeripheralItemFactory.create(PeripheralType.AdvancedModem, null, 1) : null;
	}

	@Override
	public IPeripheral createPeripheral(@Nonnull IPocketAccess access) {
		return Config.Misc.pocketEnderModem ? new PocketModem(access, true) : null;
	}

	@Override
	public void update(@Nonnull IPocketAccess access, IPeripheral peripheral) {
		if (Config.Misc.pocketEnderModem && peripheral instanceof PocketModem) {
			PocketModem modem = (PocketModem) peripheral;
			access.setModemLight(modem.isActive());
		}
	}

	@Override
	public boolean onRightClick(@Nonnull World world, @Nonnull IPocketAccess access, IPeripheral peripheral) {
		return false;
	}

	private static class PocketModem extends WirelessModemPeripheral implements IPeripheral {
		private final IPocketAccess access;

		public PocketModem(IPocketAccess access, boolean advanced) {
			super(advanced);
			this.access = access;
		}

		@Override
		protected World getWorld() {
			/**
			 * Normal modem updates this, but I don't think we need this as {@link #getNetwork()} always returns
			 * {@link WirelessNetwork#getUniversal()}
			 * @see PocketModemPeripheral#getNetwork()
			 * @see dan200.computercraft.shared.pocket.items.ItemPocketComputer
			 */

			Entity entity = this.access.getEntity();
			return entity == null ? null : entity.getEntityWorld();
		}

		@Override
		protected Vec3 getPosition() {
			Entity entity = this.access.getEntity();
			if (entity instanceof EntityLivingBase) {
				EntityLivingBase modemLight = (EntityLivingBase) entity;
				return new Vec3(modemLight.posX, modemLight.posY + modemLight.height, modemLight.posZ);
			} else if (entity != null) {
				return new Vec3(entity.posX, entity.posY, entity.posZ);
			} else {
				return null;
			}
		}

		@Override
		public boolean equals(IPeripheral other) {
			return other instanceof PocketModem && ((PocketModem) other).access == access;
		}
	}

	@Override
	public void preInit() {
		IPocketRegistry registry = CCTweaksAPI.instance().pocketRegistry();
		registry.addUpgrade(this);
	}
}
