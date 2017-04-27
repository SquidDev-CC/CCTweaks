package org.squiddev.cctweaks.turtle;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.*;
import dan200.computercraft.shared.util.PeripheralUtil;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.cctweaks.api.IDataCard;
import org.squiddev.cctweaks.api.network.INetworkCompatiblePeripheral;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNodeHost;
import org.squiddev.cctweaks.api.turtle.IExtendedTurtleUpgrade;
import org.squiddev.cctweaks.blocks.network.TileNetworkedWirelessBridge;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.network.bridge.NetworkBindingWithModem;
import org.squiddev.cctweaks.core.network.modem.BasicModemPeripheral;
import org.squiddev.cctweaks.core.network.modem.PeripheralCollection;
import org.squiddev.cctweaks.core.peripheral.PeripheralProxy;
import org.squiddev.cctweaks.core.registry.Registry;
import org.squiddev.cctweaks.core.turtle.LuaDirection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.Map;

/**
 * Turtle upgrade for the {@link TileNetworkedWirelessBridge} tile
 */
public class TurtleUpgradeWirelessBridge extends TurtleUpgradeBase implements IExtendedTurtleUpgrade {
	@SideOnly(Side.CLIENT)
	@SuppressWarnings("deprecation")
	private IBakedModel modelLeft;

	@SideOnly(Side.CLIENT)
	@SuppressWarnings("deprecation")
	private IBakedModel modelRight;

	public TurtleUpgradeWirelessBridge() {
		super("wirelessBridge", Config.Network.WirelessBridge.turtleId);
	}

	@Override
	public TurtleUpgradeType getType() {
		return TurtleUpgradeType.Peripheral;
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Registry.blockNetworked, 1, 0);
	}

	@Override
	public ItemStack getCraftingItem() {
		return Config.Network.WirelessBridge.turtleEnabled ? super.getCraftingItem() : null;
	}

	@Override
	public IPeripheral createPeripheral(ITurtleAccess turtle, TurtleSide side) {
		return Config.Network.WirelessBridge.turtleEnabled ? new TurtleBinding(turtle, side).getModem().modem : null;
	}

	@Override
	public TurtleCommandResult useTool(ITurtleAccess turtle, TurtleSide side, TurtleVerb verb, EnumFacing direction) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings("deprecation")
	public Pair<IBakedModel, Matrix4f> getModel(ITurtleAccess access, TurtleSide side) {
		if (modelLeft == null) {
			ModelManager manager = getMesher().getModelManager();
			modelLeft = manager.getModel(new ModelResourceLocation("cctweaks:wireless_bridge_turtle_left", "inventory"));
			modelRight = manager.getModel(new ModelResourceLocation("cctweaks:wireless_bridge_turtle_right", "inventory"));
		}

		return Pair.of(side == TurtleSide.Left ? modelLeft : modelRight, null);
	}

	@Override
	public void init() {
		ComputerCraft.registerTurtleUpgrade(this);
	}

	@Override
	public void upgradeChanged(@Nonnull ITurtleAccess turtle, @Nonnull TurtleSide side, ITurtleUpgrade oldUpgrade, ITurtleUpgrade newUpgrade) {
		if (Config.Network.WirelessBridge.turtleEnabled) {
			IPeripheral peripheral = turtle.getPeripheral(side);
			if (peripheral instanceof TurtleBinding.TurtleModemPeripheral) {
				IWorldNetworkNode binding = ((TurtleBinding.TurtleModemPeripheral) peripheral).getNode();
				INetworkController controller = binding.getAttachedNetwork();
				if (controller != null) controller.invalidateNode(binding);
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
					@Nonnull
					@Override
					protected IPeripheral createPeripheral() {
						return PeripheralUtil.getPeripheral(turtle.getWorld(), turtle.getPosition(), EnumFacing.DOWN);
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

			@Nonnull
			@Override
			public Map<String, IPeripheral> getConnectedPeripherals() {
				return peripherals.getConnectedPeripherals();
			}

			@Override
			protected BasicModemPeripheral<?> createPeripheral() {
				return new TurtleModemPeripheral(this);
			}

			@Override
			public boolean canConnect(@Nullable EnumFacing side) {
				return side == null;
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

						BlockPos coords = LuaDirection.getRelative(direction, turtle.getDirection(), turtle.getPosition());
						TileEntity tile = turtle.getWorld().getTileEntity(coords);

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

						BlockPos coords = LuaDirection.getRelative(direction, turtle.getDirection(), turtle.getPosition());
						TileEntity tile = turtle.getWorld().getTileEntity(coords);

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

			@Nonnull
			@Override
			public IWorldNetworkNode getNode() {
				return TurtleBinding.this;
			}
		}
	}
}
