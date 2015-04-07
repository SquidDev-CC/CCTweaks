package squiddev.cctweaks.core.items;

import dan200.computercraft.core.lua.LuaJLuaMachine;
import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.DebugLib;
import squiddev.cctweaks.core.utils.ComputerAccessor;
import squiddev.cctweaks.core.utils.DebugLogger;

public class ItemDebugger extends ItemComputerAction {
	public ItemDebugger() {
		super("debugger");
	}

	protected boolean upgradeComputer(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, TileComputerBase computerTile) {
		ServerComputer serverComputer = computerTile.getServerComputer();

		if (serverComputer == null) return false;

		try {
			Object computer = ComputerAccessor.serverComputerComputer.get(serverComputer);
			Object luaMachine = ComputerAccessor.computerMachine.get(computer);

			if (!(luaMachine instanceof LuaJLuaMachine)) {
				DebugLogger.debug("Computer is not instance of LuaJLuaMachine, cannot get globals");
				return false;
			}

			LuaValue globals = (LuaValue) ComputerAccessor.luaMachineGlobals.get(luaMachine);
			globals.load(new DebugLib());

		} catch (ReflectiveOperationException e) {
			DebugLogger.debug("Could not add DebugLib: " + e.toString());
			return false;
		} catch (NullPointerException e) {
			DebugLogger.debug("Could not add DebugLib: " + e.toString());
			return false;
		} catch (Exception e) {
			DebugLogger.debug("Unknown error in injecting DebugLib");
			e.printStackTrace();
			return false;
		}

		return true;
	}
}
