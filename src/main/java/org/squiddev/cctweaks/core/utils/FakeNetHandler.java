package org.squiddev.cctweaks.core.utils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.*;
import net.minecraft.network.play.client.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import javax.crypto.SecretKey;

public class FakeNetHandler extends NetHandlerPlayServer {
	public static class FakeNetworkManager extends NetworkManager {
		public FakeNetworkManager() {
			super(EnumPacketDirection.CLIENTBOUND);
		}

		@Override
		public void channelActive(ChannelHandlerContext context) throws Exception {
		}

		@Override
		public void setConnectionState(@Nonnull EnumConnectionState state) {
		}

		@Override
		public void channelInactive(ChannelHandlerContext context) {
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext context, @Nonnull Throwable e) {
		}

		@Override
		public void setNetHandler(INetHandler handler) {
		}

		@Override
		public void processReceivedPackets() {
		}

		@Override
		public void closeChannel(@Nonnull ITextComponent channel) {
		}

		@Override
		public boolean isLocalChannel() {
			return false;
		}


		@Override
		public void enableEncryption(SecretKey key) {
		}

		@Override
		public boolean isChannelOpen() {
			return false;
		}

		@Nonnull
		@Override
		public INetHandler getNetHandler() {
			return null;
		}

		@Nonnull
		@Override
		public ITextComponent getExitMessage() {
			return null;
		}

		@Override
		public void disableAutoRead() {
		}

		@Nonnull
		@Override
		public Channel channel() {
			return null;
		}
	}


	public FakeNetHandler(FakePlayer player) {
		this(FMLCommonHandler.instance().getMinecraftServerInstance(), player);
	}

	public FakeNetHandler(MinecraftServer server, FakePlayer player) {
		super(server, new FakeNetworkManager(), player);
	}

	@Override
	public void kickPlayerFromServer(@Nonnull String player) {
	}

	@Override
	public void processInput(CPacketInput packet) {
	}

	@Override
	public void processPlayer(CPacketPlayer packet) {
	}

	@Override
	public void setPlayerLocation(double x, double y, double z, float yaw, float pitch) {
	}

	@Override
	public void processPlayerDigging(CPacketPlayerDigging packet) {
	}

	@Override
	public void processPlayerBlockPlacement(CPacketPlayerTryUseItem packet) {
	}

	@Override
	public void onDisconnect(@Nonnull ITextComponent chat) {
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void sendPacket(@Nonnull final Packet packet) {

	}

	@Override
	public void processHeldItemChange(CPacketHeldItemChange packet) {
	}

	@Override
	public void processChatMessage(@Nonnull CPacketChatMessage packet) {
	}

	@Override
	public void processEntityAction(CPacketEntityAction packet) {
	}

	@Override
	public void processUseEntity(CPacketUseEntity packet) {
	}

	@Override
	public void processClientStatus(CPacketClientStatus packet) {
	}

	@Override
	public void processCloseWindow(@Nonnull CPacketCloseWindow packet) {
	}

	@Override
	public void processClickWindow(CPacketClickWindow packet) {
	}

	@Override
	public void processEnchantItem(CPacketEnchantItem packet) {
	}

	@Override
	public void processCreativeInventoryAction(@Nonnull CPacketCreativeInventoryAction packet) {
	}

	@Override
	public void processConfirmTransaction(@Nonnull CPacketConfirmTransaction packet) {
	}

	@Override
	public void processUpdateSign(CPacketUpdateSign packet) {
	}

	@Override
	public void processKeepAlive(CPacketKeepAlive packet) {
	}

	@Override
	public void processPlayerAbilities(CPacketPlayerAbilities packet) {
	}

	@Override
	public void processTabComplete(CPacketTabComplete packet) {
	}

	@Override
	public void processClientSettings(@Nonnull CPacketClientSettings packet) {
	}

	@Override
	public void processCustomPayload(CPacketCustomPayload packet) {
	}
}
