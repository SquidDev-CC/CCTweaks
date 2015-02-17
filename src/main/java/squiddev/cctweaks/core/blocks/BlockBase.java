package squiddev.cctweaks.core.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import squiddev.cctweaks.CCTweaks;
import squiddev.cctweaks.core.reference.ModInfo;

/**
 * Base block for all CC-Tweaks blocks
 */
public class BlockBase extends Block {
	/**
	 * The name of the block for CC-Tweaks
	 */
	protected final String blockName;

	public BlockBase(Material material, String name) {
		super(material);
		blockName = name;

		setHardness(2);
		setCreativeTab(CCTweaks.getCreativeTab());
		setBlockName(ModInfo.RESOURCE_DOMAIN + ":" + name);


	}

	public BlockBase(String name) {
		this(Material.rock, name);
	}
}
