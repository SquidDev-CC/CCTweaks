package org.squiddev.cctweaks.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import org.squiddev.cctweaks.GuiHandler;
import org.squiddev.cctweaks.core.command.*;
import org.squiddev.cctweaks.core.utils.ComputerAccessor;
import org.squiddev.cctweaks.core.utils.Helpers;
import org.squiddev.cctweaks.lua.Config;
import org.squiddev.cctweaks.lua.lib.ComputerMonitor;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.squiddev.cctweaks.core.command.ChatHelpers.*;

public final class CommandCCTweaks {
	private static final Comparator<ComputerMonitor.ComputerEntry> PROFILE_COMPARATOR = new Comparator<ComputerMonitor.ComputerEntry>() {
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

	private CommandCCTweaks() {
	}

	public static ICommand create(MinecraftServer server) {
		CommandRoot root = new CommandRoot(
			"cctweaks", "Various commands for CCTweaks.",
			"The CCTweaks command provides various debugging and administrator tools for controlling and interacting " +
				"with computers."
		);

		{
			CommandRoot profile = new CommandRoot(
				"profile", "Profile the CPU usage of computers.",
				"Monitor all computers on the server and counts the time they ran for, along with number of yields and " +
					"other useful information. This requires multi-threading to be enabled, though the thread count can be 1"
			);

			profile.register(new SubCommandBase(
				"start", "Start profiling all computers",
				"Start monitoring the the execution time for all computers. Fails if the profiler is already running."
			) {
				@Override
				public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull CommandContext context, @Nonnull List<String> arguments) throws CommandException {
					if (!Config.Computer.MultiThreading.enabled) {
						throw new CommandException("You must enable multi-threading to use the computer monitor");
					}

					ComputerMonitor.start();

					sender.addChatMessage(list(
						text("Run "),
						link(text("/cctweaks profile stop"), "/cctweaks profile stop", "Click to stop profiling"),
						text(" to stop profiling and view the results")
					));
				}
			});

			profile.register(new SubCommandBase(
				"stop", "Stop profiling all computers.",
				"Stop the monitoring of execution time and print the results. Fails if the profiler is not running."
			) {
				@Override
				public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull CommandContext context, @Nonnull List<String> arguments) throws CommandException {
					List<ComputerMonitor.ComputerEntry> entries = Lists.newArrayList(ComputerMonitor.stop().getEntries());
					Collections.sort(entries, PROFILE_COMPARATOR);

					if (entries.isEmpty()) {
						throw new CommandException("No computers were detected to run. Try '/cctweaks dump' to see all existing computers");
					}

					Map<Computer, ServerComputer> lookup = Maps.newHashMap();
					for (ServerComputer serverComputer : ComputerCraft.serverComputerRegistry.getComputers()) {
						Computer computer = ComputerAccessor.getField(ComputerAccessor.serverComputerComputer, serverComputer);
						lookup.put(computer, serverComputer);
					}

					TextTable table = new TextTable("Inst", "Id", "Computer", "Tasks", "Total", "Mean");

					for (ComputerMonitor.ComputerEntry entry : entries) {
						Computer computer = entry.getComputer();
						ServerComputer serverComputer = lookup.get(computer);
						table.addRow(
							serverComputer == null ? text("?") : linkComputer(serverComputer),
							text(Integer.toString(computer.getID())),
							serverComputer == null ? text("?") : linkPosition(serverComputer),
							formatted("%5d", entry.getTasks()),
							formatted("%5d", entry.getTime()),
							formatted("%4.1f", (double) entry.getTime() / entry.getTasks())
						);
					}

					table.displayTo(sender);
				}
			});

			root.register(profile);
		}

		root.register(new SubCommandBase(
			"dump", "[id]", "Display the status of computers.", false,
			"Display the status of all computers or specific information about one computer. You can either specify the computer's instance " +
				"id (e.g. 123) or computer id (e.g #123)."
		) {
			@Override
			public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull CommandContext context, @Nonnull List<String> arguments) throws CommandException {
				if (arguments.size() == 0) {
					TextTable table = new TextTable("Inst", "Id", "On", "Position");

					for (ServerComputer computer : ComputerCraft.serverComputerRegistry.getComputers()) {
						table.addRow(
							linkComputer(computer),
							text(Integer.toString(computer.getID())),
							bool(computer.isOn()),
							linkPosition(computer)
						);
					}

					table.displayTo(sender);
				} else if (arguments.size() == 1) {
					ServerComputer computer = ComputerSelector.getComputer(arguments.get(0));

					TextTable table = new TextTable();
					table.addRow(header("Instance"), text(Integer.toString(computer.getInstanceID())));
					table.addRow(header("Id"), text(Integer.toString(computer.getID())));
					table.addRow(header("Label"), text(computer.getLabel()));
					table.addRow(header("On"), bool(computer.isOn()));
					table.addRow(header("Position"), linkPosition(computer));
					table.addRow(header("Family"), text(Helpers.guessFamily(computer).toString()));

					for (int i = 0; i < 6; i++) {
						IPeripheral peripheral = computer.getPeripheral(i);
						if (peripheral != null) {
							table.addRow(header("Peripheral " + Computer.s_sideNames[i]), text(peripheral.getType()));
						}
					}

					table.displayTo(sender);
				} else {
					throw new CommandException(context.getFullUsage());
				}
			}

			@Nonnull
			@Override
			public List<String> getCompletion(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull List<String> arguments) {
				return arguments.size() == 1
					? ComputerSelector.completeComputer(arguments.get(0))
					: Collections.<String>emptyList();
			}
		});

		root.register(new SubCommandBase(
			"shutdown", "[ids...]", "Shutdown computers remotely.",
			"Shutdown the listed computers or all if none are specified. You can either specify the computer's instance " +
				"id (e.g. 123) or computer id (e.g #123)."
		) {
			@Override
			public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull CommandContext context, @Nonnull List<String> arguments) throws CommandException {
				List<ServerComputer> computers = Lists.newArrayList();
				if (arguments.size() > 0) {
					for (String arg : arguments) {
						computers.add(ComputerSelector.getComputer(arg));
					}
				} else {
					computers.addAll(ComputerCraft.serverComputerRegistry.getComputers());
				}

				int shutdown = 0;
				for (ServerComputer computer : computers) {
					if (computer.isOn()) shutdown++;
					computer.unload();
				}
				sender.addChatMessage(text("Shutdown " + shutdown + " / " + computers.size()));
			}

			@Nonnull
			@Override
			public List<String> getCompletion(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull List<String> arguments) {
				return arguments.size() == 0
					? Collections.<String>emptyList()
					: ComputerSelector.completeComputer(arguments.get(arguments.size() - 1));
			}
		});

		root.register(new SubCommandBase(
			"tp", "<id>", "Teleport to a specific computer.",
			"Teleport to the location of a computer. You can either specify the computer's instance " +
				"id (e.g. 123) or computer id (e.g #123)."
		) {
			@Override
			public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull CommandContext context, @Nonnull List<String> arguments) throws CommandException {
				if (arguments.size() != 1) throw new CommandException(context.getFullUsage());

				ServerComputer computer = ComputerSelector.getComputer(arguments.get(0));
				World world = computer.getWorld();
				BlockPos pos = computer.getPosition();

				if (world == null || pos == null) throw new CommandException("Cannot locate computer in world");

				if (!(sender instanceof Entity)) throw new CommandException("Sender is not an entity");

				if (sender instanceof EntityPlayerMP) {
					EntityPlayerMP entity = (EntityPlayerMP) sender;
					if (entity.getEntityWorld() != world) {
						server.getPlayerList().changePlayerDimension(entity, world.provider.getDimension());
					}

					entity.setPositionAndUpdate(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
				} else {
					Entity entity = (Entity) sender;
					if (entity.getEntityWorld() != world) {
						entity.changeDimension(world.provider.getDimension());
					}

					entity.setLocationAndAngles(
						pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
						entity.rotationYaw, entity.rotationPitch
					);
				}
			}

			@Nonnull
			@Override
			public List<String> getCompletion(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull List<String> arguments) {
				return arguments.size() == 1
					? ComputerSelector.completeComputer(arguments.get(0))
					: Collections.<String>emptyList();
			}
		});

		root.register(new SubCommandGive());

		root.register(new SubCommandBase(
			"view", "<id>", "View the terminal of a computer.",
			"Open the terminal of a computer, allowing remote control of a computer. This does not provide access to " +
				"turtle's inventories. You can either specify the computer's instance id (e.g. 123) or computer id (e.g #123)."
		) {
			@Override
			public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull CommandContext context, @Nonnull List<String> arguments) throws CommandException {
				if (arguments.size() != 1) throw new CommandException(context.getFullUsage());

				if (!(sender instanceof EntityPlayerMP)) {
					throw new CommandException("Cannot open terminal for non-player");
				}

				ServerComputer computer = ComputerSelector.getComputer(arguments.get(0));
				GuiHandler.openComputer((EntityPlayerMP) sender, computer);
			}

			@Nonnull
			@Override
			public List<String> getCompletion(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull List<String> arguments) {
				return arguments.size() == 1
					? ComputerSelector.completeComputer(arguments.get(0))
					: Collections.<String>emptyList();
			}
		});

		return new CommandDelegate(root);
	}

	private static ITextComponent linkComputer(ServerComputer computer) {
		return link(
			text(Integer.toString(computer.getInstanceID())),
			"/cctweaks dump " + computer.getInstanceID(),
			"View more info about this computer"
		);
	}

	private static ITextComponent linkPosition(ServerComputer computer) {
		return link(
			position(computer.getPosition()),
			"/cctweaks tp " + computer.getInstanceID(),
			"Teleport to this computer"
		);
	}
}
