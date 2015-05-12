package org.squiddev.cctweaks.api;

/**
 * Main entry point for CCTweaks API
 */
public final class CCTweaksAPI {
	private static final ICCTweaksAPI API;

	/**
	 * Get the main API entry point
	 *
	 * @return Main API entry point
	 */
	public static ICCTweaksAPI instance() {
		return API;
	}

	static {
		ICCTweaksAPI api;
		String name = "org.squiddev.cctweaks.core.API";
		try {
			Class<?> registryClass = Class.forName(name);
			api = (ICCTweaksAPI) registryClass.newInstance();
		} catch (ClassNotFoundException e) {
			throw new CoreNotFoundException("Cannot load CCTweaks API as " + name + " cannot be found", e);
		} catch (InstantiationException e) {
			throw new CoreNotFoundException("Cannot load CCTweaks API as " + name + " cannot be created", e);
		} catch (IllegalAccessException e) {
			throw new CoreNotFoundException("Cannot load CCTweaks API as " + name + " cannot be accessed", e);
		}
		API = api;
	}
}
