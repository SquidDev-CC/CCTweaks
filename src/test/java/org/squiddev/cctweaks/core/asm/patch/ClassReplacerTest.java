package org.squiddev.cctweaks.core.asm.patch;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

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
		RewriteClassLoader loader = new RewriteClassLoader(new IPatcher[0]);

		Class<?> base = loader.loadClass(CLASS);
		Object instance = base.newInstance();

		Method method = base.getMethod(METHOD);
		assertEquals("Foo", method.invoke(instance));

		base.getField(FIELD).set(instance, "Bar");
		assertEquals("Bar", method.invoke(instance));
	}

	@Test
	public void defaultReplacer() throws Exception {
		RewriteClassLoader loader = new RewriteClassLoader(new IPatcher[]{new ClassReplacer(CLASS)});

		Class<?> base = loader.loadClass(CLASS);
		Object instance = base.newInstance();

		Method method = base.getMethod(METHOD);
		assertEquals("Bar_Rewrite", method.invoke(instance));

		base.getField("renamedMessage").set(instance, "Baz");
		assertEquals("Baz_Rewrite", method.invoke(instance));
	}

	@Test
	public void defaultPatch() throws Exception {
		RewriteClassLoader loader = new RewriteClassLoader(new IPatcher[]{new ClassPartialPatcher(CLASS)});

		Class<?> base = loader.loadClass(CLASS);
		Object instance = base.newInstance();

		Method method = base.getMethod(METHOD);
		assertEquals("Foo_Patch", method.invoke(instance));

		base.getField(FIELD).set(instance, "Bar");
		assertEquals("Bar_Patch", method.invoke(instance));
	}
}
