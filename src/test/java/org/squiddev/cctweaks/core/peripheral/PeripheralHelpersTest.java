package org.squiddev.cctweaks.core.peripheral;

import dan200.computercraft.api.peripheral.IPeripheral;
import org.junit.Test;
import org.squiddev.cctweaks.api.CCTweaksAPI;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHelpers;
import org.squiddev.cctweaks.api.peripheral.IPeripheralProxy;
import org.squiddev.cctweaks.api.peripheral.IPeripheralTargeted;

import static org.junit.Assert.assertEquals;

/**
 * Some tests for {@link PeripheralHelpers}
 */
public class PeripheralHelpersTest {
	public static final IPeripheralHelpers helpers = CCTweaksAPI.instance().peripheralHelpers();

	@Test
	public void testGetBasePeripheral() throws Exception {
		BasePeripheral base = new BasePeripheral();
		Proxy empty = new Proxy(null);


		assertProxy("Nested: 0", base, base);
		assertProxy("Nested: 3", base, new Proxy(new Proxy(new Proxy(base))));

		assertProxy("Null getBasePeripheral", empty, new Proxy(new Proxy(empty)));
	}

	@Test
	public void testGetTarget() throws Exception {
		IPeripheral base = new BasePeripheral();
		Object target = new Object();
		Targeted empty = new Targeted(null);

		assertTarget("Straight run", base, base);
		assertTarget("Target", target, new Targeted(target));

		assertTarget("Proxy", base, new Proxy(new Proxy(base)));
		assertTarget("Target and proxy", target, new Proxy(new Proxy(new Targeted(target))));

		assertTarget("Prefer Target", target, new TargetProxy(
			target,
			new Proxy(new Targeted(target))
		));

		assertTarget("Return proxy on null target", target, new TargetProxy(
			null,
			new Proxy(new Targeted(target))
		));

		assertTarget("Return parent on null target", empty, new Proxy(new Proxy(empty)));
	}

	public static void assertProxy(String message, IPeripheral target, IPeripheral start) {
		assertEquals(message, target, helpers.getBasePeripheral(start));
	}

	public static void assertTarget(String message, Object target, IPeripheral start) {
		assertEquals(message, target, helpers.getTarget(start));
	}

	public static class Targeted extends BasePeripheral implements IPeripheralTargeted {
		public final Object target;

		public Targeted(Object target) {
			this.target = target;
		}

		@Override
		public Object getTarget() {
			return target;
		}
	}

	public static class Proxy extends BasePeripheral implements IPeripheralProxy {
		public final IPeripheral base;

		public Proxy(IPeripheral base) {
			this.base = base;
		}

		@Override
		public IPeripheral getBasePeripheral() {
			return base;
		}
	}

	public static class TargetProxy extends Targeted implements IPeripheralProxy {
		public final IPeripheral base;

		public TargetProxy(Object target, IPeripheral base) {
			super(target);
			this.base = base;
		}

		@Override
		public IPeripheral getBasePeripheral() {
			return base;
		}
	}
}
