package org.squiddev.cctweaks.integration.multipart;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.cctweaks.core.FmlEvents;

/**
 * Multipart equivalent of {@link org.squiddev.cctweaks.blocks.TileLazyNBT}
 */
public abstract class PartLazyNBT extends PartBase {
	private NBTTagCompound lazyTag;

	/**
	 * Lazy load the NBT tag
	 *
	 * @param tag The NBT tag to load
	 */
	public abstract void loadLazy(NBTTagCompound tag);

	/**
	 * The fields that the tag stores.
	 *
	 * Used in the rare case that we are saving without having had an update tick
	 *
	 * @return The list of fields to keep
	 */
	public abstract Iterable<String> getFields();

	@Override
	public void onWorldJoin() {
		super.onWorldJoin();
		FmlEvents.schedule(new Runnable() {
			@Override
			public void run() {
				if (lazyTag != null) {
					loadLazy(lazyTag);
					lazyTag = null;
				}
			}
		});
	}

	@Override
	public void load(NBTTagCompound tag) {
		super.load(tag);
		lazyTag = tag;
	}

	@Override
	public void save(NBTTagCompound tag) {
		super.save(tag);
		if (lazyTag != null) {
			for (String field : getFields()) {
				NBTBase fieldTag = lazyTag.getTag(field);
				if (fieldTag != null) tag.setTag(field, fieldTag);
			}
		}
	}
}
