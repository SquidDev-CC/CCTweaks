package org.squiddev.cctweaks.core.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.squiddev.cctweaks.api.peripheral.IPeripheralEnvironments;
import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.visitors.FindingVisitor;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

/**
 * Add {@link IPeripheralEnvironments#ARG_NETWORK} to ComputerCraft's
 * environment list:
 *
 * https://github.com/OpenMods/OpenPeripheral/blob/master/src/main/java/openperipheral/interfaces/cc/ModuleComputerCraft.java#L33-L37
 */
public class PatchOpenModule implements IPatcher {
	@Override
	public boolean matches(String className) {
		return className.equals("openperipheral.interfaces.cc.ModuleComputerCraft");
	}

	@Override
	public ClassVisitor patch(String className, final ClassVisitor delegate) throws Exception {
		return new FindingVisitor(
			delegate,
			new LdcInsnNode("computer"),
			new LdcInsnNode(Type.getType("Ldan200/computercraft/api/peripheral/IComputerAccess;")),
			new MethodInsnNode(INVOKEVIRTUAL,
				"openperipheral/adapter/composed/MethodSelector",
				"addProvidedEnv",
				"(Ljava/lang/String;Ljava/lang/Class;)Lopenperipheral/adapter/composed/MethodSelector;",
				false
			)
		) {
			@Override
			public void handle(InsnList nodes, MethodVisitor visitor) {
				nodes.accept(visitor);
				visitor.visitLdcInsn(IPeripheralEnvironments.ARG_NETWORK);
				visitor.visitLdcInsn(Type.getType("Lorg/squiddev/cctweaks/api/network/INetworkAccess;"));
				visitor.visitMethodInsn(INVOKEVIRTUAL,
					"openperipheral/adapter/composed/MethodSelector",
					"addProvidedEnv",
					"(Ljava/lang/String;Ljava/lang/Class;)Lopenperipheral/adapter/composed/MethodSelector;",
					false
				);
			}
		}.onMethod("<clinit>").onMethod("init").once().mustFind();
	}
}
