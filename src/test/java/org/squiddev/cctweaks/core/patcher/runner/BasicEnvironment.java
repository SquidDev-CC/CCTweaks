package org.squiddev.cctweaks.core.patcher.runner;

import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.core.computer.IComputerEnvironment;
import dan200.computercraft.core.filesystem.JarMount;
import org.squiddev.cctweaks.core.patcher.utils.MountHelpers;

import java.io.IOException;

/**
 * A very basic environment
 */
public class BasicEnvironment implements IComputerEnvironment {
	private final IWritableMount mount;

	public BasicEnvironment() {
		this(new MemoryMount());
	}


	public BasicEnvironment(IWritableMount mount) {
		this.mount = mount;
	}

	@Override
	public IWritableMount createSaveDirMount(String s, long l) {
		return mount;
	}

	@Override
	public int getDay() {
		return 0;
	}

	@Override
	public double getTimeOfDay() {
		return 0;
	}

	@Override
	public boolean isColour() {
		return true;
	}

	@Override
	public long getComputerSpaceLimit() {
		return 1000000L;
	}

	@Override
	public int assignNewID() {
		return 0;
	}

	@Override
	public String getHostString() {
		return "CCTweaks test runner";
	}

	@Override
	public IMount createResourceMount(String domain, String subPath) {
		subPath = "assets/" + domain + "/" + subPath;

		try {
			return new JarMount(MountHelpers.mainJar, subPath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
