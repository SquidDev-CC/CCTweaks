package org.squiddev.cctweaks.core.command;

import com.google.common.base.Strings;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.*;

/**
 * Various helpers for building chat messages
 */
public final class ChatHelpers {
	private static final EnumChatFormatting HEADER = EnumChatFormatting.LIGHT_PURPLE;
	private static final EnumChatFormatting SYNOPSIS = EnumChatFormatting.AQUA;
	private static final EnumChatFormatting NAME = EnumChatFormatting.GREEN;

	public static IChatComponent coloured(String text, EnumChatFormatting colour) {
		IChatComponent component = new ChatComponentText(text == null ? "" : text);
		component.getChatStyle().setColor(colour);
		return component;
	}

	public static IChatComponent text(String text) {
		return new ChatComponentText(text == null ? "" : text);
	}

	public static IChatComponent list(IChatComponent... children) {
		IChatComponent component = new ChatComponentText("");
		for (IChatComponent child : children) {
			component.appendSibling(child);
		}
		return component;
	}

	public static IChatComponent getHelp(ISubCommand command, String prefix) {
		IChatComponent output = new ChatComponentText("")
			.appendSibling(coloured(command.getName() + " " + command.getUsage(), HEADER))
			.appendText(" ")
			.appendSibling(coloured(command.getSynopsis(), SYNOPSIS));

		String desc = command.getDescription();
		if (!Strings.isNullOrEmpty(desc)) output.appendText("\n" + desc);

		if (command instanceof CommandRoot) {
			for (ISubCommand subCommand : ((CommandRoot) command).getSubCommands().values()) {
				output.appendText("\n");

				IChatComponent component = coloured(subCommand.getName(), NAME);
				component.getChatStyle().setChatClickEvent(new ClickEvent(
					ClickEvent.Action.SUGGEST_COMMAND,
					"/" + prefix + " " + subCommand.getName()
				));
				output.appendSibling(component);

				output.appendText(" - " + subCommand.getSynopsis());
			}
		}

		return output;
	}

	public static IChatComponent position(BlockPos pos) {
		if (pos == null) return text("<no pos>");
		return formatted("%d, %d, %d", pos.getX(), pos.getY(), pos.getZ());
	}

	public static IChatComponent bool(boolean value) {
		if (value) {
			IChatComponent component = new ChatComponentText("Y");
			component.getChatStyle().setColor(EnumChatFormatting.GREEN);
			return component;
		} else {
			IChatComponent component = new ChatComponentText("N");
			component.getChatStyle().setColor(EnumChatFormatting.RED);
			return component;
		}
	}

	public static IChatComponent formatted(String format, Object... args) {
		return new ChatComponentText(String.format(format, args));
	}

	public static IChatComponent link(IChatComponent component, String command, String toolTip) {
		ChatStyle style = component.getChatStyle();

		if (style.getColor() == null) style.setColor(EnumChatFormatting.YELLOW);
		style.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
		style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(toolTip)));

		return component;
	}

	public static IChatComponent header(String text) {
		return coloured(text, HEADER);
	}
}
