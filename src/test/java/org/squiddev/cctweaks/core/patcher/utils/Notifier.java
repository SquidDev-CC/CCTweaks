package org.squiddev.cctweaks.core.patcher.utils;

/**
 * More advanced waiter
 */
public class Notifier {
	private boolean waited = false;
	private final Object handle = new Object();

	public void doWait() throws InterruptedException {
		synchronized (this) {
			if (waited) {
				waited = false;
				return;
			}
		}

		synchronized (handle) {
			handle.wait();
		}
	}

	public void doNotify() {
		synchronized (this) {
			waited = true;

			synchronized (handle) {
				handle.notify();
			}
		}
	}
}
