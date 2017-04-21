package org.squiddev.cctweaks.core.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * {@link net.minecraft.command.ICommand} which delegates to a {@link ISubCommand}.
 */
public class CommandDelegate extends CommandBase {
	private final ISubCommand command;
	private final MinecraftServer server;

	public CommandDelegate(MinecraftServer server, ISubCommand command) {
		this.command = command;
		this.server = server;
	}

	@Override
	public String getCommandName() {
		return command.getName();
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return command.getUsage(new CommandContext(server, sender, command));
	}

	@Override
	public List<String> getCommandAliases() {
		return Collections.emptyList();
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		try {
			command.execute(new CommandContext(server, sender, command), Arrays.asList(args));
		} catch (CommandException e) {
			throw e;
		} catch (Throwable e) {
			DebugLogger.debug("Unhandled exception in command", e);
			throw new CommandException("Unhandled exception: " + e.toString());
		}
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		return command.getCompletion(new CommandContext(server, sender, command), Arrays.asList(args));
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return command.userLevel().canExecute(new CommandContext(server, sender, command));
	}

	@Override
	public int getRequiredPermissionLevel() {
		return command.userLevel().toLevel();
	}
}
