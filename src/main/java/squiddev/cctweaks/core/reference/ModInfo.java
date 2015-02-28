package squiddev.cctweaks.core.reference;

/**
 * Stores basic info about the Mod
 */
public final class ModInfo {
	public static final String ID = "CCTweaks";
	public static final String NAME = ID;
	public static final String VERSION = "${mod_version}";
	public static final String RESOURCE_DOMAIN = ID.toLowerCase();
	public static final String PROXY_CLIENT = "squiddev.cctweaks.client.ProxyClient";
	public static final String PROXY_SERVER = "squiddev.cctweaks.core.proxy.ProxyServer";
	public static final String DEPENDENCIES = "required-after:ComputerCraft;after:CCTurtle;";

	public static final String GUI_FACTORY = "squiddev.cctweaks.client.gui.GuiConfigFactory";
}
