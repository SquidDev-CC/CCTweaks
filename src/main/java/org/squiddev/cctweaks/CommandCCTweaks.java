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
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.*;
import org.apache.commons.lang3.StringUtils;
import org.squiddev.cctweaks.core.utils.ComputerAccessor;
import org.squiddev.cctweaks.lua.Config;
import org.squiddev.cctweaks.lua.lib.ComputerMonitor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CommandCCTweaks extends CommandBase {
	private static final IChatComponent SEPARATOR = new ChatComponentText(" | ");

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
		SEPARATOR.getChatStyle().setColor(EnumChatFormatting.GRAY);
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
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 1 || Strings.isNullOrEmpty(args[0])) throw new CommandException(getCommandUsage(sender));

		String command = args[0];
		if (command.equals("start")) {
			if (!Config.Computer.MultiThreading.enabled) {
				throw new CommandException("You must enable multi-threading to use the computer monitor");
			}

			try {
				ComputerMonitor.start();
				sender.addChatMessage(new ChatComponentText("Run '/" + getCommandName() + " stop' to stop monitoring and view results"));
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

				sender.addChatMessage(new ChatComponentText("")
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
					IChatComponent position;
					if (serverComputer == null) {
						position = fixed("?", 19);
					} else {
						position = link(fixed(formatPosition(serverComputer.getPosition()), 19), serverComputer.getPosition(), sender);
					}

					sender.addChatMessage(new ChatComponentText("")
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
			sender.addChatMessage(new ChatComponentText("")
				.appendSibling(header("Istn", 5))
				.appendSibling(SEPARATOR)
				.appendSibling(header("Id", 3))
				.appendSibling(SEPARATOR)
				.appendSibling(header("On", 2))
				.appendSibling(SEPARATOR)
				.appendSibling(header("Position", 30))
			);

			for (ServerComputer computer : ComputerCraft.serverComputerRegistry.getComputers()) {
				sender.addChatMessage(new ChatComponentText("")
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
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
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
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return !restricted || super.canCommandSenderUseCommand(sender);
	}

	private static String formatPosition(BlockPos pos) {
		return String.format("%d, %d, %d", pos.getX(), pos.getY(), pos.getZ());
	}

	private static IChatComponent formatBool(boolean value, int l) {
		if (value) {
			IChatComponent component = new ChatComponentText("Y" + StringUtils.repeat(' ', l - 1));
			component.getChatStyle().setColor(EnumChatFormatting.GREEN);
			return component;
		} else {
			IChatComponent component = new ChatComponentText("N" + StringUtils.repeat(' ', l - 1));
			component.getChatStyle().setColor(EnumChatFormatting.RED);
			return component;
		}
	}

	private static IChatComponent formatted(String format, Object... args) {
		return new ChatComponentText(String.format(format, args));
	}

	private static IChatComponent link(IChatComponent component, BlockPos pos, ICommandSender sender) {
		if (!sender.canCommandSenderUseCommand(2, "tp")) return component;

		ChatStyle style = component.getChatStyle();

		style.setColor(EnumChatFormatting.YELLOW);
		style.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
			"/tp " + pos.getX() + " " + pos.getY() + " " + pos.getZ()
		));
		style.setChatHoverEvent(new HoverEvent(
			HoverEvent.Action.SHOW_TEXT,
			new ChatComponentText("Click to teleport to this location")
		));

		return component;
	}

	private static IChatComponent fixed(String text, int length) {
		if (text.length() < length) {
			return new ChatComponentText(text + StringUtils.repeat(' ', length - text.length()));
		} else {
			IChatComponent component = new ChatComponentText(text.substring(0, length - 1) + "…");
			component.getChatStyle().setChatHoverEvent(new HoverEvent(
				HoverEvent.Action.SHOW_TEXT,
				new ChatComponentText(text)
			));
			return component;
		}
	}

	private static IChatComponent header(String text, int length) {
		IChatComponent component = new ChatComponentText(text + StringUtils.repeat(' ', length - text.length()));
		component.getChatStyle().setColor(EnumChatFormatting.LIGHT_PURPLE);
		return component;
	}
}
