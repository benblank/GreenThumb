package com.five35.minecraft.greenthumb;

import cpw.mods.fml.common.Loader;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class Fertilizer extends Item {
	private static final Fertilizer INSTANCE = new Fertilizer();
	private static final String NAME = "fertilizer";

	public static Fertilizer getInstance() {
		return Fertilizer.INSTANCE;
	}

	public Fertilizer() {
		super();

		this.setCreativeTab(CreativeTabs.tabMaterials);
		this.setTextureName(Loader.instance().activeModContainer().getModId() + ":" + Fertilizer.NAME);
		this.setUnlocalizedName(Fertilizer.NAME);
	}

	@Override
	public boolean onItemUse(final ItemStack stack, final EntityPlayer player, final World world, final int x, final int y, final int z, final int side, final float hitX, final float hitY, final float hitZ) {
		if (ItemDye.applyBonemeal(stack, world, x, y, z, player)) {
			if (!world.isRemote) {
				world.playAuxSFX(2005, x, y, z, 0);
			}

			return true;
		}

		return false;
	}
}
