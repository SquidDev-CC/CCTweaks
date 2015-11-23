package org.squiddev.cctweaks.core.asm.binary;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.TypeInsnNode;
import org.squiddev.patcher.transformer.ClassMerger;
import org.squiddev.patcher.transformer.TransformationChain;
import org.squiddev.patcher.visitors.FindingVisitor;

import java.util.Arrays;

import static org.objectweb.asm.Opcodes.*;

/**
 * Utilities for injecting binary patches
 */
public final class BinaryUtils {
	public static final String LUA_CONVERTER = "org/squiddev/cctweaks/core/lua/LuaConverter";
	public static final String BINARY_OBJECT = "org/squiddev/cctweaks/api/lua/IBinaryHandler";

	public static final String DELEGATOR = "org/squiddev/cctweaks/api/lua/ArgumentDelegator";
	public static final String DELEGATE_OBJECT = "(Ldan200/computercraft/api/lua/ILuaObject;Ldan200/computercraft/api/lua/ILuaContext;ILorg/squiddev/cctweaks/api/lua/IArguments;)[Ljava/lang/Object;";
	public static final String DELEGATE_PERIPHERAL = "(Ldan200/computercraft/api/peripheral/IPeripheral;Ldan200/computercraft/api/peripheral/IComputerAccess;Ldan200/computercraft/api/lua/ILuaContext;ILorg/squiddev/cctweaks/api/lua/IArguments;)[Ljava/lang/Object;";

	private BinaryUtils() {
		throw new RuntimeException("Cannot create instance of BinaryUtils");
	}

	private static String[] addInterface(String[] interfaces) {
		String[] newInterfaces = Arrays.copyOf(interfaces, interfaces.length + 1);
		newInterfaces[interfaces.length] = BINARY_OBJECT;
		return newInterfaces;
	}

	/**
	 * Add a binary interface to a class
	 *
	 * @param visitor The original visitor
	 * @return The new visitor
	 */
	public static ClassVisitor withBinaryInterface(ClassVisitor visitor) {
		return new ClassVisitor(ASM5, visitor) {
			@Override
			public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
				super.visit(version, access, name, signature, superName, addInterface(interfaces));
			}
		};
	}

	/**
	 * Replace string casts with byte array casts then string creation
	 *
	 * @param visitor The original visitor
	 * @return The new visitor
	 */
	public static ClassVisitor withStringCasts(ClassVisitor visitor, String... methodNames) {
		FindingVisitor finder = new FindingVisitor(
			visitor,
			new TypeInsnNode(INSTANCEOF, "java/lang/String")
		) {
			@Override
			public void handle(InsnList nodes, MethodVisitor visitor) {
				visitor.visitTypeInsn(INSTANCEOF, "[B");
			}
		}.onMethod("callMethod");
		for (String name : methodNames) {
			finder.onMethod(name);
		}

		finder = new FindingVisitor(
			finder,
			new TypeInsnNode(CHECKCAST, "java/lang/String")
		) {
			@Override
			public void handle(InsnList nodes, MethodVisitor visitor) {
				visitor.visitTypeInsn(CHECKCAST, "[B");
				visitor.visitMethodInsn(INVOKESTATIC, LUA_CONVERTER, "decodeString", "([B)Ljava/lang/String;", false);
			}
		}.onMethod("callMethod");
		for (String name : methodNames) {
			finder.onMethod(name);
		}

		return finder;
	}

	/**
	 * Inject all binary patches into the chain
	 *
	 * @param chain The chain to inject
	 * @return The chain that has been injected
	 */
	public static TransformationChain inject(TransformationChain chain) {
		chain.add(new BinaryGeneric());
		chain.add(new BinaryMachine());
		chain.add(new BinaryFS());

		// HTTP rewrite
		chain.add(new ClassMerger("dan200.computercraft.core.apis.HTTPAPI", "org.squiddev.cctweaks.core.patch.HTTPAPI_Patch"));
		chain.add(new ClassMerger("dan200.computercraft.core.apis.HTTPRequest", "org.squiddev.cctweaks.core.patch.HTTPRequest_Patch"));

		// FS handle rewrites
		chain.add(new ClassMerger(BinaryFS.READER_OBJECT, BinaryFS.READER_OBJECT));
		chain.add(new ClassMerger(BinaryFS.WRITER_OBJECT, BinaryFS.WRITER_OBJECT));
		chain.add(new ClassMerger("dan200.computercraft.core.filesystem.FileSystem", "org.squiddev.cctweaks.core.patch.binfs.FileSystem_Patch"));

		return chain;
	}
}
