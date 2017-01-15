package org.squiddev.cctweaks.core.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import scala.actors.threadpool.Arrays;

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
		return command.getUsage();
	}

	@Override
	public List<String> getCommandAliases() {
		return Collections.emptyList();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		try {
			command.execute(server, sender, new CommandContext(command), Arrays.asList(args));
		} catch (CommandException e) {
			throw e;
		} catch (Throwable e) {
			DebugLogger.debug("Unhandled exception in command", e);
			throw new CommandException("Unhandled exception: " + e.toString());
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		return command.getCompletion(server, sender, Arrays.asList(args));
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		if (command.requiresAdmin() || server.isDedicatedServer()) {
			return super.canCommandSenderUseCommand(sender);
		} else {
			return true;
		}
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}
}
