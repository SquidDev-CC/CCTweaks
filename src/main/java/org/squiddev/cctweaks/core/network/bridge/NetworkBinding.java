package org.squiddev.cctweaks.core.network.bridge;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.cctweaks.api.IDataCard;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.core.network.AbstractWorldNode;

import java.util.Set;
import java.util.UUID;

/**
 * An implementation for {@link NetworkBindings} that saves the id to NBT
 */
public class NetworkBinding extends AbstractWorldNode {
	public static final String MSB = "bound_id_msb";
	public static final String LSB = "bound_id_lsb";
	public static final String ID = "bound_id";

	protected UUID uuid = UUID.randomUUID();
	protected Integer id = null;
	protected final IWorldPosition position;

	public NetworkBinding(IWorldPosition position) {
		this.position = position;
	}

	/**
	 * Add the position to the bindings
	 */
	public void add() {
		if (getPosition().getBlockAccess() != null) {
			NetworkBindings.addNode(uuid, this);
			if (id != null) NetworkBindings.addNode(id, this);
		}
	}

	/**
	 * Remove the position from the bindings
	 */
	public void remove() {
		NetworkBindings.removeNode(uuid, this);
		if (id != null) NetworkBindings.removeNode(id, this);
	}

	@Override
	public Set<INetworkNode> getConnectedNodes() {
		Set<INetworkNode> nodes = super.getConnectedNodes();

		nodes.addAll(NetworkBindings.getNodes(uuid));
		if (id != null) nodes.addAll(NetworkBindings.getNodes(id));

		nodes.remove(this);
		return nodes;
	}

	@Override
	public void connect() {
		NetworkBindings.addNode(uuid, this);
		if (id != null) NetworkBindings.addNode(id, this);
		super.connect();
	}

	@Override
	public void destroy() {
		NetworkBindings.removeNode(uuid, this);
		if (id != null) NetworkBindings.removeNode(id, this);
		super.destroy();
	}

	public void setUuid(UUID newId) {
		remove();
		uuid = newId;
		add();
	}

	public void setId(int newId) {
		remove();
		id = newId;
		add();
	}

	public UUID getUuid() {
		return uuid;
	}

	public Integer getId() {
		return id;
	}

	public void removeId() {
		remove();
		id = null;
	}

	/**
	 * Save the UUID to a NBT tag
	 *
	 * @param tag The tag to save to
	 */
	public void save(NBTTagCompound tag) {
		tag.setLong(MSB, uuid.getMostSignificantBits());
		tag.setLong(LSB, uuid.getLeastSignificantBits());
		if (id != null) {
			tag.setInteger(ID, id);
		} else {
			tag.removeTag(ID);
		}
	}

	/**
	 * Load the UUID from a NBT tag
	 *
	 * @param tag The tag to load from
	 * @return If data was loaded from the tag
	 */
	public boolean load(NBTTagCompound tag) {
		boolean loaded = false;

		if (tag.hasKey(MSB) && tag.hasKey(LSB)) {
			UUID newId = new UUID(tag.getLong(MSB), tag.getLong(LSB));
			if (!newId.equals(uuid)) setUuid(newId);
			loaded = true;
		}

		if (tag.hasKey(ID)) {
			setId(tag.getInteger(ID));
			loaded = true;
		} else {
			removeId();
		}

		return loaded;
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

		data.setString("details", uuid.toString());
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

	@Override
	public IWorldPosition getPosition() {
		return position;
	}

	@Override
	public String toString() {
		return "Binding: " + super.toString();
	}
}
