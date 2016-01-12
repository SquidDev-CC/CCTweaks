/**
 * Copyright (c) 2013-2015 Florian "Sangar" Nücke
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.squiddev.cctweaks.core.lua.socket;

import com.google.common.net.InetAddresses;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Whitelist/blacklist for addresses
 */
public class AddressMatcher {
	private static final Pattern cidrPattern = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})(?:/(\\d{1,2}))");

	private final HashSet<String> hosts = new HashSet<String>();
	private final HashSet<InetAddress> addresses = new HashSet<InetAddress>();
	private final ArrayList<HostRange> ranges = new ArrayList<HostRange>();

	public final boolean active;

	private static class HostRange {
		private final int min;
		private final int max;

		private HostRange(int min, int max) {
			this.min = min;
			this.max = max;
		}
	}


	public AddressMatcher(String[] matches) {
		boolean active = false;
		for (String host : matches) {
			try {
				Matcher matcher = cidrPattern.matcher(host);
				if (matcher.matches()) {
					String address = matcher.group(1), prefix = matcher.group(2);

					int addr = InetAddresses.coerceToInteger(InetAddresses.forString(address));
					int mask = 0xFFFFFFFF << (32 - Integer.parseInt(prefix));
					int min = addr & mask;
					int max = addr | ~mask;

					active = true;
					ranges.add(new HostRange(min, max));
				} else {
					active = true;
					hosts.add(host);
					addresses.add(InetAddress.getByName(host));
				}
			} catch (Exception e) {
				DebugLogger.error("Error adding " + host + " to blacklist/whitelist", e);
			}
		}

		this.active = active;
	}

	public boolean matches(InetAddress address, String host) {
		if (hosts.contains(host)) return true;
		if (addresses.contains(address)) return true;

		// Presume true for IPv6 addresses
		if (!(address instanceof Inet4Address)) return true;

		int value = InetAddresses.coerceToInteger(address);
		for (HostRange range : ranges) {
			if (value >= range.min && value <= range.max) return true;
		}

		return false;
	}
}
