package org.squiddev.cctweaks;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.apache.commons.lang3.StringUtils;
import org.squiddev.cctweaks.core.utils.ComputerAccessor;
import org.squiddev.cctweaks.lua.Config;
import org.squiddev.cctweaks.lua.lib.ComputerMonitor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CommandCCTweaks extends CommandBase {
	private static final ITextComponent SEPARATOR = new TextComponentString(" | ");

	private static final Comparator<ComputerMonitor.ComputerEntry> comparator = new Comparator<ComputerMonitor.ComputerEntry>() {
		@Override
		public int compare(ComputerMonitor.ComputerEntry o1, ComputerMonitor.ComputerEntry o2) {
			long t1 = o1.getTime(), t2 = o2.getTime();

			if (t1 > t2) {
				return -1;
			} else if (t1 == t2) {
				return 0;
			} else {
				return 1;
			}
		}
	};

	static {
		SEPARATOR.getStyle().setColor(TextFormatting.GRAY);
	}

	private final boolean restricted;

	public CommandCCTweaks(boolean restricted) {
		this.restricted = restricted;
	}

	@Override
	public String getCommandName() {
		return "cctweaks";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "cctweaks [start|stop|dump]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 1 || Strings.isNullOrEmpty(args[0])) throw new CommandException(getCommandUsage(sender));

		String command = args[0];
		if (command.equals("start")) {
			if (!Config.Computer.MultiThreading.enabled) {
				throw new CommandException("You must enable multi-threading to use the computer monitor");
			}

			try {
				ComputerMonitor.start();
				sender.addChatMessage(new TextComponentString("Run '/" + getCommandName() + " stop' to stop monitoring and view results"));
			} catch (IllegalStateException e) {
				throw new CommandException(e.getMessage());
			} catch (Throwable e) {
				throw new CommandException(e.toString());
			}
		} else if (command.equals("stop")) {
			try {
				List<ComputerMonitor.ComputerEntry> entries = Lists.newArrayList(ComputerMonitor.stop().getEntries());
				Collections.sort(entries, comparator);

				if (entries.isEmpty()) {
					throw new CommandException("No computers were detected to run. Try '/" + getCommandName() + " dump' to see all existing computers");
				}

				Map<Computer, ServerComputer> lookup = Maps.newHashMap();
				for (ServerComputer serverComputer : ComputerCraft.serverComputerRegistry.getComputers()) {
					Computer computer = (Computer) ComputerAccessor.serverComputerComputer.get(serverComputer);

					lookup.put(computer, serverComputer);
				}

				sender.addChatMessage(new TextComponentString("")
					.appendSibling(header("Id", 3))
					.appendSibling(SEPARATOR)
					.appendSibling(header("Computer", 19))
					.appendSibling(SEPARATOR)
					.appendSibling(header("Tasks", 5))
					.appendSibling(SEPARATOR)
					.appendSibling(header("Total", 5))
					.appendSibling(SEPARATOR)
					.appendSibling(header("Mean", 6))
				);

				for (ComputerMonitor.ComputerEntry entry : entries) {
					Computer computer = entry.getComputer();
					ServerComputer serverComputer = lookup.get(computer);
					ITextComponent position;
					if (serverComputer == null) {
						position = fixed("?", 19);
					} else {
						position = link(fixed(formatPosition(serverComputer.getPosition()), 19), serverComputer.getPosition(), sender);
					}

					sender.addChatMessage(new TextComponentString("")
						.appendSibling(fixed(Integer.toString(computer.getID()), 3))
						.appendSibling(SEPARATOR)
						.appendSibling(position)
						.appendSibling(SEPARATOR)
						.appendSibling(formatted("%5d", entry.getTasks()))
						.appendSibling(SEPARATOR)
						.appendSibling(formatted("%5d", entry.getTime()))
						.appendSibling(SEPARATOR)
						.appendSibling(formatted("%4.1f", (double) entry.getTime() / entry.getTasks()))
					);
				}
			} catch (CommandException e) {
				throw e;
			} catch (IllegalStateException e) {
				throw new CommandException(e.getMessage());
			} catch (Throwable e) {
				throw new CommandException(e.toString());
			}
		} else if (command.equals("dump")) {
			sender.addChatMessage(new TextComponentString("")
				.appendSibling(header("Istn", 5))
				.appendSibling(SEPARATOR)
				.appendSibling(header("Id", 3))
				.appendSibling(SEPARATOR)
				.appendSibling(header("On", 2))
				.appendSibling(SEPARATOR)
				.appendSibling(header("Position", 30))
			);

			for (ServerComputer computer : ComputerCraft.serverComputerRegistry.getComputers()) {
				sender.addChatMessage(new TextComponentString("")
					.appendSibling(fixed(Integer.toString(computer.getInstanceID()), 5))
					.appendSibling(SEPARATOR)
					.appendSibling(fixed(Integer.toString(computer.getID()), 3))
					.appendSibling(SEPARATOR)
					.appendSibling(formatBool(computer.isOn(), 2))
					.appendSibling(SEPARATOR)
					.appendSibling(link(fixed(formatPosition(computer.getPosition()), 30), computer.getPosition(), sender))
				);
			}
		} else {
			throw new CommandException("Unknown command '" + command + "'");
		}
	}

	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
		if (args.length == 1) {
			return getListOfStringsMatchingLastWord(args, "start", "stop", "dump");
		}

		return null;
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return !restricted || super.checkPermission(server, sender);
	}

	private static String formatPosition(BlockPos pos) {
		return String.format("%d, %d, %d", pos.getX(), pos.getY(), pos.getZ());
	}

	private static ITextComponent formatBool(boolean value, int l) {
		if (value) {
			ITextComponent component = new TextComponentString("Y" + StringUtils.repeat(' ', l - 1));
			component.getStyle().setColor(TextFormatting.GREEN);
			return component;
		} else {
			ITextComponent component = new TextComponentString("N" + StringUtils.repeat(' ', l - 1));
			component.getStyle().setColor(TextFormatting.RED);
			return component;
		}
	}

	private static ITextComponent formatted(String format, Object... args) {
		return new TextComponentString(String.format(format, args));
	}

	private static ITextComponent link(ITextComponent component, BlockPos pos, ICommandSender sender) {
		if (!sender.canCommandSenderUseCommand(2, "tp")) return component;

		Style style = component.getStyle();

		style.setColor(TextFormatting.YELLOW);
		style.setClickEvent(new ClickEvent(net.minecraft.util.text.event.ClickEvent.Action.RUN_COMMAND,
			"/tp " + pos.getX() + " " + pos.getY() + " " + pos.getZ()
		));
		style.setHoverEvent(new HoverEvent(
			net.minecraft.util.text.event.HoverEvent.Action.SHOW_TEXT,
			new TextComponentString("Click to teleport to this location")
		));

		return component;
	}

	private static ITextComponent fixed(String text, int length) {
		if (text.length() < length) {
			return new TextComponentString(text + StringUtils.repeat(' ', length - text.length()));
		} else {
			ITextComponent component = new TextComponentString(text.substring(0, length - 1) + "…");
			component.getStyle().setHoverEvent(new HoverEvent(
				HoverEvent.Action.SHOW_TEXT,
				new TextComponentString(text)
			));
			return component;
		}
	}

	private static ITextComponent header(String text, int length) {
		ITextComponent component = new TextComponentString(text + StringUtils.repeat(' ', length - text.length()));
		component.getStyle().setColor(TextFormatting.LIGHT_PURPLE);
		return component;
	}
}
