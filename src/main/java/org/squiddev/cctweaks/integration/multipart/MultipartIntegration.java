package org.squiddev.cctweaks.integration.multipart;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import mcmultipart.item.ItemMultiPart;
import mcmultipart.multipart.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.CCTweaksAPI;
import org.squiddev.cctweaks.api.network.INetworkNodeProvider;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHost;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.registry.IClientModule;
import org.squiddev.cctweaks.integration.ModIntegration;
import org.squiddev.cctweaks.integration.multipart.network.PartCable;
import org.squiddev.cctweaks.integration.multipart.network.PartModem;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MultipartIntegration extends ModIntegration implements IClientModule {
	public static final String MOD_NAME = "mcmultipart";

	/**
	 * Temporary item whilst testing
	 */
	private final ItemMultiPart multi = new ItemMultiPart() {
		{
			setUnlocalizedName(CCTweaks.RESOURCE_DOMAIN + ".partCable");
			setCreativeTab(CCTweaks.getCreativeTab());
		}

		@Override
		public void getSubItems(Item p_getSubItems_1_, CreativeTabs p_getSubItems_2_, List<ItemStack> p_getSubItems_3_) {
			p_getSubItems_3_.add(new ItemStack(this, 1, 0));
			p_getSubItems_3_.add(new ItemStack(this, 1, 1));
		}

		@Override
		public IMultipart createPart(World world, BlockPos blockPos, EnumFacing enumFacing, Vec3 vec3, ItemStack itemStack, EntityPlayer entityPlayer) {
			return itemStack.getItemDamage() == 0 ? new PartCable() : new PartModem(enumFacing);
		}
	};

	public MultipartIntegration() {
		super(MOD_NAME);
	}

	@Override
	public boolean canLoad() {
		return super.canLoad() && Config.Integration.mcMultipart;
	}

	@Override
	public void preInit() {
		GameRegistry.registerItem(multi, "partCable");

		// Register parts
		MultipartRegistry.registerPart(PartCable.class, CCTweaks.NAME + ":Cable");
		MultipartRegistry.registerPart(PartModem.class, CCTweaks.NAME + ":Modem");

		// All the providers!
		MultipartRegistry.registerPartConverter(new IPartConverter.IPartConverter2() {
			@Override
			public Collection<Block> getConvertableBlocks() {
				return Collections.<Block>singleton(ComputerCraft.Blocks.cable);
			}

			@Override
			public Collection<? extends IMultipart> convertBlock(IBlockAccess world, BlockPos position, boolean simulate) {
				TileEntity te = world.getTileEntity(position);

				if (te == null || !(te instanceof TileCable)) return Collections.emptySet();

				TileCable cable = (TileCable) te;
				switch (cable.getPeripheralType()) {
					case Cable:
						return Collections.singleton(new PartCable());
					case WiredModem:
						return Collections.singleton(new PartModem(cable));
					case WiredModemWithCable:
						return Arrays.asList(new PartCable(), new PartModem(cable));
					default:
						return Collections.emptySet();
				}
			}
		});
		CCTweaksAPI.instance().networkRegistry().addNodeProvider(new INetworkNodeProvider() {
			@Override
			public IWorldNetworkNode getNode(TileEntity tile) {
				if (tile instanceof IMultipartContainer) {
					IMultipart part = ((IMultipartContainer) tile).getPartInSlot(PartSlot.CENTER);
					return part == null ? null : MultipartHelpers.getWorldNode(part);
				}

				return null;
			}

			@Override
			public boolean isNode(TileEntity tile) {
				return getNode(tile) != null;
			}
		});
		ComputerCraftAPI.registerPeripheralProvider(new IPeripheralProvider() {
			@Override
			public IPeripheral getPeripheral(World world, BlockPos position, EnumFacing enumFacing) {
				TileEntity tile = world.getTileEntity(position);
				if (tile instanceof IMultipartContainer) {
					IMultipart part = ((IMultipartContainer) tile).getPartInSlot(PartSlot.getFaceSlot(enumFacing));
					if (part instanceof IPeripheral) return (IPeripheral) part;
					if (part instanceof IPeripheralHost) return ((IPeripheralHost) part).getPeripheral(enumFacing);
				}
				return null;
			}
		});
	}


	@Override
	public void clientInit() {
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(multi, 0, new ModelResourceLocation("computercraft:CC-Cable", "inventory"));
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(multi, 1, new ModelResourceLocation("computercraft:wired_modem", "inventory"));
	}
}
