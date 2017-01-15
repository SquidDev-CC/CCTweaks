package org.squiddev.cctweaks.command;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameData;
import org.squiddev.cctweaks.api.IComputerItemFactory;
import org.squiddev.cctweaks.core.command.CommandContext;
import org.squiddev.cctweaks.core.command.SubCommandBase;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Special command used to give an item to the user.
 */
public class SubCommandGive extends SubCommandBase {
	private Map<ResourceLocation, IComputerItemFactory> factories;

	private Map<ResourceLocation, IComputerItemFactory> getFactories() {
		if (factories != null) return factories;

		synchronized (this) {
			if (factories != null) return factories;

			factories = Maps.newHashMap();
			for (Item item : GameData.getItemRegistry().typeSafeIterable()) {
				if (item instanceof IComputerItemFactory) {
					IComputerItemFactory factory = (IComputerItemFactory) item;
					if (factory.getSupportedFamilies().size() > 0) {
						factories.put(item.delegate.getResourceName(), factory);
					}
				}
			}

			return factories;
		}
	}

	public SubCommandGive() {
		super(
			"give", "<item> <id> [label] [family]", "Spawn in a computer item with the specified id.",
			"You can optionally specify the label and computer family of the item. Valid families are normal, " +
				"advanced, and command."
		);
	}


	@Override
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull CommandContext context, @Nonnull List<String> arguments) throws CommandException {
		if (arguments.size() < 2) throw new CommandException(context.getFullUsage());

		if (!(sender instanceof EntityPlayer)) throw new CommandException("Must be executed by the player");

		ResourceLocation item = new ResourceLocation(arguments.get(0));
		IComputerItemFactory factory = getFactories().get(item);

		if (factory == null) throw new CommandException("Cannot create computer from " + item);

		int id;
		try {
			id = Integer.parseInt(arguments.get(1));
		} catch (NumberFormatException e) {
			throw new CommandException("'" + arguments.get(1) + "' is not a number");
		}

		if (id < 0) throw new CommandException("Id must be >= 0");

		String label = arguments.size() > 2 ? arguments.get(2) : null;
		if (Strings.isNullOrEmpty(label)) label = null;

		String familyName = arguments.size() > 3 ? arguments.get(3) : null;
		ComputerFamily family = null;
		if (familyName == null) {
			family = factory.getDefaultFamily();
		} else {
			for (ComputerFamily option : ComputerFamily.values()) {
				if (option.name().equalsIgnoreCase(familyName)) {
					family = option;
					break;
				}
			}

			if (family == null) {
				throw new CommandException("Unknown family '" + familyName + "'.");
			} else if (!factory.getSupportedFamilies().contains(family)) {
				throw new CommandException("Family is not supported by " + item + ".");
			}
		}

		ItemStack stack = factory.createComputer(id, label, family);

		EntityPlayer player = (EntityPlayer) sender;
		boolean ok = player.inventory.addItemStackToInventory(stack);
		if (ok) {
			player.worldObj.playSoundAtEntity(player, "random.pop", 0.2F, ((player.getRNG().nextFloat() - player.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
			player.inventoryContainer.detectAndSendChanges();
		}

		if (ok && stack.stackSize <= 0) {
			stack.stackSize = 1;
			EntityItem entity = player.dropPlayerItemWithRandomChoice(stack, false);
			if (entity != null) entity.func_174870_v();
		} else {
			EntityItem entity = player.dropPlayerItemWithRandomChoice(stack, false);
			if (entity != null) {
				entity.setNoPickupDelay();
				entity.setOwner(player.getName());
			}
		}
	}

	@Nonnull
	@Override
	public List<String> getCompletion(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull List<String> arguments) {
		switch (arguments.size()) {
			default:
				return Collections.emptyList();
			case 1: {
				List<String> out = Lists.newArrayList();
				String search = arguments.get(0);
				for (ResourceLocation entry : getFactories().keySet()) {
					if (CommandBase.doesStringStartWith(search, entry.toString())) {
						out.add(entry.toString());
					}
				}
				return out;
			}
			case 4: {
				ResourceLocation location = new ResourceLocation(arguments.get(0));
				IComputerItemFactory factory = getFactories().get(location);
				if (factory == null) return Collections.emptyList();

				List<String> out = Lists.newArrayList();
				String search = arguments.get(3);
				for (ComputerFamily family : factory.getSupportedFamilies()) {
					if (CommandBase.doesStringStartWith(search, family.name())) {
						out.add(family.name());
					}
				}
				return out;
			}
		}
	}
}
