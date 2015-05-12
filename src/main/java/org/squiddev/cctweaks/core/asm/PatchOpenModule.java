package org.squiddev.cctweaks.core.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.squiddev.cctweaks.api.peripheral.INetworkedAdapter;
import org.squiddev.cctweaks.core.asm.chickenlib.ASMMatcher;
import org.squiddev.cctweaks.core.asm.chickenlib.InsnListSection;
import org.squiddev.cctweaks.core.asm.patch.IPatcher;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

/**
 * Add {@link org.squiddev.cctweaks.api.peripheral.INetworkedAdapter#ARG_NETWORK} to ComputerCraft's
 * environment list:
 *
 * https://github.com/OpenMods/OpenPeripheral/blob/master/src/main/java/openperipheral/interfaces/cc/ModuleComputerCraft.java#L33-L37
 */
public class PatchOpenModule implements IPatcher {
	public final String className = "openperipheral.interfaces.cc.ModuleComputerCraft";

	@Override
	public boolean matches(String className) {
		return className.equals(this.className);
	}

	@Override
	public byte[] patch(String className, byte[] bytes) {
		try {
			ClassNode classNode = new ClassNode();
			new ClassReader(bytes).accept(classNode, ClassReader.EXPAND_FRAMES);

			boolean changed = false;
			for (MethodNode method : classNode.methods) {
				if (method.name.equals("<clinit>")) {
					/*
						LDC "computer"
					    LDC Ldan200/computercraft/api/peripheral/IComputerAccess;.class
					    INVOKEVIRTUAL openperipheral/adapter/composed/MethodSelector.addProvidedEnv
					        (Ljava/lang/String;Ljava/lang/Class;)Lopenperipheral/adapter/composed/MethodSelector;
					 */

					InsnList addComputer = new InsnList();
					addComputer.add(new LdcInsnNode("computer"));
					addComputer.add(new LdcInsnNode(Type.getType("Ldan200/computercraft/api/peripheral/IComputerAccess;")));
					addComputer.add(new MethodInsnNode(INVOKEVIRTUAL,
						"openperipheral/adapter/composed/MethodSelector",
						"addProvidedEnv",
						"(Ljava/lang/String;Ljava/lang/Class;)Lopenperipheral/adapter/composed/MethodSelector;",
						false
					));

					InsnListSection found = ASMMatcher.findOnce(method.instructions, new InsnListSection(addComputer), true);

					// Add the same as before but with INetworkAccess instead
					InsnList insert = new InsnList();
					insert.add(new LdcInsnNode(INetworkedAdapter.ARG_NETWORK));
					insert.add(new LdcInsnNode(Type.getType("Lorg/squiddev/cctweaks/api/network/INetworkAccess;")));
					insert.add(new MethodInsnNode(INVOKEVIRTUAL,
						"openperipheral/adapter/composed/MethodSelector",
						"addProvidedEnv",
						"(Ljava/lang/String;Ljava/lang/Class;)Lopenperipheral/adapter/composed/MethodSelector;",
						false
					));

					found.insert(insert);

					changed = true;
					break;
				}
			}

			if (!changed) throw new RuntimeException("Cannot find <clinit> method");

			DebugLogger.debug(MARKER, "Injected custom " + className);

			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			classNode.accept(writer);
			return writer.toByteArray();
		} catch (Exception e) {
			DebugLogger.error(MARKER, "Cannot replace " + className + ", falling back to default", e);
			return bytes;
		}
	}
}
