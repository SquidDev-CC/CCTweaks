package org.squiddev.cctweaks.core.integration.multipart;

import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import net.minecraft.world.World;

/**
 * Various helpers for multiparts
 */
public class MultipartHelpers {
	private static final Cuboid6[] peripheralOcclusion = new Cuboid6[]{
		new Cuboid6(0.125, 0.0, 0.125, 0.875, 0.125, 0.875D),
		new Cuboid6(0.125, 0.875, 0.125, 0.875, 1.0, 0.875D),
		new Cuboid6(0.125, 0.125, 0.0, 0.875, 0.875, 0.125D),
		new Cuboid6(0.125, 0.125, 0.875, 0.875, 0.875, 1.0D),
		new Cuboid6(0.0, 0.125, 0.125, 0.125, 0.875, 0.875D),
		new Cuboid6(0.875, 0.125, 0.125, 1.0, 0.875, 0.875D),
	};

	/**
	 * Get the occlusion boxes for sided peripherals in a multipart
	 *
	 * @param direction The direction the multipart is facing
	 * @return The bounding boxes
	 */
	public static Cuboid6 peripheralOcclusion(int direction) {
		if (direction < 0 || direction > 5) direction = 0;
		return peripheralOcclusion[direction];
	}

	private static final Cuboid6[] peripheralBounds = new Cuboid6[]{
		new Cuboid6(0.125, 0.0, 0.125, 0.875, 0.1875, 0.875),
		new Cuboid6(0.125, 0.8125, 0.125, 0.875, 1.0, 0.875),
		new Cuboid6(0.125, 0.125, 0.0, 0.875, 0.875, 0.1875),
		new Cuboid6(0.125, 0.125, 0.8125, 0.875, 0.875, 1.0),
		new Cuboid6(0.0, 0.125, 0.125, 0.1875, 0.875, 0.875),
		new Cuboid6(0.8125, 0.125, 0.125, 1.0, 0.875, 0.875),
	};

	/**
	 * Get the bounds for sided peripherals in a multipart
	 *
	 * This is used for subparts and render bounds
	 *
	 * @param direction The direction the multipart is facing
	 * @return The bounding boxes
	 */
	public static Cuboid6 peripheralBounds(int direction) {
		if (direction < 0 || direction > 5) direction = 0;
		return peripheralBounds[direction];
	}

	/**
	 * Place a multipart in the world
	 *
	 * @param world The world to place in
	 * @param pos   The position to place at
	 * @param hit   The hit position
	 * @param side  The side to place on
	 * @param part  The part to place
	 * @return Success at placing the part
	 */
	public static boolean place(World world, BlockCoord pos, Vector3 hit, int side, TMultiPart part) {
		double hitDepth = hit.copy().scalarProject(Rotation.axes[side]) + (side % 2 ^ 1);

		return (hitDepth < 1 && place(world, pos, part)) || place(world, pos.offset(side), part);
	}

	/**
	 * Place a multipart into the world
	 *
	 * @param world The world to place in
	 * @param pos   The position to place at
	 * @param part  The part to place
	 * @return Success at placing the part
	 */
	public static boolean place(World world, BlockCoord pos, TMultiPart part) {
		if (part == null || !TileMultipart.canPlacePart(world, pos, part)) return false;
		if (!world.isRemote) TileMultipart.addPart(world, pos, part);

		return true;
	}
}
