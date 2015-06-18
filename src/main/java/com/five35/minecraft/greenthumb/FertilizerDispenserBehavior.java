package com.five35.minecraft.greenthumb;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;

public class FertilizerDispenserBehavior implements IBehaviorDispenseItem {
	@Override
	public ItemStack dispense(final IBlockSource source, final ItemStack stack) {
		final EnumFacing facing = BlockDispenser.func_149937_b(source.getBlockMetadata());
		final World world = source.getWorld();
		final int x = source.getXInt();
		final int y = source.getYInt();
		final int z = source.getZInt();
		final int dx = facing.getFrontOffsetX();
		final int dy = facing.getFrontOffsetY();
		final int dz = facing.getFrontOffsetZ();

		if (world instanceof WorldServer) {
			if (ItemDye.applyBonemeal(stack, world, x + dx, y + dy, z + dz, FakePlayerFactory.getMinecraft((WorldServer) world))) {
				if (!world.isRemote) {
					// plant growth particles
					world.playAuxSFX(2005, x + dx, y + dy, z + dz, 0);
				}

				// dispenser "success" click
				world.playAuxSFX(1000, x, y, z, 0);
			} else {
				// dispenser "failure" click
				world.playAuxSFX(1001, x, y, z, 0);
			}
		}

		// smoke particles
		world.playAuxSFX(2000, x, y, z, dx + 1 + 3 * (dz + 1));

		return stack;
	}
}
