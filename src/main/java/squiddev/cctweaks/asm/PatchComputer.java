package squiddev.cctweaks.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import squiddev.cctweaks.reference.Config;

import java.util.*;

public class PatchComputer implements Opcodes{
	public static byte[] PatchLuaMachine(byte[] bytes)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		boolean changed = false;

		for(MethodNode method : classNode.methods)
		{
			if(method.name.equals("<init>"))
			{
				AbstractInsnNode targetNode = null;
				int index = 0;

				List<Integer> toRemove = new ArrayList<Integer>();

				Iterator<AbstractInsnNode> iter = method.instructions.iterator();
				while (iter.hasNext())
				{
					AbstractInsnNode thisNode = iter.next();

					// Check for removing globals
					if(thisNode.getOpcode() == GETFIELD && thisNode instanceof FieldInsnNode) {
						FieldInsnNode fieldNode = (FieldInsnNode) thisNode;

						if(fieldNode.name.equals("m_globals"))
						{
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
						method.instructions.remove(method.instructions.get(instructionIndex));
					}
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
}
