package squiddev.cctweaks.client.gui;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import squiddev.cctweaks.core.reference.Config;
import squiddev.cctweaks.core.reference.ModInfo;

import java.util.ArrayList;
import java.util.List;

public class GuiConfigCCTweaks extends GuiConfig {

	public GuiConfigCCTweaks(GuiScreen screen) {
		super(screen, getConfigElements(), ModInfo.ID, false, false, ModInfo.NAME);
	}

	private static List<IConfigElement> getConfigElements() {
		ArrayList<IConfigElement> elements = new ArrayList<IConfigElement>();
		for (String category : Config.CATEGORIES) {
			elements.add(new ConfigElement(Config.configuration.getCategory(category)));
		}
		return elements;
	}
}
