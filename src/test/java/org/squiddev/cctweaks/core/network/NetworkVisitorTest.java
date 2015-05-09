package org.squiddev.cctweaks.core.network;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.squiddev.cctweaks.api.network.ISearchLoc;
import org.squiddev.cctweaks.api.network.NetworkAPI;
import org.squiddev.cctweaks.core.network.mock.BasicNetwork;
import org.squiddev.cctweaks.core.network.mock.CountingNetworkNode;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Tests various facts about the network visitor
 */
@RunWith(Parameterized.class)
public class NetworkVisitorTest {
	private final BasicNetwork network;

	public NetworkVisitorTest(String name, BasicNetwork network) {
		this.network = network;
	}

	@Parameterized.Parameters(name = "{0}")
	public static List<Object[]> data() {
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();

		JsonObject object = parser.parse(
			new InputStreamReader(NetworkVisitorTest.class.getResourceAsStream("data.json"))
		).getAsJsonObject();

		List<Object[]> result = new ArrayList<Object[]>();

		for (Map.Entry<String, JsonElement> items : object.entrySet()) {
			result.add(new Object[]{
				items.getKey(),
				new BasicNetwork(gson.fromJson(items.getValue(), String[].class))
			});
		}

		return result;
	}

	@Test
	public void testVisitOnce() {
		for (ISearchLoc loc : NetworkAPI.visitor().visitNetwork(network, 0, 0, 0)) {
			loc.getNode().networkInvalidated();
		}

		for (CountingNetworkNode node : network) {
			assertEquals(1, node.invalidated());
		}
	}
}
