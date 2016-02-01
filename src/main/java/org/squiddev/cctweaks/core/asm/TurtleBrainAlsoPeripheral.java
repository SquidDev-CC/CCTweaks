package org.squiddev.cctweaks.core.asm;

import dan200.computercraft.shared.computer.core.ServerComputer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.visitors.FindingVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * Allow a turtle upgrade being both a peripheral and a turtle tool
 *
 * @see dan200.computercraft.shared.turtle.core.TurtleBrain#updatePeripherals(ServerComputer)
 */
public class TurtleBrainAlsoPeripheral implements IPatcher {
	private static final String EXTENDED_UPGRADE = "org/squiddev/cctweaks/api/turtle/IExtendedTurtleUpgrade";

	@Override
	public boolean matches(String className) {
		return className.equals("dan200.computercraft.shared.turtle.core.TurtleBrain") || className.equals("dan200.computercraft.shared.turtle.blocks.TileTurtle");
	}

	@Override
	public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
		if (className.equals("dan200.computercraft.shared.turtle.core.TurtleBrain")) {
			return new FindingVisitor(delegate,
				new MethodInsnNode(INVOKEINTERFACE, "dan200/computercraft/api/turtle/ITurtleUpgrade", "getType", "()Ldan200/computercraft/api/turtle/TurtleUpgradeType;", true),
				new FieldInsnNode(GETSTATIC, "dan200/computercraft/api/turtle/TurtleUpgradeType", "Peripheral", "Ldan200/computercraft/api/turtle/TurtleUpgradeType;"),
				new JumpInsnNode(IF_ACMPNE, null)
			) {
				@Override
				public void handle(InsnList nodes, MethodVisitor visitor) {
					inject(visitor, nodes, 6);
				}
			}.onMethod("updatePeripherals").once().mustFind();
		} else {
			return new FindingVisitor(delegate,
				new MethodInsnNode(INVOKEINTERFACE, "dan200/computercraft/api/turtle/ITurtleUpgrade", "getType", "()Ldan200/computercraft/api/turtle/TurtleUpgradeType;", true),
				new FieldInsnNode(GETSTATIC, "dan200/computercraft/api/turtle/TurtleUpgradeType", "Peripheral", "Ldan200/computercraft/api/turtle/TurtleUpgradeType;"),
				new JumpInsnNode(IF_ACMPNE, null)
			) {
				@Override
				public void handle(InsnList nodes, MethodVisitor visitor) {
					inject(visitor, nodes, 2);
				}
			}.onMethod("hasPeripheralUpgradeOnSide").once().mustFind();
		}
	}

	private static void inject(MethodVisitor visitor, InsnList nodes, int slot) {
		Label add = new Label();

		JumpInsnNode jump = (JumpInsnNode) nodes.getLast();
		nodes.remove(jump);
		Label exit = jump.label.getLabel();

		// if(upgrade.getType() == TurtleUpgradeType.Peripheral)
		nodes.accept(visitor);
		visitor.visitJumpInsn(IF_ACMPEQ, add);

		// if(upgrade instanceof EXTENDED_UPGRADE && upgrade.alsoPeripheral())
		visitor.visitVarInsn(ALOAD, slot);
		visitor.visitTypeInsn(INSTANCEOF, EXTENDED_UPGRADE);
		visitor.visitJumpInsn(IFEQ, exit);

		visitor.visitVarInsn(ALOAD, slot);
		visitor.visitTypeInsn(CHECKCAST, EXTENDED_UPGRADE);
		visitor.visitMethodInsn(INVOKEINTERFACE, EXTENDED_UPGRADE, "alsoPeripheral", "()Z", true);
		visitor.visitJumpInsn(IFEQ, exit);

		visitor.visitLabel(add);
	}
}
