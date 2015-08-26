package org.squiddev.cctweaks.core.utils;

import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.lua.LuaJLuaMachine;
import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Reflection for stuff Dan200 doesn't let us do.
 */
public final class ComputerAccessor {
	/**
	 * Copy a computer from one tile to another
	 */
	public static Method tileCopy;

	/**
	 * Set the fact that the turtle has moved. This is so we can muck about with it
	 * without the broken event being set
	 *
	 * @see dan200.computercraft.shared.turtle.blocks.TileTurtle#m_moved
	 */
	public static Field turtleTileMoved;

	/**
	 * The ServerComputer's {@link dan200.computercraft.core.computer.Computer}
	 *
	 * @see dan200.computercraft.shared.computer.core.ServerComputer#m_computer
	 */
	public static Field serverComputerComputer;

	/**
	 * The Computer's {@link dan200.computercraft.core.lua.ILuaMachine}
	 *
	 * @see dan200.computercraft.core.computer.Computer#m_machine
	 */
	public static Field computerMachine;

	/**
	 * The globals of a LuaMachine
	 *
	 * @see dan200.computercraft.core.lua.LuaJLuaMachine#m_globals
	 */
	public static Field luaMachineGlobals;

	/**
	 * The peripheral ID of a wired modem
	 *
	 * @see TileCable#m_attachedPeripheralID
	 */
	public static Field cablePeripheralId;

	/**
	 * Get the computer for pocket computers
	 *
	 * @see ItemPocketComputer#createServerComputer(World, IInventory, ItemStack)
	 */
	public static Method pocketServerComputer;

	static {
		try {
			tileCopy = TileComputerBase.class.getDeclaredMethod("transferStateFrom", TileComputerBase.class);
			tileCopy.setAccessible(true);

			turtleTileMoved = TileTurtle.class.getDeclaredField("m_moved");
			turtleTileMoved.setAccessible(true);

			serverComputerComputer = ServerComputer.class.getDeclaredField("m_computer");
			serverComputerComputer.setAccessible(true);

			computerMachine = Computer.class.getDeclaredField("m_machine");
			computerMachine.setAccessible(true);

			luaMachineGlobals = LuaJLuaMachine.class.getDeclaredField("m_globals");
			luaMachineGlobals.setAccessible(true);

			cablePeripheralId = TileCable.class.getDeclaredField("m_attachedPeripheralID");
			cablePeripheralId.setAccessible(true);

			pocketServerComputer = ItemPocketComputer.class.getDeclaredMethod("createServerComputer", World.class, IInventory.class, ItemStack.class);
			pocketServerComputer.setAccessible(true);
		} catch (Exception e) {
			DebugLogger.error("ComputerCraft not found", e);
			e.printStackTrace();
		}
	}
}
