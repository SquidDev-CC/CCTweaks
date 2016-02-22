package org.squiddev.cctweaks.integration;

import cpw.mods.fml.common.Optional;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.CCTweaksAPI;
import org.squiddev.cctweaks.api.turtle.ITurtleInteraction;
import org.squiddev.cctweaks.api.turtle.ITurtleRegistry;
import portalgun.common.core.EntityHelper;
import portalgun.common.item.ItemPortalGun;
import portalgun.common.item.ItemPortalGunBlue;

/**
 * Adds turtle interaction for the portal gun mod
 */
public class PortalGunIntegration extends ModIntegration {
	public static final String MOD_NAME = "PortalGun";

	public PortalGunIntegration() {
		super(MOD_NAME);
	}

	@Override
	@Optional.Method(modid = MOD_NAME)
	public void init() {
		ITurtleRegistry registry = CCTweaksAPI.instance().turtleRegistry();

		registry.registerInteraction(new ITurtleInteraction() {
			@Override
			public TurtleCommandResult swing(ITurtleAccess turtle, IComputerAccess computer, FakePlayer player, ItemStack stack, ForgeDirection direction, MovingObjectPosition hit) throws LuaException {
				if (!(stack.getItem() instanceof ItemPortalGun)) return null;

				EntityHelper.shootPortal(player, stack, 1);
				return TurtleCommandResult.success(new Object[]{"portal"});
			}

			@Override
			public TurtleCommandResult use(ITurtleAccess turtle, IComputerAccess computer, FakePlayer player, ItemStack stack, ForgeDirection direction, MovingObjectPosition hit) throws LuaException {
				if (!(stack.getItem() instanceof ItemPortalGun)) return null;

				EntityHelper.shootPortal(player, stack, stack.getItem() instanceof ItemPortalGunBlue ? 1 : 2);
				return TurtleCommandResult.success(new Object[]{"portal"});
			}
		});
	}
}
