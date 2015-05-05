package org.squiddev.cctweaks.api;

/**
 * Thrown when we cannot load an API
 */
public class CoreNotFoundException extends RuntimeException {
	public CoreNotFoundException(String message) {
		super(message);
	}

	public CoreNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public CoreNotFoundException(ClassNotFoundException e, String name) {
		this("Cannot load CCTweaks API as " + name + " cannot be found");
	}

	public CoreNotFoundException(InstantiationException e, String name) {
		this("Cannot load CCTweaks API as " + name + " cannot be created", e);
	}

	public CoreNotFoundException(IllegalAccessException e, String name) {
		this("Cannot load CCTweaks API as " + name + " cannot be created");
	}
}
