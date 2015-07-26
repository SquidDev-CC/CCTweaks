package org.squiddev.cctweaks.integration.peripheralspp;

import com.austinv11.peripheralsplusplus.hooks.IPocketComputerUpgrade;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.IDataCard;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.network.bridge.NetworkBindingWithModem;
import org.squiddev.cctweaks.core.network.modem.BasicModemPeripheral;
import org.squiddev.cctweaks.core.registry.Registry;
import org.squiddev.cctweaks.core.utils.EntityPosition;

public class PocketWirelessBinding implements IPocketComputerUpgrade {
	@Override
	public int getUpgradeID() {
		return Config.Network.WirelessBridge.pocketId;
	}

	@Override
	public String getUnlocalisedAdjective() {
		return "pocket." + CCTweaks.RESOURCE_DOMAIN + ".wirelessBridge.adjective";
	}

	@Override
	public ItemStack getCraftingItem() {
		return Config.Network.WirelessBridge.pocketEnabled ? new ItemStack(Registry.blockNetworked, 0) : null;
	}

	@Override
	public IPeripheral createPeripheral(Entity entity, ItemStack stack) {
		return Config.Network.WirelessBridge.pocketEnabled ? new PocketBinding(entity, stack).getModem().modem : null;
	}

	@Override
	public void update(Entity entity, ItemStack itemStack, IPeripheral peripheral) {
		if (peripheral instanceof PocketBinding.PocketModemPeripheral) {
			PocketBinding binding = ((PocketBinding.PocketModemPeripheral) peripheral).getBinding();
			if (binding != null) binding.setOwner(entity);
		}
	}

	@Override
	public boolean onRightClick(World world, EntityPlayer entity, ItemStack itemStack, IPeripheral peripheral) {
		return false;
	}

	public static class PocketBinding extends NetworkBindingWithModem {
		public final ItemStack stack;
		protected Entity owner;

		public PocketBinding(Entity owner, ItemStack stack) {
			super(new EntityPosition(owner));
			this.stack = stack;
		}

		@Override
		public BindingModem createModem() {
			return new PocketModem();
		}

		@Override
		public PocketModem getModem() {
			return (PocketModem) modem;
		}

		@Override
		public void connect() {
			load(stack.getTagCompound());
			super.connect();
		}

		public void save() {
			save(stack.getTagCompound());
		}

		public void setOwner(Entity owner) {
			((EntityPosition) position).entity = owner;
			this.owner = owner;
		}

		/**
		 * Custom modem that allows modifying bindings
		 */
		public class PocketModem extends BindingModem {
			@Override
			protected BasicModemPeripheral createPeripheral() {
				return new PocketModemPeripheral(this);
			}

			@Override
			public boolean canConnect(ForgeDirection side) {
				return side == ForgeDirection.UNKNOWN;
			}
		}

		/**
		 * Calls {@link PocketBinding#connect()} and {@link PocketBinding#destroy()} on attach and detach.
		 */
		public class PocketModemPeripheral extends BindingModemPeripheral {
			public PocketModemPeripheral(NetworkBindingWithModem.BindingModem modem) {
				super(modem);
			}

			@Override
			public String[] getMethodNames() {
				String[] methods = super.getMethodNames();
				String[] newMethods = new String[methods.length + 2];
				System.arraycopy(methods, 0, newMethods, 0, methods.length);


				int l = methods.length;
				newMethods[l] = "bindFromCard";
				newMethods[l + 1] = "bindToCard";

				return newMethods;
			}

			@Override
			public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
				String[] methods = super.getMethodNames();
				switch (method - methods.length) {
					case 0: { // bindFromCard
						if (!(owner instanceof EntityPlayer)) return new Object[]{false, "No inventory found"};
						InventoryPlayer inventory = ((EntityPlayer) owner).inventory;

						int size = inventory.getSizeInventory(), held = inventory.currentItem;
						for (int i = 0; i < size; i++) {
							ItemStack stack = inventory.getStackInSlot((i + held) % size);
							if (stack != null && stack.getItem() instanceof IDataCard) {
								IDataCard card = (IDataCard) stack.getItem();
								if (PocketBinding.this.load(stack, card)) {
									PocketBinding.this.save();
									return new Object[]{true};
								}
							}
						}

						return new Object[]{false, "No card found"};
					}
					case 1: { // bindToCard
						if (!(owner instanceof EntityPlayer)) return new Object[]{false, "No inventory found"};
						InventoryPlayer inventory = ((EntityPlayer) owner).inventory;

						int size = inventory.getSizeInventory(), held = inventory.currentItem;
						for (int i = 0; i < size; i++) {
							ItemStack stack = inventory.getStackInSlot((i + held) % size);
							if (stack != null && stack.getItem() instanceof IDataCard) {
								IDataCard card = (IDataCard) stack.getItem();
								PocketBinding.this.save(stack, card);
								return new Object[]{true};
							}
						}

						return new Object[]{false, "No card found"};
					}
				}

				return super.callMethod(computer, context, method, arguments);
			}

			@Override
			public synchronized void attach(IComputerAccess computer) {
				PocketBinding.this.connect();
				super.attach(computer);
			}

			@Override
			public synchronized void detach(IComputerAccess computer) {
				super.detach(computer);
				PocketBinding.this.destroy();
			}

			public PocketBinding getBinding() {
				return PocketBinding.this;
			}
		}
	}
}
