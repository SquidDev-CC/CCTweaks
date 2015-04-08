package org.squiddev.cctweaks.core.asm.chickenlib;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.LinkedList;
import java.util.List;

public class ASMMatcher implements Opcodes {
	public static boolean varInsnEqual(VarInsnNode insn1, VarInsnNode insn2) {
		return insn1.var == -1 || insn2.var == -1 || insn1.var == insn2.var;
	}

	public static boolean methodInsnEqual(MethodInsnNode insn1, MethodInsnNode insn2) {
		return insn1.owner.equals(insn2.owner) && insn1.name.equals(insn2.name) && insn1.desc.equals(insn2.desc);
	}

	public static boolean fieldInsnEqual(FieldInsnNode insn1, FieldInsnNode insn2) {
		return insn1.owner.equals(insn2.owner) && insn1.name.equals(insn2.name) && insn1.desc.equals(insn2.desc);
	}

	public static boolean ldcInsnEqual(LdcInsnNode insn1, LdcInsnNode insn2) {
		return insn1.cst == null || insn2.cst == null || insn1.cst.equals(insn2.cst);
	}

	public static boolean typeInsnEqual(TypeInsnNode insn1, TypeInsnNode insn2) {
		return insn1.desc.equals("*") || insn2.desc.equals("*") || insn1.desc.equals(insn2.desc);
	}

	public static boolean iincInsnEqual(IincInsnNode node1, IincInsnNode node2) {
		return node1.var == node2.var && node1.incr == node2.incr;
	}

	public static boolean intInsnEqual(IntInsnNode node1, IntInsnNode node2) {
		return node1.operand == -1 || node2.operand == -1 || node1.operand == node2.operand;
	}

	public static boolean insnEqual(AbstractInsnNode node1, AbstractInsnNode node2) {
		if (node1.getOpcode() != node2.getOpcode())
			return false;

		switch (node2.getType()) {
			case AbstractInsnNode.VAR_INSN:
				return varInsnEqual((VarInsnNode) node1, (VarInsnNode) node2);
			case AbstractInsnNode.TYPE_INSN:
				return typeInsnEqual((TypeInsnNode) node1, (TypeInsnNode) node2);
			case AbstractInsnNode.FIELD_INSN:
				return fieldInsnEqual((FieldInsnNode) node1, (FieldInsnNode) node2);
			case AbstractInsnNode.METHOD_INSN:
				return methodInsnEqual((MethodInsnNode) node1, (MethodInsnNode) node2);
			case AbstractInsnNode.LDC_INSN:
				return ldcInsnEqual((LdcInsnNode) node1, (LdcInsnNode) node2);
			case AbstractInsnNode.IINC_INSN:
				return iincInsnEqual((IincInsnNode) node1, (IincInsnNode) node2);
			case AbstractInsnNode.INT_INSN:
				return intInsnEqual((IntInsnNode) node1, (IntInsnNode) node2);
			default:
				return true;
		}
	}

	public static boolean insnImportant(AbstractInsnNode insn) {
		switch (insn.getType()) {
			case AbstractInsnNode.LINE:
			case AbstractInsnNode.FRAME:
			case AbstractInsnNode.LABEL:
				return false;
			default:
				return true;
		}
	}

	public static List<InsnListSection> find(InsnListSection haystack, InsnListSection needle, boolean matchUnimportant) {
		LinkedList<InsnListSection> list = new LinkedList<InsnListSection>();
		for (int start = 0; start <= haystack.size() - needle.size(); start++) {
			InsnListSection section = matches(haystack.drop(start), needle, matchUnimportant);
			if (section != null) {
				list.add(section);
				start = section.end - 1;
			}
		}

		return list;
	}

	public static List<InsnListSection> find(InsnList haystack, InsnListSection needle, boolean excludeUnimportant) {
		return find(new InsnListSection(haystack), needle, excludeUnimportant);
	}

	public static InsnListSection matches(InsnListSection haystack, InsnListSection needle, boolean excludeUnimportant) {
		int h = 0, n = 0;
		for (; h < haystack.size() && n < needle.size(); h++) {
			AbstractInsnNode instruction = haystack.get(h);
			if (!excludeUnimportant && !insnImportant(instruction)) {
				continue;
			}

			if (!insnEqual(haystack.get(h), needle.get(n)))
				return null;
			n++;
		}
		if (n != needle.size())
			return null;

		return haystack.take(h);
	}

	public static InsnListSection findOnce(InsnListSection haystack, InsnListSection needle, boolean excludeUnimportant) {
		List<InsnListSection> list = find(haystack, needle, excludeUnimportant);
		if (list.size() != 1)
			throw new RuntimeException("Needle found " + list.size() + " times in Haystack:\n" + haystack + "\n\n" + needle);

		return list.get(0);
	}

	public static InsnListSection findOnce(InsnList haystack, InsnListSection needle, boolean excludeUnimportant) {
		return findOnce(new InsnListSection(haystack), needle, excludeUnimportant);
	}
}
