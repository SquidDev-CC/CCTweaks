package squiddev.cctweaks.core.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import squiddev.cctweaks.core.asm.chickenlib.ASMMatcher;
import squiddev.cctweaks.core.asm.chickenlib.InsnListSection;
import squiddev.cctweaks.core.reference.Config;
import squiddev.cctweaks.core.utils.DebugLogger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PatchComputer implements Opcodes {
	/**
	 * Patch the Lua machine, using LuaJC and removing global clearing
	 */
	public static byte[] PatchLuaMachine(byte[] bytes) {
		Set<String> whitelist = Config.globalWhitelist;

		// Don't process if not needed
		if (whitelist.size() == 0) return bytes;

		// Setup class reader
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		boolean changed = false;

		for (MethodNode method : classNode.methods) {
			if (method.name.equals("<init>")) {
				int index = 0;
				/*
					JsePlatform
					ALOAD 0
				    INVOKESTATIC org/luaj/vm2/lib/jse/JsePlatform.debugGlobals ()Lorg/luaj/vm2/LuaTable;
				    PUTFIELD dan200/computercraft/core/lua/LuaJLuaMachine.m_globals : Lorg/luaj/vm2/LuaValue;
				 */
				/*
					ALOAD 0
				    GETFIELD dan200/computercraft/core/lua/LuaJLuaMachine.m_globals : Lorg/luaj/vm2/LuaValue;
				    LDC <globalName>
				    GETSTATIC org/luaj/vm2/LuaValue.NIL : Lorg/luaj/vm2/LuaValue;
				    INVOKEVIRTUAL org/luaj/vm2/LuaValue.set (Ljava/lang/String;Lorg/luaj/vm2/LuaValue;)V
				 */

				List<Integer> toRemove = new ArrayList<Integer>();

				Iterator<AbstractInsnNode> iter = method.instructions.iterator();
				while (iter.hasNext()) {
					AbstractInsnNode thisNode = iter.next();

					// Check for removing globals
					if (thisNode.getOpcode() == GETFIELD && thisNode instanceof FieldInsnNode) {
						FieldInsnNode fieldNode = (FieldInsnNode) thisNode;

						if (fieldNode.name.equals("m_globals")) {
							AbstractInsnNode globalName = fieldNode.getNext();
							AbstractInsnNode luaNil = globalName.getNext();
							AbstractInsnNode invoke = luaNil.getNext();
							if (
								globalName.getOpcode() == LDC && globalName instanceof LdcInsnNode &&
									luaNil.getOpcode() == GETSTATIC && luaNil instanceof FieldInsnNode &&
									invoke.getOpcode() == INVOKEVIRTUAL && invoke instanceof MethodInsnNode
								) {
								FieldInsnNode luaNilNode = (FieldInsnNode) luaNil;
								String globalNameValue = (String) ((LdcInsnNode) globalName).cst;
								if (
									((MethodInsnNode) invoke).name.equals("set") && luaNilNode.name.equals("NIL") &&
										luaNilNode.owner.equals("org/luaj/vm2/LuaValue") &&
										whitelist.contains(globalNameValue)
									) {

									// Include ALOAD 0
									toRemove.add(index - 1);
								}
							}
						}
					}

					index++;
				}

				int offset = 0;
				for (Integer instructionIndex : toRemove) {
					changed = true;
					/* Remove:
						ALOAD 0
						GETFIELD dan200/computercraft/core/lua/LuaJLuaMachine.m_globals
						LDC Name
						GETSTATIC org/luaj/vm2/LuaValue.NIL
						INVOKEVIRTUAL org/luaj/vm2/LuaValue.set
					*/
					for (int i = 0; i < 5; i++) {
						method.instructions.remove(method.instructions.get(instructionIndex - offset));
					}

					offset += 5;
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

	public static byte[] PatchLuaThread(byte[] bytes) {
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
				} catch (Exception e) {
					DebugLogger.error("Cannot inject into ComputerThread -> Run");
					e.printStackTrace();
				}
			}
		}

		ClassWriter writer = new ClassWriter(0);
		classNode.accept(writer);
		return writer.toByteArray();
	}
}
