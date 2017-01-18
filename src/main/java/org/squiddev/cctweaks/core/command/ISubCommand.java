package org.squiddev.cctweaks.core.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A slightly different implementation of {@link net.minecraft.command.ICommand} which is delegated to.
 */
public interface ISubCommand {
	/**
	 * Get the name of this command
	 *
	 * @return The name of this command
	 */
	@Nonnull
	String getName();

	/**
	 * Get the usage of this command
	 *
	 * @return The usage of this command
	 */
	@Nonnull
	String getUsage();

	/**
	 * Get a short description of this command, including its usage.
	 *
	 * @return The command's synopsis
	 */
	@Nonnull
	String getSynopsis();

	/**
	 * Get the lengthy description of this command. This synopsis is prepended to this.
	 *
	 * @return The command's description
	 */
	@Nonnull
	String getDescription();

	/**
	 * If this requires an admin to execute
	 *
	 * @return If this requires an admin.
	 */
	boolean requiresAdmin();

	/**
	 * Execute this command
	 *
	 * @param server    The server we are executing under
	 * @param sender    The thing which executed this command
	 * @param context   The path taken to this command
	 * @param arguments The arguments passed  @throws CommandException When an error occurs
	 */
	void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull CommandContext context, @Nonnull List<String> arguments) throws CommandException;

	/**
	 * Get a list of possible completions
	 *
	 * @param server    The server we are executing under
	 * @param sender    The thing which executed this command
	 * @param arguments The arguments passed. You should complete the last one.
	 * @return List of possible completions
	 */
	@Nonnull
	List<String> getCompletion(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull List<String> arguments);
}
