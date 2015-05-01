package org.squiddev.cctweaks.core.network;

import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.network.NetworkVisitor;

import java.util.Queue;
import java.util.Set;

/**
 * Network visitor that does not visit non-loaded nodes
 */
public abstract class SafeNetworkVisitor extends NetworkVisitor {
	@Override
	protected void enqueue(Queue<SearchLoc> queue, Set<SearchLoc> visited, SearchLoc location) {
		IBlockAccess world = location.world;
		if (!(world instanceof World) || ((World) world).blockExists(location.x, location.y, location.z)) {
			super.enqueue(queue, visited, location);
		}
	}
}
