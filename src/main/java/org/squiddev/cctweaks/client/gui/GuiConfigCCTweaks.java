package org.squiddev.cctweaks.client.gui;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.core.Config;

import java.util.ArrayList;
import java.util.List;

public class GuiConfigCCTweaks extends GuiConfig {

	public GuiConfigCCTweaks(GuiScreen screen) {
		super(screen, getConfigElements(), CCTweaks.ID, false, false, CCTweaks.NAME);
	}

	@SuppressWarnings("rawtypes")
	private static List<IConfigElement> getConfigElements() {
		ArrayList<IConfigElement> elements = new ArrayList<IConfigElement>();
		for (String category : Config.configuration.getCategoryNames()) {
			if (!category.contains(".")) elements.add(new ConfigElement(Config.configuration.getCategory(category)));
		}
		return elements;
	}
}
