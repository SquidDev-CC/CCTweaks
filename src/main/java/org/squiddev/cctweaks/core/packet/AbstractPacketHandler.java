package org.squiddev.cctweaks.core.packet;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.core.registry.IModule;

public class AbstractPacketHandler<T extends AbstractPacketHandler.IPacket> implements IMessageHandler<T, IMessage>, IModule {
	private final int id;
	private final Side side;
	private final Class<T> type;

	public AbstractPacketHandler(int id, Class<T> type) {
		this(id, Side.CLIENT, type);
	}

	public AbstractPacketHandler(int id, Side side, Class<T> type) {
		this.id = id;
		this.side = side;
		this.type = type;
	}

	@Override
	public void preInit() {
		CCTweaks.network.registerMessage(this, type, id, side);
	}

	@Override
	public IMessage onMessage(T message, MessageContext ctx) {
		return message.handle(ctx);
	}

	public interface IPacket extends IMessage {
		IMessage handle(MessageContext ctx);
	}
}
