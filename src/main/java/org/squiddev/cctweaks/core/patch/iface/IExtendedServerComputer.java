package org.squiddev.cctweaks.core.patch.iface;

import dan200.computercraft.shared.network.ComputerCraftPacket;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public interface IExtendedServerComputer {
	ComputerCraftPacket createStatePacket();

	void writeDescription(NBTTagCompound tag, boolean withTerminal);

	FMLProxyPacket encode(ComputerCraftPacket packet);
}
