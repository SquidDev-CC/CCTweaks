package org.squiddev.cctweaks.core.integration;

import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.common.Loader;
import net.minecraft.tileentity.TileEntity;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.INetworkNodeProvider;
import org.squiddev.cctweaks.api.network.NetworkRegistry;
import org.squiddev.cctweaks.core.integration.multipart.RegisterBlockPart;

import java.util.HashSet;
import java.util.Set;

/**
 * Custom integration module
 */
public class IntegrationRegistry {
	public interface IIntegrationModule {
		void load();

		boolean canLoad();
	}

	public static abstract class ModIntegrationModule implements IIntegrationModule {
		public final String modName;

		public ModIntegrationModule(String modName) {
			this.modName = modName;
		}

		@Override
		public boolean canLoad() {
			return Loader.isModLoaded(modName);
		}
	}

	private static final Set<IIntegrationModule> modules = new HashSet<IIntegrationModule>();

	public static void addModule(IIntegrationModule module) {
		modules.add(module);
	}

	public static void init() {
		addModule(new ModIntegrationModule("ForgeMultipart") {
			@Override
			public void load() {
				new RegisterBlockPart().init();

				NetworkRegistry.addNodeProvider(new INetworkNodeProvider() {
					@Override
					public INetworkNode getNode(TileEntity tile) {
						if (tile instanceof TileMultipart) {
							// Cables reside in the central slot so we can just fetch that
							// instead and use the cable to delegate to other nodes in the multipart
							TMultiPart part = ((TileMultipart) tile).partMap(6);
							if (part != null && part instanceof INetworkNode) {
								return (INetworkNode) part;
							}
						}

						return null;
					}

					@Override
					public boolean isNode(TileEntity tile) {
						return getNode(tile) != null;
					}
				});
			}
		});

		for (IIntegrationModule module : modules) {
			if (module.canLoad()) module.load();
		}
	}
}
