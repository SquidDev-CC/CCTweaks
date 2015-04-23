package org.squiddev.cctweaks.api.network;

/**
 * A packet sent along a wired network
 *
 * Previously dan200.computercraft.shared.peripheral.modem.TileCable.Packet
 */
public class Packet {
	public final int channel;
	public final int replyChannel;
	public final Object payload;
	public final Object senderObject;

	public Packet(int channel, int replyChannel, Object payload, Object senderObject) {
		this.channel = channel;
		this.replyChannel = replyChannel;
		this.payload = payload;
		this.senderObject = senderObject;
	}
}
