package org.squiddev.cctweaks.api.network;

/**
 * A packet sent along a wired network
 *
 * Previously dan200.computercraft.shared.peripheral.modem.TileCable.Packet
 */
public final class Packet {
	/**
	 * The channel the packet is sent to
	 */
	public final int channel;

	/**
	 * The channel to reply to
	 */
	public final int replyChannel;

	/**
	 * Content of the packet
	 */
	public final Object payload;

	/**
	 * The sender of the packet
	 */
	public final Object senderObject;

	/**
	 * Construct a new packet
	 *
	 * @param channel      The channel the packet is sent to
	 * @param replyChannel The channel to reply to
	 * @param payload      Content of the packet
	 * @param senderObject The sender of the packet
	 */
	public Packet(int channel, int replyChannel, Object payload, Object senderObject) {
		this.channel = channel;
		this.replyChannel = replyChannel;
		this.payload = payload;
		this.senderObject = senderObject;
	}
}
