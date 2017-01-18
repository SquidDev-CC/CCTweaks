package org.squiddev.cctweaks.api;

/**
 * The result of performing certain actions. Basically EnumActionResult for 1.8.9
 */
public enum ActionResult {
	/**
	 * This action was performed
	 */
	SUCCESS,
	/**
	 * This action could not be performed
	 */
	FAILURE,
	/**
	 * We can't process this action. Another handler will deal with this.
	 */
	PASS;
}
