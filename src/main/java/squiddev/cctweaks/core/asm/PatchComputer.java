package squiddev.cctweaks.core.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import squiddev.cctweaks.core.asm.chickenlib.ASMMatcher;
import squiddev.cctweaks.core.asm.chickenlib.InsnListSection;
import squiddev.cctweaks.core.reference.Config;
import squiddev.cctweaks.core.utils.DebugLogger;

import java.util.List;
import java.util.Set;

public class PatchComputer implements Opcodes {
	/**
	 * Patch the Lua machine, using LuaJC and removing global clearing
	 */
	public static byte[] patchLuaMachine(byte[] bytes) {
		Set<String> whitelist = Config.globalWhitelist;
		boolean luaJC = Config.config.luaJC;

		// Don't process if not needed
		if (whitelist.size() == 0 && !luaJC) return bytes;

		// Setup class reader
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		boolean changed = false;

		for (MethodNode method : classNode.methods) {
			if (method.name.equals("<init>")) {
				if (luaJC) {
					/*
						ALOAD 0
					    INVOKESTATIC org/luaj/vm2/lib/jse/JsePlatform.debugGlobals ()Lorg/luaj/vm2/LuaTable;
					    PUTFIELD dan200/computercraft/core/lua/LuaJLuaMachine.m_globals : Lorg/luaj/vm2/LuaValue;
					 */

					InsnList jsePlatform = new InsnList();
					jsePlatform.add(new VarInsnNode(ALOAD, 0));
					jsePlatform.add(new MethodInsnNode(INVOKESTATIC, "org/luaj/vm2/lib/jse/JsePlatform", "debugGlobals", "()Lorg/luaj/vm2/LuaTable;", false));
					jsePlatform.add(new FieldInsnNode(PUTFIELD, "dan200/computercraft/core/lua/LuaJLuaMachine", "m_globals", "Lorg/luaj/vm2/LuaValue;"));

					try {
						InsnListSection found = ASMMatcher.findOnce(method.instructions, new InsnListSection(jsePlatform), true);

						InsnList insert = new InsnList();
						insert.add(new MethodInsnNode(INVOKESTATIC, "squiddev/cctweaks/core/luajc/LuaJC", "install", "()V", false));
						found.insert(insert);

						changed = true;
						DebugLogger.debug("Injected LuaJCRewrite.install() call into LuaJLuaMachine.<init>");
					} catch (Exception e) {
						DebugLogger.error("Cannot inject LuaJCRewrite.install() into LuaJLuaMachine.<init>");
						e.printStackTrace();
					}
				}


				if (whitelist.size() > 0) {
					/*
						ALOAD 0
					    GETFIELD dan200/computercraft/core/lua/LuaJLuaMachine.m_globals : Lorg/luaj/vm2/LuaValue;
					    LDC <globalName>
					    GETSTATIC org/luaj/vm2/LuaValue.NIL : Lorg/luaj/vm2/LuaValue;
					    INVOKEVIRTUAL org/luaj/vm2/LuaValue.set (Ljava/lang/String;Lorg/luaj/vm2/LuaValue;)V
					 */

					InsnList removeBlacklist = new InsnList();
					removeBlacklist.add(new VarInsnNode(ALOAD, 0));
					removeBlacklist.add(new FieldInsnNode(GETFIELD, "dan200/computercraft/core/lua/LuaJLuaMachine", "m_globals", "Lorg/luaj/vm2/LuaValue;"));
					removeBlacklist.add(new LdcInsnNode(null)); // Match anything
					removeBlacklist.add(new FieldInsnNode(GETSTATIC, "org/luaj/vm2/LuaValue", "NIL", "Lorg/luaj/vm2/LuaValue;"));
					removeBlacklist.add(new MethodInsnNode(INVOKEVIRTUAL, "org/luaj/vm2/LuaValue", "set", "(Ljava/lang/String;Lorg/luaj/vm2/LuaValue;)V", false));

					try {
						List<InsnListSection> found = ASMMatcher.find(method.instructions, new InsnListSection(removeBlacklist), true);

						int offset = 0;
						int offsetChange = removeBlacklist.size();

						for (InsnListSection item : found) {
							// As we have removed items we should shift everything by the length of removed items
							item.shift(offset);

							// Check that this constant should be removed
							if (whitelist.contains(((LdcInsnNode) item.get(2)).cst)) {
								item.remove();

								offset -= offsetChange;

								changed = true;
							}
						}

						DebugLogger.debug("Injected whitelisted globals into LuaJLuaMachine.<init>");
					} catch (Exception e) {
						DebugLogger.error("Cannot inject whitelisted globals into LuaJLuaMachine.<init>");
						e.printStackTrace();
					}
				}
			}
		}

		if (changed) {
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			classNode.accept(writer);
			return writer.toByteArray();
		}

		return bytes;
	}

	/*
		Patch the Lua Thead with a new timeout length
	 */
	public static byte[] patchLuaThread(byte[] bytes) {
		long timeout = Config.defaults.computerThreadTimeout;
		long targetTimeout = Config.config.computerThreadTimeout;

		// If the timeouts are the same then continue
		if (targetTimeout == timeout) return bytes;

		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		for (MethodNode method : classNode.methods) {
			if (method.name.equals("run")) {
				// This is the Java source we need to find
				InsnList finding = new InsnList();
				finding.add(new VarInsnNode(ALOAD, 4));
				finding.add(new LdcInsnNode(timeout));
				finding.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Thread", "join", "(J)V", false));

				try {
					InsnListSection found = ASMMatcher.findOnce(method.instructions, new InsnListSection(finding), true);
					((LdcInsnNode) found.get(1)).cst = targetTimeout;

					DebugLogger.debug("Inject timeout into ComputerThread.Run");
				} catch (Exception e) {
					DebugLogger.error("Cannot inject timeout into ComputerThread.Run");
					e.printStackTrace();
				}
			}
		}

		ClassWriter writer = new ClassWriter(0);
		classNode.accept(writer);
		return writer.toByteArray();
	}
}

