package org.squiddev.cctweaks.core.network;

import org.apache.commons.lang3.StringUtils;
import org.squiddev.cctweaks.api.network.ISearchLoc;
import org.squiddev.cctweaks.api.network.NetworkAPI;
import org.squiddev.cctweaks.core.network.mock.BasicNetwork;

/**
 * Various network utils
 */
public class NetworkTestUtils {
	/**
	 * Returns a string representing the order we visit in
	 *
	 * <pre>
	 *  1  3  5  7  9
	 *  2          11
	 *  4  6  8 10 12
	 * </pre>
	 *
	 * @param network The network to visit
	 * @return The visit order
	 */
	public static String getVisitOrder(BasicNetwork network) {
		int[][] map = new int[network.height][];

		int counter = 0;
		for (ISearchLoc loc : NetworkAPI.visitor().visitNetwork(network, 0, 0, 0)) {
			int[] row = map[loc.getZ()];
			if (row == null) map[loc.getZ()] = row = new int[network.width];
			row[loc.getX()] = ++counter;
		}

		int max = Integer.toString(counter - 1).length() + 1;
		String format = "%" + (max - 1) + "d ";
		String blank = StringUtils.repeat(' ', max);
		StringBuilder builder = new StringBuilder(network.width * network.height * (max + 1));

		for (int[] row : map) {
			if (row != null) {
				for (int item : row) {
					if (item <= 0) {
						builder.append(blank);
					} else {
						builder.append(String.format(format, item));
					}
				}
			}
			builder.append("\n");
		}

		return builder.toString();
	}
}
