package org.squiddev.cctweaks.blocks;

/**
 * Interface to be implemented by {@link BlockBase} for blocks that represent more than one object
 */
public interface IMultiBlock {
	/**
	 * Get the unlocalised name for this damage value
	 *
	 * @param meta Metadata value
	 * @return The metadata value
	 */
	String getUnlocalizedName(int meta);
}
