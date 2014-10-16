package squiddev.cctweaks.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import squiddev.cctweaks.reference.Config;

import java.util.*;

public class PatchComputer implements Opcodes{
	public static byte[] PatchLuaMachine(byte[] bytes) {
		// Don't process if not needed
		if(Config.globalWhitelist.size() == 0) return bytes;

		// Setup class reader
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		boolean changed = false;

		for(MethodNode method : classNode.methods) {
			if(method.name.equals("<init>")) {
				int index = 0;

				List<Integer> toRemove = new ArrayList<Integer>();

				Iterator<AbstractInsnNode> iter = method.instructions.iterator();
				while (iter.hasNext()) {
					AbstractInsnNode thisNode = iter.next();

					// Check for removing globals
					if(thisNode.getOpcode() == GETFIELD && thisNode instanceof FieldInsnNode) {
						FieldInsnNode fieldNode = (FieldInsnNode) thisNode;

						if(fieldNode.name.equals("m_globals")) {
							AbstractInsnNode globalName = fieldNode.getNext();
							AbstractInsnNode luaNil = globalName.getNext();
							AbstractInsnNode invoke = luaNil.getNext();
							if(
								globalName.getOpcode() == LDC && globalName instanceof LdcInsnNode &&
								luaNil.getOpcode() == GETSTATIC && luaNil instanceof FieldInsnNode &&
								invoke.getOpcode() == INVOKEVIRTUAL && invoke instanceof MethodInsnNode
							){
								FieldInsnNode luaNilNode = (FieldInsnNode) luaNil;
								String globalNameValue = (String)((LdcInsnNode)globalName).cst;
								if(
									((MethodInsnNode) invoke).name.equals("set") && luaNilNode.name.equals("NIL") &&
									luaNilNode.owner.equals("org/luaj/vm2/LuaValue") &&
									Config.globalWhitelist.contains(globalNameValue)
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
				for(Integer instructionIndex : toRemove){
					changed = true;
					/* Remove:
					ALOAD 0
					GETFIELD dan200/computercraft/core/lua/LuaJLuaMachine.m_globals
					LDC Name
					GETSTATIC org/luaj/vm2/LuaValue.NIL
					INVOKEVIRTUAL org/luaj/vm2/LuaValue.set
					*/
					for(int i = 0; i < 5; i++)
					{
						method.instructions.remove(method.instructions.get(instructionIndex - offset));
					}

					offset += 5;
				}
			}
		}

		if(changed){
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
			classNode.accept(writer);
			return writer.toByteArray();
		}

		return bytes;
	}

	public static byte[] PatchLuaThread(byte[] bytes) {
		// 5000 is the default
		long timeout = 5000L;
		long targetTimeout = (long)Config.computerThreadTimeout;
		if(targetTimeout == timeout) return bytes;

		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		for(MethodNode method : classNode.methods) {
			if (method.name.equals("run")) {
				InsnList finding = new InsnList();
				finding.add(new VarInsnNode(ALOAD, 4));
				finding.add(new LdcInsnNode(timeout));
				finding.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Thread", "join", "(J)V", false));

				InsnListSection found = ASMMatcher.findOnce(method.instructions, new InsnListSection(finding), true);
				//found.remove(1);
				//((MethodInsnNode)found.get(1)).desc = "()V";
				((LdcInsnNode)found.get(1)).cst = targetTimeout;
			}
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		return writer.toByteArray();
	}
}
