package org.squiddev.cctweaks.core.asm.chickenlib;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;

import java.util.Iterator;
import java.util.Map;

public class InsnListSection implements Iterable<AbstractInsnNode> {
	public InsnList list;
	public int start;
	public int end;

	public InsnListSection(InsnList list, int start, int end) {
		this.list = list;
		this.start = start;
		this.end = end;
	}

	public InsnListSection(InsnList list, AbstractInsnNode first, AbstractInsnNode last) {
		this(list, list.indexOf(first), list.indexOf(last) + 1);
	}

	public InsnListSection(InsnList list) {
		this(list, 0, list.size());
	}

	public InsnListSection() {
		this(new InsnList());
	}

	public void accept(MethodVisitor mv) {
		for (AbstractInsnNode insn : this)
			insn.accept(mv);
	}

	public AbstractInsnNode getFirst() {
		return size() == 0 ? null : list.get(start);
	}

	public AbstractInsnNode getLast() {
		return size() == 0 ? null : list.get(end - 1);
	}

	public int size() {
		return end - start;
	}

	public AbstractInsnNode get(int i) {
		return list.get(start + i);
	}

	public void set(int i, AbstractInsnNode insn) {
		list.set(get(i), insn);
	}

	public void remove(int i) {
		list.remove(get(i));
		end--;
	}

	public void replace(AbstractInsnNode location, AbstractInsnNode insn) {
		list.set(location, insn);
	}

	public void add(AbstractInsnNode insn) {
		list.add(insn);
		end++;
	}

	public void shift(int shift) {
		start += shift;
		end += shift;
	}

	public void insertBefore(InsnList insns) {
		int s = insns.size();
		if (this.list.size() == 0)
			list.insert(insns);
		else
			list.insertBefore(list.get(start), insns);
		start += s;
		end += s;
	}

	public void insert(InsnList insns) {
		if (end == 0)
			list.insert(insns);
		else
			list.insert(list.get(end - 1), insns);
	}

	public void replace(InsnList insns) {
		int s = insns.size();
		remove();
		insert(insns);
		end = start + s;
	}

	public void remove() {
		while (end != start)
			remove(0);
	}

	public InsnListSection drop(int n) {
		return slice(n, size());
	}

	public InsnListSection take(int n) {
		return slice(0, n);
	}

	public InsnListSection slice(int start, int end) {
		return new InsnListSection(list, this.start + start, this.start + end);
	}

	public InsnListSection copy(Map<LabelNode, LabelNode> labelMap) {
		InsnListSection copy = new InsnListSection();
		for (AbstractInsnNode insn : this)
			copy.add(insn.clone(labelMap));

		return copy;
	}

	@Override
	public Iterator<AbstractInsnNode> iterator() {
		return new InsnListSectionIterator();
	}

	private class InsnListSectionIterator implements Iterator<AbstractInsnNode> {
		int i = 0;

		@Override
		public boolean hasNext() {
			return i < size();
		}

		@Override
		public AbstractInsnNode next() {
			return get(i++);
		}

		@Override
		public void remove() {
			InsnListSection.this.remove(--i);
		}
	}
}
