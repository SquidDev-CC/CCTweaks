package org.squiddev.cctweaks.core.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public abstract class SubCommandBase implements ISubCommand {
	private final String name;
	private final String usage;
	private final String synopsis;
	private final String description;
	private final boolean requiresAdmin;

	public SubCommandBase(String name, String usage, String synopsis, boolean requiresAdmin, String description) {
		this.name = name;
		this.usage = usage;
		this.synopsis = synopsis;
		this.description = description;
		this.requiresAdmin = requiresAdmin;
	}

	public SubCommandBase(String name, String usage, String synopsis, String description) {
		this(name, usage, synopsis, true, description);
	}

	public SubCommandBase(String name, String synopsis, String description) {
		this(name, "", synopsis, true, description);
	}

	@Nonnull
	@Override
	public String getName() {
		return name;
	}

	@Nonnull
	@Override
	public String getUsage() {
		return usage;
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
		return requiresAdmin;
	}

	@Nonnull
	@Override
	public List<String> getCompletion(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull List<String> arguments) {
		return Collections.emptyList();
	}
}
