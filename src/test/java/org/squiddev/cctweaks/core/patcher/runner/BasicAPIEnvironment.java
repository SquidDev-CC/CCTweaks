package org.squiddev.cctweaks.core.patcher.runner;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.core.apis.IAPIEnvironment;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.computer.IComputerEnvironment;
import dan200.computercraft.core.filesystem.FileSystem;
import dan200.computercraft.core.filesystem.FileSystemException;
import dan200.computercraft.core.terminal.Terminal;

public class BasicAPIEnvironment implements IAPIEnvironment {
	private final IComputerEnvironment environment;
	private final FileSystem system;

	public BasicAPIEnvironment() {
		this(new BasicEnvironment());
	}

	public BasicAPIEnvironment(IComputerEnvironment environment) {
		this.environment = environment;
		try {
			system = new FileSystem("hdd", environment.createSaveDirMount("hdd", 1000000L));
			system.mount("rom", "rom", environment.createResourceMount("computercraft", "lua/rom"));
		} catch (FileSystemException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Computer getComputer() {
		return null;
	}

	@Override
	public int getComputerID() {
		return 0;
	}

	@Override
	public IComputerEnvironment getComputerEnvironment() {
		return environment;
	}

	@Override
	public Terminal getTerminal() {
		return null;
	}

	@Override
	public FileSystem getFileSystem() {
		return system;
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void reboot() {
	}

	@Override
	public void queueEvent(String paramString, Object[] paramArrayOfObject) {

	}

	@Override
	public void setOutput(int paramInt1, int paramInt2) {
	}

	@Override
	public int getOutput(int paramInt) {
		return 0;
	}

	@Override
	public int getInput(int paramInt) {
		return 0;
	}

	@Override
	public void setBundledOutput(int paramInt1, int paramInt2) {

	}

	@Override
	public int getBundledOutput(int paramInt) {
		return 0;
	}

	@Override
	public int getBundledInput(int paramInt) {
		return 0;
	}

	@Override
	public void setPeripheralChangeListener(IPeripheralChangeListener paramIPeripheralChangeListener) {
	}

	@Override
	public IPeripheral getPeripheral(int side) {
		return null;
	}

	@Override
	public String getLabel() {
		return null;
	}

	@Override
	public void setLabel(String paramString) {

	}
}
