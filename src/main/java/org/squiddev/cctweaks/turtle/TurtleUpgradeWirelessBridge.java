package org.squiddev.cctweaks.turtle;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.*;
import dan200.computercraft.shared.util.PeripheralUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.IDataCard;
import org.squiddev.cctweaks.api.network.INetworkCompatiblePeripheral;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNodeHost;
import org.squiddev.cctweaks.api.turtle.IExtendedTurtleUpgrade;
import org.squiddev.cctweaks.blocks.network.BlockNetworked;
import org.squiddev.cctweaks.blocks.network.TileNetworkedWirelessBridge;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.network.bridge.NetworkBindingWithModem;
import org.squiddev.cctweaks.core.network.modem.BasicModemPeripheral;
import org.squiddev.cctweaks.core.network.modem.PeripheralCollection;
import org.squiddev.cctweaks.core.peripheral.PeripheralProxy;
import org.squiddev.cctweaks.core.registry.Module;
import org.squiddev.cctweaks.core.registry.Registry;
import org.squiddev.cctweaks.core.turtle.LuaDirection;

import java.util.Map;

/**
 * Turtle upgrade for the {@link TileNetworkedWirelessBridge} tile
 */
public class TurtleUpgradeWirelessBridge extends Module implements ITurtleUpgrade, IExtendedTurtleUpgrade {
	@Override
	public int getUpgradeID() {
		return Config.Network.WirelessBridge.turtleId;
	}

	@Override
	public String getUnlocalisedAdjective() {
		return "turtle." + CCTweaks.RESOURCE_DOMAIN + ".wirelessBridge.adjective";
	}

	@Override
	public TurtleUpgradeType getType() {
		return TurtleUpgradeType.Peripheral;
	}

	@Override
	public ItemStack getCraftingItem() {
		return Config.Network.WirelessBridge.turtleEnabled ? new ItemStack(Registry.blockNetworked, 1, 0) : null;
	}

	@Override
	public IPeripheral createPeripheral(ITurtleAccess turtle, TurtleSide side) {
		return Config.Network.WirelessBridge.turtleEnabled ? new TurtleBinding(turtle, side).getModem().modem : null;
	}

	@Override
	public TurtleCommandResult useTool(ITurtleAccess turtle, TurtleSide side, TurtleVerb verb, int direction) {
		return null;
	}

	@Override
	public IIcon getIcon(ITurtleAccess turtle, TurtleSide side) {
		return BlockNetworked.bridgeSmallIcon;
	}

	@Override
	public void update(ITurtleAccess turtle, TurtleSide side) {
	}

	@Override
	public void init() {
		ComputerCraft.registerTurtleUpgrade(this);
	}

	@Override
	public void upgradeChanged(ITurtleAccess turtle, TurtleSide side, ITurtleUpgrade oldUpgrade, ITurtleUpgrade newUpgrade) {
		if (Config.Network.WirelessBridge.turtleEnabled) {
			IPeripheral peripheral = turtle.getPeripheral(side);
			if (peripheral instanceof TurtleBinding.TurtleModemPeripheral) {
				IWorldNetworkNode binding = ((TurtleBinding.TurtleModemPeripheral) peripheral).getNode();
				INetworkController network = binding.getAttachedNetwork();
				if (network != null) network.invalidateNode(binding);
			}
		}
	}

	@Override
	public boolean alsoPeripheral() {
		return true;
	}

	public static class TurtleBinding extends NetworkBindingWithModem {
		public final ITurtleAccess turtle;
		public final TurtleSide side;

		public TurtleBinding(ITurtleAccess turtle, TurtleSide side) {
			super(new TurtlePosition(turtle));
			this.turtle = turtle;
			this.side = side;
		}

		@Override
		public BindingModem createModem() {
			return new TurtleModem();
		}

		@Override
		public TurtleModem getModem() {
			return (TurtleModem) modem;
		}

		@Override
		public void markDirty() {
			save();
		}

		@Override
		public void connect() {
			load(turtle.getUpgradeNBTData(side));
			getModem().load();

			super.connect();
		}

		public void save() {
			save(turtle.getUpgradeNBTData(side));
			getModem().save();
			turtle.updateUpgradeNBTData(side);
		}

		/**
		 * Custom modem that allows modifying bindings
		 */
		public class TurtleModem extends BindingModem {
			protected final PeripheralCollection peripherals = new PeripheralCollection(2) {
				private final IPeripheral peripheral = new PeripheralProxy("turtle") {
					@Override
					protected IPeripheral createPeripheral() {
						ChunkCoordinates pos = turtle.getPosition();
						return PeripheralUtil.getPeripheral(turtle.getWorld(), pos.posX, pos.posY, pos.posZ, 0);
					}
				};

				@Override
				protected IPeripheral[] getPeripherals() {
					IPeripheral[] peripherals = new IPeripheral[2];
					peripherals[0] = peripheral;

					IPeripheral opposite = turtle.getPeripheral(side == TurtleSide.Left ? TurtleSide.Right : TurtleSide.Left);
					if (opposite instanceof INetworkCompatiblePeripheral) peripherals[1] = opposite;

					return peripherals;
				}

				@Override
				protected World getWorld() {
					return turtle.getWorld();
				}

				@Override
				protected void changed() {
					super.changed();
					TurtleBinding.this.save();
				}
			};

			public void load() {
				NBTTagCompound data = turtle.getUpgradeNBTData(side);

				// Backwards compatibility
				if (data.hasKey("turtle_id")) {
					peripherals.ids[0] = data.getInteger("turtle_id");
					data.removeTag("turtle_id");
				}

				int[] ids = data.getIntArray("peripheral_ids");
				if (ids != null && ids.length == 6) System.arraycopy(ids, 0, peripherals.ids, 0, 6);
			}

			public void save() {
				NBTTagCompound tag = turtle.getUpgradeNBTData(side);
				tag.setIntArray("peripheral_ids", peripherals.ids);
			}

			@Override
			public Map<String, IPeripheral> getConnectedPeripherals() {
				return peripherals.getConnectedPeripherals();
			}

			@Override
			protected BasicModemPeripheral<?> createPeripheral() {
				return new TurtleModemPeripheral(this);
			}

			@Override
			public boolean canConnect(ForgeDirection side) {
				return side == ForgeDirection.UNKNOWN;
			}
		}

		/**
		 * Extension of modem with bindToCard and bindFromCard methods
		 *
		 * Also calls {@link TurtleBinding#connect()} and {@link TurtleBinding#destroy()} on attach and detach.
		 */
		public class TurtleModemPeripheral extends BindingModemPeripheral implements IWorldNetworkNodeHost {
			public TurtleModemPeripheral(BindingModem modem) {
				super(modem);
			}

			@Override
			public String[] getMethodNames() {
				String[] methods = super.getMethodNames();
				String[] newMethods = new String[methods.length + 4];
				System.arraycopy(methods, 0, newMethods, 0, methods.length);


				int l = methods.length;
				newMethods[l] = "bindFromCard";
				newMethods[l + 1] = "bindToCard";
				newMethods[l + 2] = "bindToBlock";
				newMethods[l + 3] = "bindFromBlock";

				return newMethods;
			}

			@Override
			public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
				String[] methods = super.getMethodNames();
				switch (method - methods.length) {
					case 0: { // bindFromCard
						ItemStack stack = turtle.getInventory().getStackInSlot(turtle.getSelectedSlot());
						if (stack != null && stack.getItem() instanceof IDataCard) {
							IDataCard card = (IDataCard) stack.getItem();
							if (TurtleBinding.this.load(stack, card)) {
								TurtleBinding.this.save();
								return new Object[]{true};
							}
						}
						return new Object[]{false};
					}
					case 1: { // bindToCard
						ItemStack stack = turtle.getInventory().getStackInSlot(turtle.getSelectedSlot());
						if (stack != null && stack.getItem() instanceof IDataCard) {
							IDataCard card = (IDataCard) stack.getItem();
							TurtleBinding.this.save(stack, card);
							return new Object[]{true};
						}
						return new Object[]{false};
					}
					case 2: {
						String direction;
						if (arguments.length == 0) {
							direction = "forward";
						} else if (arguments[1] instanceof String) {
							direction = (String) arguments[1];
						} else {
							throw new LuaException("Expected string");
						}

						ChunkCoordinates coords = LuaDirection.getRelative(direction, turtle.getDirection(), turtle.getPosition());
						TileEntity tile = turtle.getWorld().getTileEntity(coords.posX, coords.posY, coords.posZ);

						if (!(tile instanceof TileNetworkedWirelessBridge)) {
							throw new LuaException("No wireless bridge here");
						}

						TileNetworkedWirelessBridge bridge = (TileNetworkedWirelessBridge) tile;
						bridge.setBindingId(TurtleBinding.this.getUuid());
						break;
					}
					case 3: {
						String direction;
						if (arguments.length == 0) {
							direction = "forward";
						} else if (arguments[1] instanceof String) {
							direction = (String) arguments[1];
						} else {
							throw new LuaException("Expected string");
						}

						ChunkCoordinates coords = LuaDirection.getRelative(direction, turtle.getDirection(), turtle.getPosition());
						TileEntity tile = turtle.getWorld().getTileEntity(coords.posX, coords.posY, coords.posZ);

						if (!(tile instanceof TileNetworkedWirelessBridge)) {
							throw new LuaException("No wireless bridge here");
						}

						TileNetworkedWirelessBridge bridge = (TileNetworkedWirelessBridge) tile;
						TurtleBinding.this.setUuid(bridge.getBindingId());
						TurtleBinding.this.save();
						break;
					}
				}

				return super.callMethod(computer, context, method, arguments);
			}

			@Override
			public boolean equals(IPeripheral other) {
				if (other == this) return true;
				if (!(other instanceof TurtleModemPeripheral)) return false;

				TurtleModemPeripheral otherModem = (TurtleModemPeripheral) other;
				return otherModem.getTurtle().equals(otherModem.getTurtle());
			}

			@Override
			public synchronized void attach(IComputerAccess computer) {
				TurtleBinding.this.connect();
				super.attach(computer);
			}

			@Override
			public synchronized void detach(IComputerAccess computer) {
				super.detach(computer);
				TurtleBinding.this.destroy();
			}

			public ITurtleAccess getTurtle() {
				return turtle;
			}

			@Override
			public IWorldNetworkNode getNode() {
				return TurtleBinding.this;
			}
		}
	}
}
