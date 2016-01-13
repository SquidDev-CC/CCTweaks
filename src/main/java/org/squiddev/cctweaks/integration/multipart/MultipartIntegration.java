package org.squiddev.cctweaks.integration.multipart;

import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.integration.ModIntegration;

public class MultipartIntegration extends ModIntegration {
	public static final String MOD_NAME = "mcmultipart";

	public MultipartIntegration() {
		super(MOD_NAME);
	}

	@Override
	public boolean canLoad() {
		return super.canLoad() && Config.Integration.mcMultipart;
	}
}
