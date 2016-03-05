package org.squiddev.cctweaks.core.lua;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Converter for binary values
 */
public class BinaryConverter {
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
