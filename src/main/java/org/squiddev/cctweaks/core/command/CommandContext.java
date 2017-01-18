package org.squiddev.cctweaks.core.command;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

public final class CommandContext {
	private final List<ISubCommand> path;

	public CommandContext(ISubCommand initial) {
		path = Collections.singletonList(initial);
	}

	private CommandContext(List<ISubCommand> path) {
		this.path = path;
	}

	public CommandContext enter(ISubCommand child) {
		List<ISubCommand> newPath = Lists.newArrayListWithExpectedSize(path.size() + 1);
		newPath.addAll(path);
		newPath.add(child);
		return new CommandContext(newPath);
	}

	public CommandContext parent() {
		if (path.size() == 1) throw new IllegalStateException("No parent command");
		return new CommandContext(path.subList(0, path.size() - 1));
	}

	public String getFullPath() {
		StringBuilder out = new StringBuilder();
		boolean first = true;
		for (ISubCommand command : path) {
			if (first) {
				first = false;
			} else {
				out.append(' ');
			}

			out.append(command.getName());
		}

		return out.toString();
	}

	public String getFullUsage() {
		return "/" + getFullPath() + " " + path.get(path.size() - 1).getUsage();
	}

	public List<ISubCommand> getPath() {
		return Collections.unmodifiableList(path);
	}
}
