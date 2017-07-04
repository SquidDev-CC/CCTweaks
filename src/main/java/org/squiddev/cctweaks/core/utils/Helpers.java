package org.squiddev.cctweaks.core.utils;

import com.google.common.base.CaseFormat;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.util.IDAssigner;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.cctweaks.CCTweaks;

import javax.annotation.Nullable;
import java.io.File;

/**
 * Helper methods for various things
 */
public class Helpers {
	/**
	 * Translate any variant of a string
	 *
	 * @param strings The strings to try to translate
	 * @return The first translateable string
	 */
	public static String translateAny(String... strings) {
		return translateOrDefault(strings[strings.length - 1], strings);
	}

	/**
	 * Translate any variant of a string
	 *
	 * @param def     The fallback string
	 * @param strings The strings to try to translate
	 * @return The first translateable string or the default
	 */
	@SuppressWarnings("deprecation")
	public static String translateOrDefault(String def, String... strings) {
		for (String string : strings) {
			if (net.minecraft.util.text.translation.I18n.canTranslate(string)) {
				return net.minecraft.util.text.translation.I18n.translateToLocal(string);
			}
		}

		return def;
	}

	@SuppressWarnings("deprecation")
	public static String translateToLocal(String str) {
		return net.minecraft.util.text.translation.I18n.translateToLocal(str);
	}

	public static int nextId(World world, String type) {
		return IDAssigner.getNextIDFromFile(new File(ComputerCraft.getWorldDir(world), "computer/lastid_" + type + ".txt"));
	}

	public static int nextId(World world, IPeripheral peripheral) {
		return nextId(world, peripheral.getType());
	}

	public static boolean equals(Object a, Object b) {
		return a == b || (a != null && a.equals(b));
	}

	public static String snakeCase(String name) {
		return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
	}

	@SideOnly(Side.CLIENT)
	public static void setupModel(Item item, int damage, String name) {
		name = CCTweaks.ID + ":" + snakeCase(name);

		ModelResourceLocation res = new ModelResourceLocation(name, "inventory");
		ModelLoader.setCustomModelResourceLocation(item, damage, res);
	}

	@Nullable
	public static TileComputerBase getTile(ServerComputer computer) {
		World world = computer.getWorld();
		BlockPos pos = computer.getPosition();

		if (world != null && pos != null && world.isBlockLoaded(pos)) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileComputerBase && ((TileComputerBase) te).getServerComputer() == computer) {
				return ((TileComputerBase) te);
			}
		}

		return null;
	}

	public static ComputerFamily guessFamily(ServerComputer computer) {
		TileComputerBase tile = getTile(computer);
		if (tile != null) return tile.getFamily();

		return computer.isColour() ? ComputerFamily.Advanced : ComputerFamily.Normal;
	}
}
