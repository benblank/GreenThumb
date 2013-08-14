package com.five35.minecraft.greenthumb;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class Fertilizer extends Item {
	public Fertilizer(final int id) {
		super(id);

		this.setCreativeTab(CreativeTabs.tabMaterials);
		this.setUnlocalizedName("gt_fertilizer");
		this.func_111206_d("greenthumb:fertilizer");
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
