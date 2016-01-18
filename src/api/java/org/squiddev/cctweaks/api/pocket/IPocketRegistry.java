package org.squiddev.cctweaks.api.pocket;

/**
 * Handles registration for pocket peripherals/upgrades
 */
public interface IPocketRegistry {
	/**
	 * Register a pocket upgrade
	 *
	 * @param upgrade The upgrade to register
	 * @throws IllegalArgumentException If an upgrade with the same key has been registered
	 */
	void addUpgrade(IPocketUpgrade upgrade);

	/**
	 * Register a legacy Peripherals++ upgrade
	 *
	 * @param id      The id to register under
	 * @param upgrade The upgrade to register. This must also be registered with {@link #addUpgrade(IPocketUpgrade)}
	 * @throws IllegalArgumentException If id is invalid.
	 */
	void addLegacyUpgrade(int id, IPocketUpgrade upgrade);
}
