package squiddev.cctweaks.asm;

import com.google.common.eventbus.EventBus;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.Arrays;
import java.util.Map;

@IFMLLoadingPlugin.TransformerExclusions("squiddev.cctweaks.asm.")
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class LoadingPlugin implements IFMLLoadingPlugin {
	@Override
	public String[] getASMTransformerClass() {
		return new String[]{ ASMTransformer.class.getName() };
	}

	@Override
	public String getModContainerClass() {
		return CCTweaksDummyMod.class.getName();
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> stringObjectMap) {
		return;
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

	public static class CCTweaksDummyMod extends DummyModContainer {

		public CCTweaksDummyMod() {

			super(new ModMetadata());
			ModMetadata md = getMetadata();
			md.autogenerated = true;
			md.authorList = Arrays.asList("SquidDev");
			md.modId = "<CCTweaks ASM>";
			md.name = md.description = "CCTweaks ASM Transformer";
			md.version = "000";
		}

		@Override
		public boolean registerBus(EventBus bus, LoadController controller) {
			bus.register(this);
			return true;
		}
	}
}
