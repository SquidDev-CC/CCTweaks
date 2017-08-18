package org.squiddev.cctweaks.core.patch;

import com.google.common.base.Objects;
import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import dan200.computercraft.shared.computer.core.ClientComputer;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.cctweaks.api.computer.IExtendedServerComputer;
import org.squiddev.cctweaks.core.patch.iface.IExtendedComputerTile;
import org.squiddev.patcher.visitors.MergeVisitor;

import javax.annotation.Nonnull;

/**
 * Ensures NBT data sync
 */
@MergeVisitor.Rename(
	from = "org/squiddev/cctweaks/core/patch/ServerComputer_Patch",
	to = "dan200/computercraft/shared/computer/core/ServerComputer"
)
public abstract class TileComputerBase_Patch extends TileComputerBase implements IExtendedComputerTile {
	private boolean hasDisk;
	private int diskId;

	@MergeVisitor.Stub
	public ServerComputer createServerComputer() {
		return null;
	}

	/**
	 * Detect whether various methods have changed and mark as dirty if so.
	 */
	@Override
	public void update() {
		if (!getWorld().isRemote) {
			ServerComputer computer = createServerComputer();
			if (computer != null) {
				boolean oldOn = m_on || m_startOn;

				if (m_startOn) {
					computer.turnOn();
					m_startOn = false;
				}

				computer.keepAlive();
				if (computer.hasOutputChanged()) updateOutput();

				boolean changed = false;

				// Sync various properties, ensuring that the data is saved if required

				int id = computer.getID();
				int oldId = m_computerID;
				if (id != oldId) {
					m_computerID = id;
					changed = true;
				}

				String label = computer.getLabel();
				String oldLabel = m_label;
				if (!Objects.equal(label, oldLabel)) {
					m_label = label;
					changed = true;
				}

				boolean on = ((IExtendedServerComputer) computer).isMostlyOn();
				if (on != oldOn) {
					m_on = on;
					changed = true;
				}

				if (changed) {
					markDirty();
				}
			}
		} else {
			ClientComputer computer = createClientComputer();
			if (computer != null && computer.hasOutputChanged()) updateBlock();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		native_readFromNBT(compound);
		if (compound.hasKey("rom_id", 99)) {
			hasDisk = true;
			diskId = compound.getInteger("rom_id");
		}
	}

	@MergeVisitor.Rename(from = {"readFromNBT", "func_145839_a"})
	@MergeVisitor.Stub
	public void native_readFromNBT(NBTTagCompound compound) {
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		native_writeToNBT(compound);
		if (hasDisk) {
			compound.setInteger("rom_id", diskId);
		} else {
			compound.removeTag("rom_id");
		}
		return compound;
	}

	@MergeVisitor.Rename(from = {"writeToNBT", "func_189515_b"})
	@MergeVisitor.Stub
	public NBTTagCompound native_writeToNBT(NBTTagCompound compound) {
		return compound;
	}

	public void writeDescription(@Nonnull NBTTagCompound tag) {
		super.writeDescription(tag);
		tag.setInteger("instanceID", createServerComputer().getInstanceID());
		if (hasDisk) tag.setInteger("rom_id", diskId);
	}

	public void readDescription(@Nonnull NBTTagCompound gag) {
		super.readDescription(gag);
		m_instanceID = gag.getInteger("instanceID");
		if (gag.hasKey("rom_id", 99)) {
			hasDisk = true;
			diskId = gag.getInteger("rom_id");
		}
	}

	public void setDiskId(int diskId) {
		this.hasDisk = true;
		this.diskId = diskId;

		ServerComputer_Patch computer = (ServerComputer_Patch) getServerComputer();
		if (computer != null) computer.setCustomRom(diskId);
	}

	public boolean hasDisk() {
		return hasDisk;
	}

	public int getDiskId() {
		return hasDisk ? diskId : -1;
	}
}
