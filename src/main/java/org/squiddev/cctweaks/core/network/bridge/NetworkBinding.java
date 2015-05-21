package org.squiddev.cctweaks.core.network.bridge;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.cctweaks.api.IDataCard;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;

import java.util.Set;
import java.util.UUID;

/**
 * An implementation for {@link NetworkBindings} that saves the id to NBT
 */
public class NetworkBinding {
	protected static final String MSB = "bound_id_msb";
	protected static final String LSB = "bound_id_lsb";

	protected UUID id = UUID.randomUUID();
	protected IWorldNetworkNode node;

	public NetworkBinding(IWorldNetworkNode node) {
		this.node = node;
	}

	/**
	 * Add the position to the bindings
	 */
	public void add() {
		if (node.getPosition().getWorld() != null) NetworkBindings.addNode(id, node);
	}

	/**
	 * Remove the position from the bindings
	 */
	public void remove() {
		if (node.getPosition().getWorld() != null) NetworkBindings.removeNode(id, node);
	}

	/**
	 * Get all bound positions for this binding
	 *
	 * @return The positions for this binding
	 */
	public Set<IWorldNetworkNode> getNodes() {
		return NetworkBindings.getNodes(id);
	}

	public void setId(UUID newId) {
		remove();
		id = newId;
		add();
	}

	/**
	 * Save the UUID to a NBT tag
	 *
	 * @param tag The tag to save to
	 */
	public void save(NBTTagCompound tag) {
		tag.setLong(MSB, id.getMostSignificantBits());
		tag.setLong(LSB, id.getLeastSignificantBits());
	}

	/**
	 * Load the UUID from a NBT tag
	 *
	 * @param tag The tag to load from
	 * @return If data was loaded from the tag
	 */
	public boolean load(NBTTagCompound tag) {
		if (tag.hasKey(MSB) && tag.hasKey(LSB)) {
			UUID newId = new UUID(tag.getLong(MSB), tag.getLong(LSB));
			if (!newId.equals(id)) setId(newId);
			return true;
		}

		return false;
	}

	/**
	 * Save the UUID to a {@link IDataCard}
	 *
	 * @param stack The stack the card belongs to
	 * @param card  The card implementation
	 */
	public void save(ItemStack stack, IDataCard card) {
		NBTTagCompound data = new NBTTagCompound();
		save(data);

		data.setString("details", id.toString());
		card.setSettings(stack, NetworkBindings.BINDING_NAME, data);
	}

	/**
	 * Load the UUID from a {@link IDataCard}
	 *
	 * @param stack The stack the card belongs to
	 * @param card  The card implementation
	 * @return If the data was loaded from the card
	 */
	public boolean load(ItemStack stack, IDataCard card) {
		return card.getType(stack).equals(NetworkBindings.BINDING_NAME) && load(card.getData(stack));
	}
}
