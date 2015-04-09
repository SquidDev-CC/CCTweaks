package org.squiddev.cctweaks.core.asm.patch;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * Tests the {@link ClassReplacer} and {@link ClassPartialPatcher} methods
 */
public class ClassReplacerTest {
	public static final String PATCHES = "org.squiddev.cctweaks.core.asm.patch.classes.";
	public static final String CLASS = PATCHES + "BaseClass";
	public static final String METHOD = "getMessage";
	public static final String FIELD = "message";

	@Test
	public void defaultClass() throws Exception {
		TestClassLoader loader = new TestClassLoader(new IPatcher[0]);

		Class<?> base = loader.loadClass(CLASS);
		Object instance = base.newInstance();

		Method method = base.getMethod(METHOD);
		assertEquals("Foo", method.invoke(instance));

		base.getField(FIELD).set(instance, "Bar");
		assertEquals("Bar", method.invoke(instance));
	}

	@Test
	public void defaultReplacer() throws Exception {
		TestClassLoader loader = new TestClassLoader(new IPatcher[]{new ClassReplacer(CLASS)});

		Class<?> base = loader.loadClass(CLASS);
		Object instance = base.newInstance();

		Method method = base.getMethod(METHOD);
		assertEquals("Bar_Rewrite", method.invoke(instance));

		base.getField("renamedMessage").set(instance, "Baz");
		assertEquals("Baz_Rewrite", method.invoke(instance));
	}

	@Test
	public void defaultPatch() throws Exception {
		TestClassLoader loader = new TestClassLoader(new IPatcher[]{new ClassPartialPatcher(CLASS)});

		Class<?> base = loader.loadClass(CLASS);
		Object instance = base.newInstance();

		Method method = base.getMethod(METHOD);
		assertEquals("Foo_Patch", method.invoke(instance));

		base.getField(FIELD).set(instance, "Bar");
		assertEquals("Bar_Patch", method.invoke(instance));
	}

	@Test
	public void patchAnnotations() throws Exception {
		TestClassLoader loader = new TestClassLoader(new IPatcher[]{new ClassPartialPatcher(CLASS)});

		Class<?> base = loader.loadClass(CLASS);
		Object instance = base.newInstance();

		assertEquals("Foo_Patch", base.getMethod("getInternal").invoke(instance));

		assertNotNull(loader.loadClass(CLASS + "$PublicInternal").getMethod("get", String.class));

		Method method = null;
		try {
			method = loader.loadClass(CLASS + "$PublicInternal").getMethod("get", int.class);
		} catch (Exception ignored) {
		}

		assertNull(method);
	}
}
