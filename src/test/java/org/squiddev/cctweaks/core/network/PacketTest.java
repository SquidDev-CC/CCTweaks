package org.squiddev.cctweaks.core.network;

import com.google.gson.Gson;
import dan200.computercraft.api.network.IPacketSender;
import dan200.computercraft.api.network.Packet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNodeHost;
import org.squiddev.cctweaks.core.network.mock.BasicNetwork;
import org.squiddev.cctweaks.core.network.mock.KeyedNetworkNode;
import org.squiddev.cctweaks.core.network.mock.NodeTile;

import javax.annotation.Nonnull;
import java.io.InputStreamReader;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Tests various facts about the network visitor
 */
@RunWith(Parameterized.class)
public class PacketTest {
	private final TestData data;

	public PacketTest(String name, TestData data) {
		this.data = data;
	}

	@Parameterized.Parameters(name = "{0}")
	public static Object[][] data() {
		TestData[] data = new Gson().fromJson(
			new InputStreamReader(PacketTest.class.getResourceAsStream("data.json")),
			TestData[].class
		);

		Object[][] result = new Object[data.length][];
		for (int i = 0; i < data.length; i++) {
			TestData item = data[i];
			result[i] = new Object[]{
				item.name,
				item
			};
		}

		return result;
	}

	@Test
	public void testCounts() {
		if (data.counts == null) return;

		BasicNetwork network = new BasicNetwork(data);
		network.reset();
		((NodeTile) network.getTileEntity(0, 0, 0)).node.doInvalidate();

		for (Map.Entry<BlockPos, KeyedNetworkNode> location : network) {
			Integer count = data.counts.get(location.getValue().key);
			if (count != null) {
				assertEquals("Location " + location, count.intValue(), location.getValue().invalidated());
			}
		}
	}

	@Test
	public void testDistance() {
		if (data.distance == null) return;

		BasicNetwork network = new BasicNetwork(data);
		network.reset();

		IWorldNetworkNode node = ((IWorldNetworkNodeHost) network.getTileEntity(0, 0, 0)).getNode();
		node.getAttachedNetwork().transmitPacket(node, new Packet(0, 0, null, new Sender(node)));

		for (Map.Entry<BlockPos, KeyedNetworkNode> location : network) {
			Integer distance = data.distance.get(location.getValue().key);
			if (distance != null) {
				assertEquals("Location " + location, distance, location.getValue().distance(), 0.01);
			}
		}
	}

	public final class TestData {
		public String name;
		public String[] map;
		public Map<String, Integer> counts;
		public Map<String, Integer> distance;
	}

	public final class Sender implements IPacketSender {
		private final IWorldNetworkNode node;

		public Sender(IWorldNetworkNode node) {
			this.node = node;
		}

		@Nonnull
		@Override
		public World getWorld() {
			return (World) node.getPosition().getBlockAccess();
		}

		@Nonnull
		@Override
		public Vec3d getPosition() {
			return new Vec3d(node.getPosition().getPosition());
		}

		@Nonnull
		@Override
		public String getSenderID() {
			return node.toString();
		}
	}
}
