package squiddev.cctweaks.reference;

/**
 * Stores basic info about CCTweaks
 * @author SquidDev
 *
 */
public final class ModInfo {
    public static final String ID = "CCTweaks";
    public static final String NAME = ID;
    public static final String VERSION = "@VERSION@";
    public static final String RESOURCE_DOMAIN = ID.toLowerCase();
    public static final String PROXY_CLIENT = "squiddev.cctweaks.proxy.ProxyClient";
    public static final String PROXY_SERVER = "squiddev.cctweaks.proxy.ProxyServer";
    public static final String DEPENDENCIES = "required-after:ComputerCraft;after:CCTurtle;";
    public static final boolean REQUIRED_CLIENT = true;
    public static final boolean REQUIRED_SERVER = true;
}