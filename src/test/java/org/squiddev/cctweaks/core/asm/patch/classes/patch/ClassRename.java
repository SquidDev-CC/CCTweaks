package org.squiddev.cctweaks.core.asm.patch.classes.patch;

import org.squiddev.cctweaks.core.asm.patch.MergeVisitor;

/**
 * Used to test {@link org.squiddev.cctweaks.core.asm.patch.MergeVisitor.Rename}
 */
@MergeVisitor.Rename(from = "org/squiddev/cctweaks/core/asm/patch/classes/patch/Base$Foo", to = "org/squiddev/cctweaks/core/asm/patch/classes/patch/Base$Bar")
public class ClassRename extends Base {
}
