package org.squiddev.cctweaks.core.asm.patch;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * Tests the {@link ClassPartialPatcher} methods
 */
public class PartialPatcherTest {
	public static final String PATCHES = ClassReplacerTest.PATCHES + "patch.";
	public static final String CLASS = PATCHES + "Base";

	@Test
	public void classRename() throws Exception {
		RewriteClassLoader loader = new RewriteClassLoader(new IPatcher[]{new ClassPartialPatcher(CLASS, PATCHES + "ClassRename")});

		Class<?> base = loader.loadClass(CLASS);
		Object instance = base.newInstance();

		Method method = base.getMethod("getName");
		assertEquals("Bar", method.invoke(instance));
	}

	@Test
	public void methodRename() throws Exception {
		RewriteClassLoader loader = new RewriteClassLoader(new IPatcher[]{new ClassPartialPatcher(CLASS, PATCHES + "MethodRename")});

		Class<?> base = loader.loadClass(CLASS);
		Object instance = base.newInstance();

		Method method = base.getMethod("getName");
		assertEquals("FooBar", method.invoke(instance));
	}

	@Test
	public void callSuper() throws Exception {
		RewriteClassLoader loader = new RewriteClassLoader(new IPatcher[]{new ClassPartialPatcher(CLASS, PATCHES + "Super")});

		Class<?> base = loader.loadClass(CLASS);
		Object instance = base.newInstance();

		Method method = base.getMethod("getName");
		assertEquals("VeryBaseSuper", method.invoke(instance));
	}

	@Test
	public void stubMethod() throws Exception {
		RewriteClassLoader loader = new RewriteClassLoader(new IPatcher[]{new ClassPartialPatcher(CLASS, PATCHES + "MethodStub")});

		Class<?> base = loader.loadClass(CLASS);
		Object instance = base.newInstance();

		Method method = base.getMethod("getName");
		assertEquals("Foo", method.invoke(instance));

		method = base.getMethod("getPrivateName");
		assertEquals("Private", method.invoke(instance));
	}

	@Test
	public void stubMethodAccess() throws Exception {
		RewriteClassLoader loader = new RewriteClassLoader(new IPatcher[]{new ClassPartialPatcher(CLASS, PATCHES + "MethodStub")});

		Class<?> base = loader.loadClass(CLASS);
		Object instance = base.newInstance();

		Method method = base.getMethod("getPrivateName");
		assertEquals("Private", method.invoke(instance));
	}

	@Test
	public void stubClass() throws Exception {
		RewriteClassLoader loader = new RewriteClassLoader(new IPatcher[]{new ClassPartialPatcher(CLASS, PATCHES + "ClassStub")});

		Class<?> base = loader.loadClass(CLASS);
		Object instance = base.newInstance();

		Method method = base.getMethod("getName");
		assertEquals("Foo Stub", method.invoke(instance));
	}

	@Test
	public void rewriteClass() throws Exception {
		RewriteClassLoader loader = new RewriteClassLoader(new IPatcher[]{new ClassPartialPatcher(CLASS, PATCHES + "ClassRewrite")});

		Class<?> base = loader.loadClass(CLASS + "$Foo");
		{
			Method method = null;
			try {
				method = base.getMethod("getName", int.class);
			} catch (Exception ignored) {
			}

			assertNotNull(method);
		}

		{
			Method method = null;
			try {
				method = base.getMethod("getName");
			} catch (Exception ignored) {
			}

			assertNull(method);
		}

	}
}
