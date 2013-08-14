package com.five35.minecraft.greenthumb;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.FakePlayerFactory;

public class FertilizerDispenserBehavior extends BehaviorDefaultDispenseItem {
	@Override
	protected ItemStack dispenseStack(final IBlockSource source, final ItemStack stack) {
		final EnumFacing facing = BlockDispenser.getFacing(source.getBlockMetadata());
		final World world = source.getWorld();
		final int x = source.getXInt() + facing.getFrontOffsetX();
		final int y = source.getYInt() + facing.getFrontOffsetY();
		final int z = source.getZInt() + facing.getFrontOffsetZ();

		if (ItemDye.applyBonemeal(stack, world, x, y, z, FakePlayerFactory.getMinecraft(world))) {
			if (!world.isRemote) {
				world.playAuxSFX(2005, x, y, z, 0);
			}
		}

		return stack;
	}
}
