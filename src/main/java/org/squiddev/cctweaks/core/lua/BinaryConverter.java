package org.squiddev.cctweaks.core.lua;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.io.UnsupportedEncodingException;
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
		int count = values.narg();
		Object[] objects = new Object[count - start + 1];
		for (int n = start; n <= count; n++) {
			int i = n - start;
			LuaValue value = values.arg(n);
			objects[i] = toObject(value, null);
		}
		return objects;
	}

	public static Object toString(Object value, Map<Object, Object> tables) {
		if (value instanceof byte[]) {
			return new String((byte[]) value);
		} else if (value instanceof Map) {
			if (tables == null) {
				tables = new IdentityHashMap<Object, Object>();
			} else {
				Object object = tables.get(value);
				if (object != null) return object;
			}

			Map<Object, Object> newMap = new HashMap<Object, Object>();
			tables.put(value, newMap);

			Map map = (Map) value;

			for (Object key : map.keySet()) {
				newMap.put(toString(key, tables), toString(map.get(key), tables));
			}

			return newMap;
		} else {
			return value;
		}
	}


	/**
	 * Convert the arguments to use strings instead of byte arrays
	 *
	 * @param items The arguments to convert. This will be modified in place
	 */
	public static void toStrings(Object[] items) {
		for (int i = 0; i < items.length; i++) {
			items[i] = toString(items[i], null);
		}
	}

	public static String decodeString(byte[] bytes) {
		try {
			return new String(bytes, "UTF8");
		} catch (UnsupportedEncodingException e) {
			try {
				// Fall back. Shouldn't happen, but you never know
				return new String(bytes, "ISO-8859-1");
			} catch (UnsupportedEncodingException e1) {
				// This should never be reached.
				return new String(bytes);
			}
		}
	}
}
