package org.squiddev.cctweaks.core.asm.patch;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.squiddev.cctweaks.CCTweaks;

/**
 * Patches methods on a class
 */
public interface IPatcher {
	Marker MARKER = MarkerManager.getMarker(CCTweaks.ID + ":Patcher");

	/**
	 * Checks if the class matches
	 *
	 * @param className The name of the class
	 * @return If it should be patched
	 */
	boolean matches(String className);

	/**
	 * Patches a class
	 *
	 * @param className The name of the class
	 * @param bytes     The original bytes to patch
	 * @return The patched bytes
	 */
	byte[] patch(String className, byte[] bytes);
}
