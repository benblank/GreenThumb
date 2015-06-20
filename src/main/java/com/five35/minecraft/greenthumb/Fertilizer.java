package com.five35.minecraft.greenthumb;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
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
		this.setUnlocalizedName(Fertilizer.NAME);
	}

	@Override
	public boolean onItemUse(final ItemStack stack, final EntityPlayer player, final World world, final BlockPos position, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
		if (ItemDye.applyBonemeal(stack, world, position, player)) {
			if (!world.isRemote) {
				world.playAuxSFX(2005, position, 0);
			}

			return true;
		}

		return false;
	}
}
