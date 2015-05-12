package org.squiddev.cctweaks.core.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;
import org.squiddev.cctweaks.api.peripheral.INetworkedAdapter;
import org.squiddev.cctweaks.core.asm.chickenlib.ASMMatcher;
import org.squiddev.cctweaks.core.asm.patch.ClassPartialPatcher;
import org.squiddev.cctweaks.core.asm.patch.MergeVisitor;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.util.Iterator;

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
public class PatchOpenPeripheralAdapter extends ClassPartialPatcher {
	public static final String METHOD_CALL = "Lopenperipheral/adapter/IMethodCall;";

	public PatchOpenPeripheralAdapter() {
		super("openperipheral.interfaces.cc.wrappers.AdapterPeripheral", "org.squiddev.cctweaks.core.patch.op.AdapterPeripheral_Patch");
	}

	@Override
	public byte[] patch(String className, byte[] bytes) {
		try {
			ClassReader original = new ClassReader(bytes);
			ClassNode originalNode = new ClassNode();
			original.accept(originalNode, ClassReader.EXPAND_FRAMES);

			for (MethodNode method : originalNode.methods) {
				if (method.name.equals("call")) {

					// Find the Object[] IMethodCall#call(Object[]) method
					MethodInsnNode find = new MethodInsnNode(
						INVOKEINTERFACE,
						"openperipheral/adapter/IMethodCall",
						"call",
						"([Ljava/lang/Object;)[Ljava/lang/Object;",
						true
					);

					Iterator<AbstractInsnNode> instructions = method.instructions.iterator();
					boolean success = false;
					while (instructions.hasNext()) {
						AbstractInsnNode instruction = instructions.next();
						if (ASMMatcher.insnEqual(find, instruction)) {
							success = true;

							// Before that, add an additional environment
							InsnList insert = new InsnList();
							insert.add(new LdcInsnNode(INetworkedAdapter.ARG_NETWORK));
							insert.add(new VarInsnNode(ALOAD, 0));
							insert.add(new FieldInsnNode(GETFIELD, patchType, "network", "Lorg/squiddev/cctweaks/core/network/NetworkAccessDelegate;"));
							insert.add(new MethodInsnNode(
								INVOKEINTERFACE,
								"openperipheral/adapter/IMethodCall",
								"setEnv",
								"(Ljava/lang/String;Ljava/lang/Object;)" + METHOD_CALL,
								true
							));

							// We want to get the previous one as this one will be ALOAD arguments
							method.instructions.insertBefore(instruction.getPrevious(), insert);
						}
					}

					if (success) {
						DebugLogger.debug("Added additional environments into " + className);
					} else {
						DebugLogger.error("Cannot inject additional environments into " + className);
					}

					break;
				}
			}

			ClassReader override = getSource(patchType + className.substring(classNameStart));
			if (override == null) return bytes;
			ClassWriter writer = new ClassWriter(0);

			originalNode.accept(new MergeVisitor(writer, override, context));

			DebugLogger.debug(MARKER, "Injected custom " + className);
			return writer.toByteArray();
		} catch (Exception e) {
			DebugLogger.error(MARKER, "Cannot replace " + className + ", falling back to default", e);
			return bytes;
		}
	}

	@Override
	public boolean matches(String className) {
		return className.equals(this.className);
	}
}
