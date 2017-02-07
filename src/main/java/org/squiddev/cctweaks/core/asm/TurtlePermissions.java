package org.squiddev.cctweaks.core.asm;

import dan200.computercraft.api.turtle.ITurtleAccess;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.squiddev.patcher.transformer.IPatcher;

/**
 * Patches {@link dan200.computercraft.shared.turtle.upgrades.TurtleTool#dig(ITurtleAccess, EnumFacing)} to use
 * {@link org.squiddev.cctweaks.core.turtle.TurtleHooks#isBlockBreakable(World, BlockPos, EntityPlayer)} instead of
 * {@link dan200.computercraft.ComputerCraft#isBlockEditable(World, BlockPos, EntityPlayer)}.
 */
public class TurtlePermissions implements IPatcher {
	@Override
	public boolean matches(String className) {
		return className.equals("dan200.computercraft.shared.turtle.upgrades.TurtleTool");
	}

	@Override
	public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
		return new ClassVisitor(Opcodes.ASM5, delegate) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
				if (name.equals("dig")) {
					return new MethodVisitor(api, visitor) {
						@Override
						public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
							if (owner.equals("dan200/computercraft/ComputerCraft") && name.equals("isBlockEditable")) {
								super.visitMethodInsn(opcode, "org/squiddev/cctweaks/core/turtle/TurtleHooks", "isBlockBreakable", desc, itf);
							} else {
								super.visitMethodInsn(opcode, owner, name, desc, itf);
							}
						}
					};
				} else {
					return visitor;
				}
			}
		};
	}
}
