package org.squiddev.cctweaks.core.patcher.runner;

import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.computer.ComputerThread;
import dan200.computercraft.core.computer.ITask;
import dan200.computercraft.core.terminal.Terminal;
import org.junit.Assert;
import org.squiddev.cctweaks.core.patcher.utils.Notifier;

/**
 * Utilities to run a script on a computer
 */
public class RunOnComputer {
	public static void run(String program) throws Throwable {
		MemoryMount mount = new MemoryMount()
			.addFile("test", program)
			.addFile("startup", "assert.assert(pcall(loadfile('test') or error)) os.shutdown()");
		Terminal term = new Terminal(51, 19);
		final Computer computer = new Computer(
			new BasicEnvironment(mount),
			term,
			0
		);

		AssertionAPI api = new AssertionAPI();
		computer.addAPI(api);

		try {
			final Notifier waiter = new Notifier();
			computer.turnOn();

			for (int i = 0; i < 60; i++) {
				computer.advance(0.05);
				ComputerThread.queueTask(new ITask() {
					@Override
					public Computer getOwner() {
						return computer;
					}

					@Override
					public void execute() {
						waiter.doNotify();
					}
				}, computer);

				waiter.doWait();

				Throwable exception = api.getException();
				if (exception != null) {
					if (computer.isOn()) computer.shutdown();
					throw exception;
				}

				Thread.sleep(1000 / 20);

				if (!computer.isOn()) break;
			}

			if (computer.isOn()) {
				StringBuilder builder = new StringBuilder();
				for (int line = 0; line < 19; line++) {
					if (!term.getLine(line).toString().replace(" ", "").isEmpty()) {
						builder.append(line).append("|").append(term.getLine(line)).append('\n');
					}
				}
				computer.shutdown();

				String message = builder.length() == 0 ? " No result " : "\n" + builder.toString();
				Assert.fail("Still running:" + message);
			}
		} finally {
			ComputerThread.stop();
		}
	}
}
