package squiddev.cctweaks.turtle;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.*;
import dan200.computercraft.shared.turtle.core.TurtlePlaceCommand;
import dan200.computercraft.shared.turtle.core.TurtlePlayer;
import dan200.computercraft.shared.util.IEntityDropConsumer;
import dan200.computercraft.shared.util.InventoryUtil;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import squiddev.cctweaks.reference.Config;
import squiddev.cctweaks.reference.ModInfo;

public class TurtleToolHost implements ITurtleUpgrade {
	protected ItemStack thisItem = new ItemStack(Items.diamond_axe, 1);
	protected TurtlePlayer turtlePlayer;

	@Override
	public int getUpgradeID() {
		return Config.turtleToolHostId;
	}

	@Override
	public String getUnlocalisedAdjective() {
		return ModInfo.RESOURCE_DOMAIN + ".turtle.toolhost.adjective";
	}

	@Override
	public TurtleUpgradeType getType() {
		return TurtleUpgradeType.Tool;
	}

	@Override
	public ItemStack getCraftingItem() {
		return new ItemStack(Items.diamond, 1);
	}

	@Override
	public IPeripheral createPeripheral(ITurtleAccess turtleAccess, TurtleSide turtleSide) {
		return null;
	}

	@Override
	public TurtleCommandResult useTool(ITurtleAccess turtleAccess, TurtleSide turtleSide, TurtleVerb turtleVerb, int direction) {
		if(turtleVerb == TurtleVerb.Attack) {
			return attack(turtleAccess, direction);
		} else if(turtleVerb == TurtleVerb.Dig){
			return dig(turtleAccess, direction);
		}
		return TurtleCommandResult.failure("Unsupported action");
	}

	protected TurtleCommandResult attack(final ITurtleAccess turtle, int direction) {
		final World world = turtle.getWorld();
		final ChunkCoordinates position = turtle.getPosition();

		if(turtlePlayer == null) turtlePlayer = TurtlePlaceCommand.createPlayer(world, position, turtle, direction);

		Vec3 turtlePos = Vec3.createVectorHelper(turtlePlayer.posX, turtlePlayer.posY, turtlePlayer.posZ);
		Vec3 rayDir = turtlePlayer.getLook(1.0F);
		Vec3 rayStart = turtlePos.addVector(rayDir.xCoord * 0.4D, rayDir.yCoord * 0.4D, rayDir.zCoord * 0.4D);
		Entity hitEntity = WorldUtil.rayTraceEntities(world, rayStart, rayDir, 1.1D);
		if (hitEntity != null) {
			turtlePlayer.loadInventory(thisItem);

			ComputerCraft.setEntityDropConsumer(hitEntity, new IEntityDropConsumer() {
				public void consumeDrop(Entity entity, ItemStack drop) {
					ItemStack remainder = InventoryUtil.storeItems(drop, turtle.getInventory(), 0, turtle.getInventory().getSizeInventory(), turtle.getSelectedSlot());
					if (remainder != null) {
						WorldUtil.dropItemStack(remainder, world, position.posX, position.posY, position.posZ, Facing.oppositeSide[turtle.getDirection()]);
					}
				}
			});
			boolean placed = false;
			if ((hitEntity.canAttackWithItem()) && (!hitEntity.hitByEntity(turtlePlayer))) {
				turtlePlayer.attackTargetEntityWithCurrentItem(hitEntity);
				placed = true;
			}
			ComputerCraft.clearEntityDropConsumer(hitEntity);

			if(thisItem.stackSize == 0) {
				thisItem = null;
			}

			if (placed) {
				turtlePlayer.unloadInventory(turtle);
				return TurtleCommandResult.success();
			}


		}
		return TurtleCommandResult.failure("Nothing to attack here");
	}


	protected TurtleCommandResult dig(ITurtleAccess turtle, int direction) {
		final World world = turtle.getWorld();
		final ChunkCoordinates position = turtle.getPosition();
		ChunkCoordinates newPosition = WorldUtil.moveCoords(position, direction);

		TurtlePlayer turtlePlayer = TurtlePlaceCommand.createPlayer(world, position, turtle, direction);
		Block localBlock = world.getBlock(newPosition.posX, newPosition.posY, newPosition.posZ);
		if(localBlock != Blocks.air) {
			turtlePlayer.theItemInWorldManager.uncheckedTryHarvestBlock(newPosition.posX, newPosition.posY, newPosition.posZ);
			if(thisItem != null)
			{
				thisItem.getItem().onBlockDestroyed(thisItem, world, localBlock, newPosition.posX, newPosition.posY, newPosition.posZ, turtlePlayer);
			}

			return TurtleCommandResult.success();
		}

		return TurtleCommandResult.failure("Nothing here");
	}

	@Override
	public IIcon getIcon(ITurtleAccess turtleAccess, TurtleSide turtleSide) {
		if(thisItem == null)
		{
			return new ItemStack(Items.skull, 1).getIconIndex();
		}
		return thisItem.getIconIndex();
	}

	@Override
	public void update(ITurtleAccess turtleAccess, TurtleSide turtleSide) { }

	public void registerItem() {
		ComputerCraft.registerTurtleUpgrade(this);
	}
}
