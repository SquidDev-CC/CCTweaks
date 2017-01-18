package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.terminal.TextBuffer;
import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.patcher.visitors.MergeVisitor;

public class Terminal_Patch extends Terminal {
	@MergeVisitor.Stub
	private int m_cursorX;
	@MergeVisitor.Stub
	private int m_cursorY;
	@MergeVisitor.Stub
	private boolean m_cursorBlink;
	@MergeVisitor.Stub
	private int m_cursorColour;
	@MergeVisitor.Stub
	private int m_cursorBackgroundColour;
	@MergeVisitor.Stub
	private TextBuffer[] m_text;
	@MergeVisitor.Stub
	private TextBuffer[] m_textColour;
	@MergeVisitor.Stub
	private TextBuffer[] m_backgroundColour;
	@MergeVisitor.Stub
	private int m_height;

	@MergeVisitor.Stub
	public Terminal_Patch() {
		super(-1, -1);
	}

	public void writeToNBT(NBTTagCompound nbttagcompound, boolean lines) {
		nbttagcompound.setInteger("term_cursorX", m_cursorX);
		nbttagcompound.setInteger("term_cursorY", m_cursorY);
		nbttagcompound.setBoolean("term_cursorBlink", m_cursorBlink);
		nbttagcompound.setInteger("term_textColour", m_cursorColour);
		nbttagcompound.setInteger("term_bgColour", m_cursorBackgroundColour);

		if (lines) {
			for (int i = 0; i < m_height; ++i) {
				nbttagcompound.setString("term_text_" + i, m_text[i].toString());
				nbttagcompound.setString("term_textColour_" + i, m_textColour[i].toString());
				nbttagcompound.setString("term_textBgColour_" + i, m_backgroundColour[i].toString());
			}
		}
	}
}
