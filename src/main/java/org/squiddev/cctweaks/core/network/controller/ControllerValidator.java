package org.squiddev.cctweaks.core.network.controller;

import dan200.computercraft.api.peripheral.IPeripheral;
import joptsimple.internal.Strings;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Validates {@link NetworkController} instances.
 *
 * This shouldn't be used in production, but is helpful when testing.
 */
public class ControllerValidator {
	public static void validate(NetworkController controller) {
		List<String> errors = new ArrayList<String>();

		for (Map.Entry<INetworkNode, Point> entry : controller.points.entrySet()) {
			Point point = entry.getValue();

			if (point.node != entry.getKey()) {
				errors.add(String.format("Point node: %s != %s", point.node, entry.getKey()));
			}

			if (point.controller != controller) {
				errors.add(String.format("Controller for point %s: %s != %s", point, point.controller, controller));
			}

			if (point.node.getAttachedNetwork() != controller) {
				errors.add(String.format("Controller for node %s: %s != %s", point.node, point.node.getAttachedNetwork(), controller));
			}

			for (Map.Entry<String, IPeripheral> peripheral : point.peripherals.entrySet()) {
				IPeripheral other = controller.peripheralsOnNetwork.get(peripheral.getKey());

				if (other == null || !peripheral.getValue().equals(other)) {
					errors.add(String.format("Peripherals for node %s (%s): %s != %s", point.node, peripheral.getKey(), peripheral.getValue(), other));
				}
			}

			for (Point.Connection connection : point.connections) {
				if (!connection.other(point).connections.contains(connection)) {
					errors.add(String.format("One way connection for %s and %s", point, connection.other(point)));
				}
			}
		}

		if (errors.size() > 0) {
			DebugLogger.trace("Controller is invalid:\n - " + Strings.join(errors, "\n - "));
		}
	}
}
