package com.five35.minecraft.greenthumb;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;

public class FertilizerDispenserBehavior implements IBehaviorDispenseItem {
	@Override
	public ItemStack dispense(final IBlockSource source, final ItemStack stack) {
		final EnumFacing facing = BlockDispenser.getFacing(source.getBlockMetadata());
		final World world = source.getWorld();
		final BlockPos position = source.getBlockPos();
		final Vec3i offset = facing.getDirectionVec();

		if (world instanceof WorldServer) {
			if (ItemDye.applyBonemeal(stack, world, position.add(offset), FakePlayerFactory.getMinecraft((WorldServer) world))) {
				if (!world.isRemote) {
					// plant growth particles
					world.playAuxSFX(2005, position.add(offset), 0);
				}

				// dispenser "success" click
				world.playAuxSFX(1000, position, 0);
			} else {
				// dispenser "failure" click
				world.playAuxSFX(1001, position, 0);
			}
		}

		// smoke particles
		world.playAuxSFX(2000, position, facing.getFrontOffsetX() + 1 + 3 * (facing.getFrontOffsetZ() + 1));

		return stack;
	}
}
