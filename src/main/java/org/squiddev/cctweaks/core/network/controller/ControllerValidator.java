package org.squiddev.cctweaks.core.network.controller;

import com.google.common.collect.Sets;
import dan200.computercraft.api.peripheral.IPeripheral;
import joptsimple.internal.Strings;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validates {@link NetworkController} instances.
 *
 * This is helpful when testing.
 */
public class ControllerValidator {
	public static void validate(NetworkController controller) {
		List<String> errors = new ArrayList<String>();

		Set<IPeripheral> foundPeripherals = Sets.newHashSet();
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
				foundPeripherals.add(peripheral.getValue());

				if (other == null || !peripheral.getValue().equals(other)) {
					String error = String.format("Peripherals for node %s (%s): %s != %s", point.node, peripheral.getKey(), peripheral.getValue(), other);
					if (Config.Testing.extendedControllerValidation) {
						StringBuilder builder = new StringBuilder(error);

						for (Point otherPoint : controller.points.values()) {
							IPeripheral otherPeripheral = otherPoint.peripherals.get(peripheral.getKey());
							if (otherPeripheral != null) {
								builder.append(String.format("\n Found peripheral conflict: %s => %s", otherPoint.node, otherPeripheral));
							}
						}

						error = builder.toString();
					}

					errors.add(error);
				}
			}

			for (Point.Connection connection : point.connections) {
				if (!connection.other(point).connections.contains(connection)) {
					errors.add(String.format("One way connection for %s and %s", point, connection.other(point)));
				}
			}
		}

		for (Map.Entry<String, IPeripheral> peripheral : controller.getPeripheralsOnNetwork().entrySet()) {
			if (!foundPeripherals.contains(peripheral.getValue())) {
				errors.add(String.format("Peripheral not in any node %s => %s", peripheral.getKey(), peripheral.getValue()));
			}
		}


		if (errors.size() > 0) {
			trace("Controller is invalid:\n - " + Strings.join(errors, "\n - "));
		}
	}

	public static void trace(String message) {
		if (Config.Testing.extendedControllerValidation) {
			DebugLogger.trace(message);
		} else {
			DebugLogger.debug(message);
		}
	}
}
