package org.squiddev.cctweaks.core.lua;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Duplicate of {@link dan200.computercraft.core.lua.LuaJLuaMachine#toObject(LuaValue)} with
 * binary support
 */
public class BinaryConverter {
	public static Object toObject(LuaValue value, Map<LuaValue, Object> tables) {
		switch (value.type()) {
			case LuaValue.TNUMBER:
			case LuaValue.TINT:
				return value.todouble();
			case LuaValue.TBOOLEAN:
				return value.toboolean();
			case LuaValue.TSTRING: {
				LuaString string = (LuaString) value;
				byte[] result = new byte[string.m_length];
				System.arraycopy(string.m_bytes, string.m_offset, result, 0, string.m_length);
				return result;
			}
			case LuaValue.TTABLE: {
				if (tables == null) {
					tables = new IdentityHashMap<LuaValue, Object>();
				} else {
					Object object = tables.get(value);
					if (object != null) return object;
				}

				Map<Object, Object> table = new HashMap<Object, Object>();
				tables.put(value, table);

				LuaValue k = LuaValue.NIL;
				while (true) {
					Varargs keyValue = value.next(k);
					k = keyValue.arg1();
					if (k.isnil()) break;

					LuaValue v = keyValue.arg(2);
					Object keyObject = toObject(k, tables);
					Object valueObject = toObject(v, tables);
					if (keyObject != null && valueObject != null) {
						table.put(keyObject, valueObject);
					}
				}
				return table;
			}
			default:
				return null;
		}
	}

	public static Object[] toObjects(Varargs values, int start) {
		DebugLogger.debug("Dumping " + values + " from " + start);
		int count = values.narg();
		Object[] objects = new Object[count - start + 1];
		for (int n = start; n <= count; n++) {
			int i = n - start;
			LuaValue value = values.arg(n);
			objects[i] = toObject(value, null);
		}
		return objects;
	}

	/**
	 * Convert the arguments to use strings instead of byte arrays
	 *
	 * @param items The arguments to convert. This will be modified in place
	 * @return The converted arguments (same as {@code items})
	 */
	public static Object[] asStrings(Object[] items) {
		for (int i = 0; i < items.length; i++) {
			Object item = items[i];
			if (item instanceof byte[]) items[i] = new String((byte[]) item);
		}

		return items;
	}

}
