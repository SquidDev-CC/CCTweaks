package org.squiddev.cctweaks.core.utils;

import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.lua.LuaJLuaMachine;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.squiddev.cctweaks.core.patch.TileCable_Patch;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Reflection for stuff Dan200 doesn't let us do.
 */
public final class ComputerAccessor {
	/**
	 * Set the fact that the turtle has moved. This is so we can muck about with it
	 * without the broken event being set
	 *
	 * @see dan200.computercraft.shared.turtle.blocks.TileTurtle#m_moveState
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
	 * @see TileCable_Patch#getModem()
	 */
	public static Field cableModem;

	/**
	 * Get the computer for pocket computers
	 *
	 * @see ItemPocketComputer#createServerComputer(World, IInventory, Entity, ItemStack)
	 */
	public static Method pocketServerComputer;

	static {
		try {
			turtleTileMoved = findField(TileTurtle.class, "m_movedState");
			serverComputerComputer = findField(ServerComputer.class, "m_computer");
			computerMachine = findField(Computer.class, "m_machine");
			luaMachineGlobals = findField(LuaJLuaMachine.class, "m_globals");
			cableModem = findField(TileCable.class, "modem");
			pocketServerComputer = findMethod(ItemPocketComputer.class, "createServerComputer", World.class, IInventory.class, Entity.class, ItemStack.class);
		} catch (Exception e) {
			DebugLogger.error("ComputerCraft not found", e);
			e.printStackTrace();
		}
	}

	private static Method findMethod(Class<?> klass, String name, Class<?>... arguments) {
		try {
			Method method = klass.getDeclaredMethod(name, arguments);
			method.setAccessible(true);
			return method;
		} catch (NoSuchMethodException e) {
			throw new ReflectionHelper.UnableToFindMethodException(new String[]{name}, e);
		}
	}

	private static Field findField(Class<?> klass, String name) {
		try {
			Field field = klass.getDeclaredField(name);
			field.setAccessible(true);
			return field;
		} catch (NoSuchFieldException e) {
			throw new ReflectionHelper.UnableToFindFieldException(new String[]{name}, e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getField(Field field, Object instance) {
		try {
			return (T) field.get(instance);
		} catch (IllegalAccessException e) {
			throw new ReflectionHelper.UnableToAccessFieldException(new String[0], e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T callMethod(Method method, Object instance, Object... args) {
		try {
			return (T) method.invoke(instance, args);
		} catch (IllegalAccessException e) {
			throw new ReflectionHelper.UnableToAccessFieldException(new String[0], e);
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
