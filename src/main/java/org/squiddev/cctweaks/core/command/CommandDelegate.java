package org.squiddev.cctweaks.core.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * {@link net.minecraft.command.ICommand} which delegates to a {@link ISubCommand}.
 */
public class CommandDelegate extends CommandBase {
	private final ISubCommand command;

	public CommandDelegate(ISubCommand command) {
		this.command = command;
	}

	@Nonnull
	@Override
	public String getCommandName() {
		return command.getName();
	}

	@Nonnull
	@Override
	public String getCommandUsage(@Nonnull ICommandSender sender) {
		return command.getUsage();
	}

	@Nonnull
	@Override
	public List<String> getCommandAliases() {
		return Collections.emptyList();
	}

	@Override
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
		try {
			command.execute(server, sender, new CommandContext(command), Arrays.asList(args));
		} catch (CommandException e) {
			throw e;
		} catch (Throwable e) {
			DebugLogger.debug("Unhandled exception in command", e);
			throw new CommandException("Unhandled exception: " + e.toString());
		}
	}

	@Nonnull
	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
		return command.getCompletion(server, sender, Arrays.asList(args));
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		if (command.requiresAdmin() || server.isDedicatedServer()) {
			return super.checkPermission(server, sender);
		} else {
			return true;
		}
	}


	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}
}
