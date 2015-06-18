package org.squiddev.cctweaks.core.network;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.squiddev.cctweaks.api.SingleTypeUnorderedPair;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.INetworkNode;

import static org.junit.Assert.*;

public class NetworkTest {
	public static class Node extends AbstractNode {
		public final String name;

		public Node(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return "AbstractNode: " + name;
		}
	}

	/**
	 * I'm scared of infinite loops
	 */
	@Rule
	public Timeout globalTimeout = Timeout.seconds(5);

	@Test
	public void basicActions() {
		INetworkNode base = new Node("Base");
		INetworkNode a = new Node("a");
		INetworkNode b = new Node("b");

		INetworkController controller = new NetworkController(base);

		{
			// A--Base--B
			controller.formConnection(base, a);
			controller.formConnection(base, b);

			assertEquals(controller, base.getAttachedNetwork());
			assertEquals(controller, a.getAttachedNetwork());
			assertEquals(controller, b.getAttachedNetwork());
		}

		{
			// A  Base  B
			controller.removeNode(base);
			assertNull(base.getAttachedNetwork());

			assertNotNull(a.getAttachedNetwork());
			assertNotNull(b.getAttachedNetwork());
			assertNotEquals(a.getAttachedNetwork(), b.getAttachedNetwork());
		}

		{
			// A--B
			controller = a.getAttachedNetwork();
			controller.formConnection(a, b);

			assertEquals(controller, a.getAttachedNetwork());
			assertEquals(a.getAttachedNetwork(), b.getAttachedNetwork());
		}
	}

	@Test
	public void recursion() {
		INetworkNode a = new Node("a");
		INetworkNode b = new Node("b");
		INetworkNode c = new Node("c");

		INetworkController controller = new NetworkController(c);

		{
			/*
				A---B
				\  /
				 C
			 */
			controller.formConnection(c, a);
			controller.formConnection(c, b);
			controller.formConnection(a, b);

			assertEquals(controller, c.getAttachedNetwork());
			assertEquals(controller, a.getAttachedNetwork());
			assertEquals(controller, b.getAttachedNetwork());
		}

		{
			// A--B
			controller.removeNode(c);

			assertNull(c.getAttachedNetwork());

			assertNotNull(a.getAttachedNetwork());
			assertNotNull(b.getAttachedNetwork());
			assertEquals(a.getAttachedNetwork(), b.getAttachedNetwork());
		}
	}

	@Test
	public void breakConnection() {
		INetworkNode a = new Node("a");
		INetworkNode b = new Node("b");
		INetworkNode c = new Node("c");

		INetworkController controller = new NetworkController(c);

		{
			/*
				A---B
				\  /
				 C
			 */
			controller.formConnection(c, a);
			controller.formConnection(c, b);
			controller.formConnection(a, b);

			assertEquals(controller, c.getAttachedNetwork());
			assertEquals(controller, a.getAttachedNetwork());
			assertEquals(controller, b.getAttachedNetwork());
		}

		{
			// A--B--C
			a.getAttachedNetwork().breakConnection(new SingleTypeUnorderedPair<INetworkNode>(a, c));

			assertNotNull(a.getAttachedNetwork());
			assertEquals(a.getAttachedNetwork(), b.getAttachedNetwork());
			assertEquals(a.getAttachedNetwork(), c.getAttachedNetwork());
		}

		{
			// A--B  C
			a.getAttachedNetwork().breakConnection(new SingleTypeUnorderedPair<INetworkNode>(b, c));

			assertNotNull(a.getAttachedNetwork());
			assertEquals(a.getAttachedNetwork(), b.getAttachedNetwork());

			assertNotNull(c.getAttachedNetwork());
			assertNotEquals(a.getAttachedNetwork(), c.getAttachedNetwork());
		}
	}
}
