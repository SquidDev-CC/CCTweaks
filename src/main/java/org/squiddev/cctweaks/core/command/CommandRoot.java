package org.squiddev.cctweaks.core.command;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A command which delegates to a series of sub commands
 */
public class CommandRoot implements ISubCommand {
	private final String name;
	private final String synopsis;
	private final String description;
	private final Map<String, ISubCommand> subCommands = Maps.newHashMap();

	public CommandRoot(String name, String synopsis, String description) {
		this.name = name;
		this.synopsis = synopsis;
		this.description = description;

		register(new SubCommandHelp(this));
	}

	public void register(ISubCommand command) {
		subCommands.put(command.getName(), command);
	}

	@Nonnull
	@Override
	public String getName() {
		return name;
	}

	@Nonnull
	@Override
	public String getUsage() {
		return "<" + Joiner.on('|').join(subCommands.keySet()) + ">";
	}

	@Nonnull
	@Override
	public String getSynopsis() {
		return synopsis;
	}

	@Nonnull
	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean requiresAdmin() {
		for (ISubCommand command : subCommands.values()) {
			if (command instanceof SubCommandHelp) continue;
			if (!command.requiresAdmin()) return false;
		}
		return true;
	}

	public Map<String, ISubCommand> getSubCommands() {
		return Collections.unmodifiableMap(subCommands);
	}

	@Override
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull CommandContext context, @Nonnull List<String> arguments) throws CommandException {
		if (arguments.size() == 0) {
			sender.sendMessage(ChatHelpers.getHelp(this, context.getFullPath()));
		} else {
			ISubCommand command = subCommands.get(arguments.get(0));
			if (command == null) throw new CommandException(getName() + " " + getUsage());

			command.execute(server, sender, context.enter(command), arguments.subList(1, arguments.size()));
		}
	}

	@Nonnull
	@Override
	public List<String> getCompletion(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull List<String> arguments) {
		if (arguments.size() == 0) {
			return Lists.newArrayList(subCommands.keySet());
		} else if (arguments.size() == 1) {
			List<String> list = Lists.newArrayList();
			String match = arguments.get(0);

			for (String entry : subCommands.keySet()) {
				if (CommandBase.doesStringStartWith(match, entry)) {
					list.add(entry);
				}
			}

			return list;
		} else {
			ISubCommand command = subCommands.get(arguments.get(0));
			if (command == null) return Collections.emptyList();

			return command.getCompletion(server, sender, arguments.subList(1, arguments.size()));
		}
	}
}
