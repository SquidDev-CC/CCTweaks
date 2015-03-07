package squiddev.cctweaks.core.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import squiddev.cctweaks.core.reference.Config;

public class PatchTurtle implements Opcodes {
	/**
	 * Replace the Refuel command with one using CC-Tweaks' {@see squiddev.cctweaks.core.turtle.ITurtleRefuelSource}
	 */
	public static byte[] patchRefuelCommand(byte[] bytes) {
		// Setup class reader
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		for (MethodNode method : classNode.methods) {
			if (method.name.equals("execute") && method.desc.equals("(Ldan200/computercraft/api/turtle/ITurtleAccess;)Ldan200/computercraft/api/turtle/TurtleCommandResult;")) {

				/*
					Local variables:
					this: 0
					ITurtleAccess: 1
					ItemStack: 2
					Iterator: 3
					ITurtleRefuelSource: 4
				*/

				method.instructions.clear();
				method.localVariables = null;

				/*
					ItemStack stack = this.turtle.getInventory().getStackInSlot(turtle.getSelectedSlot());
					for (ITurtleRefuelSource source : TurtleRefuelList.refuelList) {
						if (source.canRefuel(turtle, stack, m_limit)) {
							if (m_limit == 0) {
								return TurtleCommandResult.success();
							} else {
								turtle.addFuel(source.refuel(turtle, stack, m_limit));
								turtle.playAnimation(TurtleAnimation.Wait);
								return TurtleCommandResult.success();
							}
						}
					}

					return TurtleCommandResult.failure("Cannot refuel from this");
				 */

				// Get current stack
				method.visitVarInsn(ALOAD, 1);
				method.visitMethodInsn(INVOKEINTERFACE, "dan200/computercraft/api/turtle/ITurtleAccess", "getInventory", "()Lnet/minecraft/inventory/IInventory;", true);
				method.visitVarInsn(ALOAD, 1);
				method.visitMethodInsn(INVOKEINTERFACE, "dan200/computercraft/api/turtle/ITurtleAccess", "getSelectedSlot", "()I", true);
				method.visitMethodInsn(INVOKEINTERFACE, "net/minecraft/inventory/IInventory", "getStackInSlot", "(I)Lnet/minecraft/item/ItemStack;", true);
				method.visitVarInsn(ASTORE, 2);

				// Get iterator
				method.visitFieldInsn(GETSTATIC, "squiddev/cctweaks/core/registry/TurtleRefuelList", "refuelList", "Ljava/util/List;");
				method.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true);
				method.visitVarInsn(ASTORE, 3);

				// Prepare iterator
				Label labelIteratorStart = new Label();
				method.visitLabel(labelIteratorStart);
				method.visitFrame(Opcodes.F_APPEND, 2, new Object[]{"net/minecraft/item/ItemStack", "java/util/Iterator"}, 0, null);
				method.visitVarInsn(ALOAD, 3);
				method.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);

				Label labelReturnFailure = new Label();

				// Load next item
				method.visitJumpInsn(IFEQ, labelReturnFailure);
				method.visitVarInsn(ALOAD, 3);
				method.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
				method.visitTypeInsn(CHECKCAST, "squiddev/cctweaks/core/turtle/ITurtleRefuelSource");
				method.visitVarInsn(ASTORE, 4);

				// check if can refuel
				method.visitVarInsn(ALOAD, 4);
				method.visitVarInsn(ALOAD, 1);
				method.visitVarInsn(ALOAD, 2);
				method.visitVarInsn(ALOAD, 0);
				method.visitFieldInsn(GETFIELD, "dan200/computercraft/shared/turtle/core/TurtleRefuelCommand", "m_limit", "I");
				method.visitMethodInsn(INVOKEINTERFACE, "squiddev/cctweaks/core/turtle/ITurtleRefuelSource", "canRefuel", "(Ldan200/computercraft/api/turtle/ITurtleAccess;Lnet/minecraft/item/ItemStack;I)Z", true);

				Label labelIteratorEnd = new Label();
				method.visitJumpInsn(IFEQ, labelIteratorEnd);

				method.visitVarInsn(ALOAD, 0);
				method.visitFieldInsn(GETFIELD, "dan200/computercraft/shared/turtle/core/TurtleRefuelCommand", "m_limit", "I");

				// If m_limit != 0 then goto actual refuel
				Label labelActualRefuel = new Label();
				method.visitJumpInsn(IFNE, labelActualRefuel);

				// Fake refuel
				method.visitMethodInsn(INVOKESTATIC, "dan200/computercraft/api/turtle/TurtleCommandResult", "success", "()Ldan200/computercraft/api/turtle/TurtleCommandResult;", false);
				method.visitInsn(ARETURN);

				// Refueling:
				method.visitLabel(labelActualRefuel);

				// Refuel and add fuel
				method.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"squiddev/cctweaks/core/turtle/ITurtleRefuelSource"}, 0, null);
				method.visitVarInsn(ALOAD, 1);
				method.visitVarInsn(ALOAD, 4);
				method.visitVarInsn(ALOAD, 1);
				method.visitVarInsn(ALOAD, 2);
				method.visitVarInsn(ALOAD, 0);
				method.visitFieldInsn(GETFIELD, "dan200/computercraft/shared/turtle/core/TurtleRefuelCommand", "m_limit", "I");
				method.visitMethodInsn(INVOKEINTERFACE, "squiddev/cctweaks/core/turtle/ITurtleRefuelSource", "refuel", "(Ldan200/computercraft/api/turtle/ITurtleAccess;Lnet/minecraft/item/ItemStack;I)I", true);
				method.visitMethodInsn(INVOKEINTERFACE, "dan200/computercraft/api/turtle/ITurtleAccess", "addFuel", "(I)V", true);

				// Play 'wait' animation
				method.visitVarInsn(ALOAD, 1);
				method.visitFieldInsn(GETSTATIC, "dan200/computercraft/api/turtle/TurtleAnimation", "Wait", "Ldan200/computercraft/api/turtle/TurtleAnimation;");
				method.visitMethodInsn(INVOKEINTERFACE, "dan200/computercraft/api/turtle/ITurtleAccess", "playAnimation", "(Ldan200/computercraft/api/turtle/TurtleAnimation;)V", true);

				// Return success
				method.visitMethodInsn(INVOKESTATIC, "dan200/computercraft/api/turtle/TurtleCommandResult", "success", "()Ldan200/computercraft/api/turtle/TurtleCommandResult;", false);
				method.visitInsn(ARETURN);


				// Next loop item
				method.visitLabel(labelIteratorEnd);
				method.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
				method.visitJumpInsn(GOTO, labelIteratorStart);

				// Return error - no refuel
				method.visitLabel(labelReturnFailure);
				method.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
				method.visitLdcInsn("Cannot refuel from this");
				method.visitMethodInsn(INVOKESTATIC, "dan200/computercraft/api/turtle/TurtleCommandResult", "failure", "(Ljava/lang/String;)Ldan200/computercraft/api/turtle/TurtleCommandResult;", false);
				method.visitInsn(ARETURN);
			}
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	/**
	 * Disable a particular turtle command
	 */
	public static byte[] disableTurtleCommand(String name, byte[] bytes) {
		if (!Config.turtleDisabledActions.contains(name.toLowerCase())) return bytes;

		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		for (MethodNode method : classNode.methods) {
			if (method.name.equals("execute") && method.desc.equals("(Ldan200/computercraft/api/turtle/ITurtleAccess;)Ldan200/computercraft/api/turtle/TurtleCommandResult;")) {
				method.instructions.clear();
				method.localVariables = null;

				method.visitLdcInsn("Action disabled");
				method.visitMethodInsn(INVOKESTATIC, "dan200/computercraft/api/turtle/TurtleCommandResult", "failure", "(Ljava/lang/String;)Ldan200/computercraft/api/turtle/TurtleCommandResult;", false);
				method.visitInsn(ARETURN);
			}
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		return writer.toByteArray();
	}
}
