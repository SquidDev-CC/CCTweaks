package org.squiddev.cctweaks.core.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.squiddev.cctweaks.api.peripheral.IPeripheralEnvironments;
import org.squiddev.patcher.InsnListSection;
import org.squiddev.patcher.search.Searcher;
import org.squiddev.patcher.transformer.IPatcher;

import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

/**
 * Add {@link IPeripheralEnvironments#ARG_NETWORK} to ComputerCraft's
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
	public ClassVisitor patch(String className, final ClassVisitor delegate) throws Exception {
		final ClassNode classNode = new ClassNode();
		return new ClassVisitor(ASM5, classNode) {
			@Override
			public void visitEnd() {
				super.visitEnd();

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

						InsnListSection found = Searcher.findOnce(method.instructions, new InsnListSection(addComputer));

						// Add the same as before but with INetworkAccess instead
						InsnList insert = new InsnList();
						insert.add(new LdcInsnNode(IPeripheralEnvironments.ARG_NETWORK));
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
				classNode.accept(delegate);
			}
		};
	}
}
