package org.squiddev.cctweaks.core.lua;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Duplicate of {@link dan200.computercraft.core.lua.LuaJLuaMachine#toObject(LuaValue)} with
 * binary support
 */
public class LuaConverter {
	public static Object toObject(LuaValue value, boolean binary) {
		return toObject(value, null, binary);
	}

	private static Object toObject(LuaValue value, Map<LuaValue, Object> tables, boolean binary) {
		switch (value.type()) {
			case LuaValue.TNUMBER:
			case LuaValue.TINT:
				return value.todouble();
			case LuaValue.TBOOLEAN:
				return value.toboolean();
			case LuaValue.TSTRING: {
				if (binary) {
					LuaString string = (LuaString) value;
					byte[] result = new byte[string.m_length];
					System.arraycopy(string.m_bytes, string.m_offset, result, 0, string.m_length);
					return result;
				} else {
					return value.toString();
				}
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
					Object keyObject = toObject(k, tables, binary);
					Object valueObject = toObject(v, tables, binary);
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

	public static Object[] toObjects(Varargs values, int start, boolean binary) {
		int count = values.narg();
		Object[] objects = new Object[count - start + 1];
		for (int n = start; n <= count; n++) {
			int i = n - start;
			LuaValue value = values.arg(n);
			objects[i] = toObject(value, null, binary);
		}
		return objects;
	}

	public static Object toString(Object value) {
		return toString(value, null);
	}

	private static Object toString(Object value, Map<Object, Object> tables) {
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

			Map<?, ?> map = (Map) value;

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

	private static Charset CHARSET;

	public static Charset getCharset() {
		if (CHARSET == null) {
			try {
				CHARSET = Charset.forName("UTF8");
			} catch (UnsupportedCharsetException e) {
				try {
					// Fall back. Shouldn't happen, but you never know
					CHARSET = Charset.forName("ISO-8859-1");
				} catch (UnsupportedCharsetException e1) {
					CHARSET = Charset.defaultCharset();
				}
			}
		}

		return CHARSET;
	}

	public static String decodeString(byte[] bytes) {
		return new String(bytes, getCharset());
	}

	public static String decodeString(byte[] bytes, int offset, int length) {
		return new String(bytes, offset, length, getCharset());
	}

	public static byte[] toBytes(String string) {
		return string.getBytes(getCharset());
	}
}
