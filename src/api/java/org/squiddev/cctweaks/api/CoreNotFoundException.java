package org.squiddev.cctweaks.api;

/**
 * Thrown when we cannot load an API
 */
public class CoreNotFoundException extends RuntimeException {
	public CoreNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
