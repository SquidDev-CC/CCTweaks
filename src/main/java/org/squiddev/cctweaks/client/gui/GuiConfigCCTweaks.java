package org.squiddev.cctweaks.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
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
