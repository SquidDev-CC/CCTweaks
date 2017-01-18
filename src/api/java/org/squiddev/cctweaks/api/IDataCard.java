package org.squiddev.cctweaks.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A card that can be used to store/retrieve data
 */
public interface IDataCard {
	/**
	 * Variable to return if no data is stored
	 */
	String EMPTY = "cctweaks.data.empty";

	/**
	 * Set the current data for this stack
	 *
	 * @param stack The stack to set the data for
	 * @param type  The type of data this card stores. {@link #getType}
	 * @param data  The data that this card stores {@link #getData}
	 */
	void setSettings(@Nonnull ItemStack stack, @Nonnull String type, @Nullable NBTTagCompound data);

	/**
	 * Get the type this card stores.
	 *
	 * This will be translated using gui.tooltip.${name} and then ${name}.
	 *
	 * @param stack The stack to read the data from
	 * @return The type this card contains or {@link #EMPTY} if nothing is stored
	 */
	@Nonnull
	String getType(@Nonnull ItemStack stack);

	/**
	 * Get the data this card stores
	 *
	 * A string called "tooltip" will be displayed
	 * A string called "details" will be displayed if F3+H is on
	 *
	 * @param stack The stack to read the data from
	 * @return The data that is stored on this card
	 */
	@Nullable
	NBTTagCompound getData(@Nonnull ItemStack stack);

	/**
	 * Notify the player of a card event
	 *
	 * @param player  The player to notify
	 * @param message The message to notify the player with
	 */
	void notifyPlayer(@Nonnull EntityPlayer player, @Nonnull Messages message);

	enum Messages {
		/**
		 * Settings are loaded from the card
		 */
		Loaded,

		/**
		 * Settings are stored on the card
		 */
		Stored,

		/**
		 * Settings are cleared from the card
		 */
		Cleared;

		/**
		 * Get the chat message for this data
		 *
		 * @return The chat message
		 */
		@Nonnull
		public ITextComponent getChatMessage() {
			return new TextComponentTranslation("chat.cctweaks.data.messages." + this.toString());
		}
	}
}
