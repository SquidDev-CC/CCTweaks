package org.squiddev.cctweaks.core.network;

import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for remote peripherals
 */
public class PeripheralAccess implements IComputerAccess {
	protected final IPeripheral peripheral;
	protected final IComputerAccess computer;
	protected final String name;

	protected final String[] methods;
	protected final Map<String, Integer> methodMap;

	public PeripheralAccess(IPeripheral peripheral, IComputerAccess computer, String name) {
		this.peripheral = peripheral;
		this.computer = computer;
		this.name = name;

		String[] methods = peripheral.getMethodNames();
		if (peripheral.getType() == null || methods == null) {
			throw new RuntimeException("Peripheral " + peripheral + " provides no name or methods");
		}

		this.methods = methods;
		Map<String, Integer> map = methodMap = new HashMap<String, Integer>();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i] != null) map.put(methods[i], i);
		}
	}

	public void attach() {
		peripheral.attach(this);
		computer.queueEvent("peripheral", new Object[]{getAttachmentName()});
	}

	public void detach() {
		peripheral.detach(this);
		computer.queueEvent("peripheral_detach", new Object[]{getAttachmentName()});
	}

	public String getType() {
		return peripheral.getType();
	}

	public String[] getMethodNames() {
		return methods;
	}

	public Object[] callMethod(ILuaContext context, String methodName, Object[] arguments) throws InterruptedException, LuaException {
		Integer method = methodMap.get(methodName);
		if (method != null) return peripheral.callMethod(this, context, method, arguments);

		throw new LuaException("No such method " + methodName);
	}

	public String mount(String desiredLocation, IMount mount) {
		return computer.mount(desiredLocation, mount, name);
	}

	public String mount(String desiredLocation, IMount mount, String driveName) {
		return computer.mount(desiredLocation, mount, driveName);
	}

	public String mountWritable(String desiredLocation, IWritableMount mount) {
		return computer.mountWritable(desiredLocation, mount, name);
	}

	public String mountWritable(String desiredLocation, IWritableMount mount, String driveName) {
		return computer.mountWritable(desiredLocation, mount, driveName);
	}

	public void unmount(String location) {
		computer.unmount(location);
	}

	public int getID() {
		return computer.getID();
	}

	public void queueEvent(String event, Object[] arguments) {
		computer.queueEvent(event, arguments);
	}

	public String getAttachmentName() {
		return name;
	}
}
