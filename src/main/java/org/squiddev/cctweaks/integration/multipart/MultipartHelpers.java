package org.squiddev.cctweaks.integration.multipart;

import mcmultipart.microblock.IMicroblock;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.OcclusionHelper;
import mcmultipart.multipart.PartSlot;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNodeHost;

/**
 * Various helpers for multiparts
 */
public class MultipartHelpers {
	public static boolean extendIn(IMultipart multipart, AxisAlignedBB bound, EnumFacing side) {
		ISlottedPart part = multipart.getContainer().getPartInSlot(PartSlot.getFaceSlot(side));

		if (part instanceof IMicroblock.IFaceMicroblock) {
			if (!((IMicroblock.IFaceMicroblock) part).isFaceHollow()) {
				return false;
			}
		}

		return OcclusionHelper.occlusionTest(multipart.getContainer().getParts(), multipart, bound);
	}

	public static INetworkNode getNode(IMultipart part) {
		if (part instanceof INetworkNode) {
			return (INetworkNode) part;
		} else if (part instanceof IWorldNetworkNodeHost) {
			return ((IWorldNetworkNodeHost) part).getNode();
		} else {
			return null;
		}
	}

	public static IWorldNetworkNode getWorldNode(IMultipart part) {
		if (part instanceof IWorldNetworkNode) {
			return (IWorldNetworkNode) part;
		} else if (part instanceof IWorldNetworkNodeHost) {
			return ((IWorldNetworkNodeHost) part).getNode();
		} else {
			return null;
		}
	}
}
