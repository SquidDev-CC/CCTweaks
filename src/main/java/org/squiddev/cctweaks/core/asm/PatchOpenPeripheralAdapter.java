package org.squiddev.cctweaks.core.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.squiddev.cctweaks.api.peripheral.IPeripheralEnvironments;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.patcher.transformer.ClassMerger;
import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.visitors.FindingVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * A custom patcher to allow us to also extend the environment
 *
 * Before changing anything check with
 * <ul>
 * <li>{@link org.squiddev.cctweaks.core.asm.PatchOpenPeripheralAdapter}</li>
 * <li>{@link org.squiddev.cctweaks.core.patch.op.AdapterPeripheral_Patch}</li>
 * </ul>
 * to make sure it doesn't break anything
 */
public class PatchOpenPeripheralAdapter implements IPatcher {
	private static final String CLASS_NAME = "openperipheral.interfaces.cc.wrappers.AdapterPeripheral";
	private static final String CLASS_TYPE = CLASS_NAME.replace('.', '/');

	@Override
	public boolean matches(String className) {
		return className.equals(CLASS_NAME);
	}

	@Override
	public ClassVisitor patch(final String className, ClassVisitor delegate) throws Exception {
		return new FindingVisitor(
			new ClassMerger(
				CLASS_NAME,
				"org.squiddev.cctweaks.core.patch.op.AdapterPeripheral_Patch"
			).patch(className, delegate),
			new MethodInsnNode(
				INVOKEVIRTUAL,
				"openperipheral/interfaces/cc/ComputerCraftEnv",
				"addPeripheralArgs",
				"(Lopenperipheral/adapter/IMethodCall;Ldan200/computercraft/api/peripheral/IComputerAccess;Ldan200/computercraft/api/lua/ILuaContext;)Lopenperipheral/adapter/IMethodCall;",
				false
			)
		) {
			@Override
			public void handle(InsnList nodes, MethodVisitor visitor) {
				nodes.accept(visitor);

				visitor.visitLdcInsn(IPeripheralEnvironments.ARG_NETWORK);
				visitor.visitVarInsn(ALOAD, 0);
				visitor.visitFieldInsn(INVOKEVIRTUAL, CLASS_TYPE, "getNetworkAccess", "()Lorg/squiddev/cctweaks/core/network/NetworkAccessDelegate;");
				visitor.visitMethodInsn(
					INVOKEINTERFACE,
					"openperipheral/adapter/IMethodCall",
					"setEnv",
					"(Ljava/lang/String;Ljava/lang/Object;)" + "Lopenperipheral/adapter/IMethodCall;",
					true
				);

				DebugLogger.debug("Added additional environments into " + CLASS_NAME);
			}
		}.once().mustFind();
	}
}
