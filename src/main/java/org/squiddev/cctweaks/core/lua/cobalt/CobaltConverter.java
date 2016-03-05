package org.squiddev.cctweaks.core.lua.cobalt;

import org.squiddev.cctweaks.core.lua.luaj.LuaJConverter;
import org.squiddev.cobalt.LuaString;
import org.squiddev.cobalt.LuaValue;
import org.squiddev.cobalt.Varargs;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import static org.squiddev.cobalt.Constants.*;

/**
 * Duplicate of {@link LuaJConverter} but for cobalt
 */
public class CobaltConverter {
	public static Object toObject(LuaValue value, boolean binary) {
		return toObject(value, null, binary);
	}

	private static Object toObject(LuaValue value, Map<LuaValue, Object> tables, boolean binary) {
		switch (value.type()) {
			case TNUMBER:
			case TINT:
				return value.toDouble();
			case TBOOLEAN:
				return value.toBoolean();
			case TSTRING: {
				if (binary) {
					LuaString string = (LuaString) value;
					if (string.offset == 0 && string.length == string.bytes.length) {
						return string.bytes;
					} else {
						byte[] result = new byte[string.length];
						System.arraycopy(string.bytes, string.offset, result, 0, string.length);
						return result;
					}
				} else {
					return value.toString();
				}
			}
			case TTABLE: {
				if (tables == null) {
					tables = new IdentityHashMap<LuaValue, Object>();
				} else {
					Object object = tables.get(value);
					if (object != null) return object;
				}

				Map<Object, Object> table = new HashMap<Object, Object>();
				tables.put(value, table);

				LuaValue k = NIL;
				while (true) {
					Varargs keyValue = value.next(k);
					k = keyValue.first();
					if (k.isNil()) break;

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
		int count = values.count();
		Object[] objects = new Object[count - start + 1];
		for (int n = start; n <= count; n++) {
			int i = n - start;
			LuaValue value = values.arg(n);
			objects[i] = toObject(value, null, binary);
		}
		return objects;
	}
}
