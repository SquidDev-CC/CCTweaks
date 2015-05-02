package org.squiddev.cctweaks.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

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
	void setSettings(ItemStack stack, String type, NBTTagCompound data);

	/**
	 * Get the type this card stores.
	 *
	 * This will be translated using gui.tooltip.${name} and then ${name}.
	 *
	 * @param stack The stack to read the data from
	 * @return The type this card contains or {@link #EMPTY} if nothing is stored
	 */
	String getType(ItemStack stack);

	/**
	 * Get the data this card stores
	 *
	 * A string called "tooltip" will be displayed
	 * A string called "details" will be displayed if F3+H is on
	 *
	 * @param stack The stack to read the data from
	 * @return The data that is stored on this card
	 */
	NBTTagCompound getData(ItemStack stack);

	/**
	 * Notify the player of a card event
	 *
	 * @param player  The player to notify
	 * @param message The message to notify the player with
	 */
	void notifyPlayer(EntityPlayer player, Messages message);

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
		public IChatComponent getChatMessage() {
			return new ChatComponentTranslation("chat.cctweaks.data.messages." + this.toString());
		}
	}
}
