package org.squiddev.cctweaks.core.command;

import net.minecraft.command.CommandException;

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
	 * @param context
	 */
	@Nonnull
	String getUsage(CommandContext context);

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
	 * Determine the level this command requires.
	 *
	 * @return The user level the player must have in order to execute it.
	 */
	UserLevel userLevel();

	/**
	 * Execute this command
	 *
	 * @param context   The current command context.
	 * @param arguments The arguments passed  @throws CommandException When an error occurs
	 */
	void execute(@Nonnull CommandContext context, @Nonnull List<String> arguments) throws CommandException;

	/**
	 * Get a list of possible completions
	 *
	 * @param context   The current command context.
	 * @param arguments The arguments passed. You should complete the last one.
	 * @return List of possible completions
	 */
	@Nonnull
	List<String> getCompletion(@Nonnull CommandContext context, @Nonnull List<String> arguments);
}
