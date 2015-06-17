package org.squiddev.cctweaks.turtle;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.*;
import dan200.computercraft.shared.util.IDAssigner;
import dan200.computercraft.shared.util.PeripheralUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.IDataCard;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.blocks.network.BlockNetworked;
import org.squiddev.cctweaks.blocks.network.TileNetworkedWirelessBridge;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.network.bridge.NetworkBinding;
import org.squiddev.cctweaks.core.network.modem.BasicModem;
import org.squiddev.cctweaks.core.network.modem.BasicModemPeripheral;
import org.squiddev.cctweaks.core.registry.Module;
import org.squiddev.cctweaks.core.registry.Registry;

import java.io.File;
import java.util.Collections;
import java.util.Map;

/**
 * Turtle upgrade for the {@link TileNetworkedWirelessBridge} tile
 */
public class TurtleUpgradeWirelessBridge extends Module implements ITurtleUpgrade {
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
		return Config.Network.WirelessBridge.turtleEnabled ? new ItemStack(Registry.blockNetworked, 0) : null;
	}

	@Override
	public IPeripheral createPeripheral(ITurtleAccess turtle, TurtleSide side) {
		return Config.Network.WirelessBridge.turtleEnabled ? new TurtleModem(turtle, side).modem : null;
	}

	@Override
	public TurtleCommandResult useTool(ITurtleAccess turtle, TurtleSide side, TurtleVerb verb, int direction) {
		return null;
	}

	@Override
	public IIcon getIcon(ITurtleAccess turtle, TurtleSide side) {
		return BlockNetworked.bridgeSmallIcon;
	}

	/**
	 * Called on turtle update. Used to check if messages should be sent
	 *
	 * @param turtle Current turtle
	 * @param side   Peripheral side
	 */
	@Override
	public void update(ITurtleAccess turtle, TurtleSide side) {
		if (Config.Network.WirelessBridge.turtleEnabled && !turtle.getWorld().isRemote) {
			IPeripheral peripheral = turtle.getPeripheral(side);
			if (peripheral != null && peripheral instanceof TurtleModemPeripheral) {
				TurtleModemPeripheral modemPeripheral = (TurtleModemPeripheral) peripheral;

				TurtleModem modem = modemPeripheral.modem;
				modem.processQueue();
				if (modemPeripheral.pollChanged()) modem.save();
			}
		}
	}

	@Override
	public void init() {
		ComputerCraft.registerTurtleUpgrade(this);
	}

	/**
	 * Custom modem that allows binding and
	 */
	public static class TurtleModem extends BasicModem implements IWorldPosition {
		public final ITurtleAccess turtle;
		public final TurtleSide side;
		protected int id = -1;
		protected final NetworkBinding binding;

		public TurtleModem(ITurtleAccess turtle, TurtleSide side) {
			this.turtle = turtle;
			this.side = side;
			binding = new NetworkBinding(this);

			NBTTagCompound data = turtle.getUpgradeNBTData(side);
			binding.load(data);
			if (data.hasKey("turtle_id")) id = data.getInteger("turtle_id");
		}

		@Override
		public IWorldPosition getPosition() {
			return this;
		}

		public void save() {
			NBTTagCompound data = turtle.getUpgradeNBTData(side);
			binding.save(data);
			data.setBoolean("active", isActive());
			data.setInteger("turtle_id", id);
			turtle.updateUpgradeNBTData(side);
		}

		/**
		 * Get the turtle as a peripheral
		 *
		 * @return The turtle peripheral
		 */
		@Override
		public Map<String, IPeripheral> getConnectedPeripherals() {
			ChunkCoordinates pos = turtle.getPosition();
			IPeripheral peripheral = PeripheralUtil.getPeripheral(getWorld(), pos.posX, pos.posY, pos.posZ, 0);
			if (peripheral == null) {
				id = -1;
				return Collections.emptyMap();
			} else if (id <= -1) {
				id = IDAssigner.getNextIDFromFile(new File(ComputerCraft.getWorldDir(getWorld()), "computer/lastid_" + peripheral.getType() + ".txt"));
			}

			return Collections.singletonMap(peripheral.getType() + "_" + id, peripheral);
		}

		@Override
		protected BasicModemPeripheral createPeripheral() {
			return new TurtleModemPeripheral(this);
		}

		@Override
		public boolean canConnect(ForgeDirection side) {
			return side == ForgeDirection.UNKNOWN;
		}

		@Override
		public World getWorld() {
			return turtle.getWorld();
		}

		@Override
		public int getX() {
			return turtle.getPosition().posX;
		}

		@Override
		public int getY() {
			return turtle.getPosition().posY;
		}

		@Override
		public int getZ() {
			return turtle.getPosition().posZ;
		}
	}

	/**
	 * Extension of modem with bindToCard and bindFromCard methods
	 */
	public static class TurtleModemPeripheral extends BasicModemPeripheral<TurtleModem> {
		public final NetworkBinding binding;

		public TurtleModemPeripheral(TurtleModem modem) {
			super(modem);
			binding = new NetworkBinding(modem);
		}

		@Override
		public String[] getMethodNames() {
			String[] methods = super.getMethodNames();
			String[] newMethods = new String[methods.length + 3];
			System.arraycopy(methods, 0, newMethods, 0, methods.length);


			int l = methods.length;
			newMethods[l] = "bindFromCard";
			newMethods[l + 1] = "bindToCard";

			return newMethods;
		}

		@Override
		public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
			try {
				String[] methods = super.getMethodNames();
				switch (method - methods.length) {
					case 0: { // bindFromCard
						ItemStack stack = modem.turtle.getInventory().getStackInSlot(modem.turtle.getSelectedSlot());
						if (stack != null && stack.getItem() instanceof IDataCard) {
							IDataCard card = (IDataCard) stack.getItem();
							if (binding.load(stack, card)) {
								modem.save();
								return new Object[]{true};
							}
						}
						return new Object[]{false};
					}
					case 1: { // bindToCard
						ItemStack stack = modem.turtle.getInventory().getStackInSlot(modem.turtle.getSelectedSlot());
						if (stack != null && stack.getItem() instanceof IDataCard) {
							IDataCard card = (IDataCard) stack.getItem();
							binding.save(stack, card);
							modem.save();
							return new Object[]{true};
						}
						return new Object[]{false};
					}
				}

				return super.callMethod(computer, context, method, arguments);
			} catch (RuntimeException e) {
				e.printStackTrace();
				throw e;
			}
		}
	}
}
