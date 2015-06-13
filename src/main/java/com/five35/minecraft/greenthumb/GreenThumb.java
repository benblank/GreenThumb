package com.five35.minecraft.greenthumb;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStem;
import net.minecraft.init.Blocks;
import net.minecraft.util.Direction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.BonemealEvent;

@Mod(modid = "GreenThumb")
public class GreenThumb {
	@SubscribeEvent
	@SuppressWarnings("static-method")
	public void onBonemeal(final BonemealEvent event) {
		if (event.world.isRemote) {
			return;
		}

		if (event.block == Blocks.cactus || event.block == Blocks.reeds) {
			int height = 1;
			int y = event.y;

			// count plant blocks below this one
			while (event.world.getBlock(event.x, --y, event.z) == event.block) {
				height++;
			}

			y = event.y;

			// count plant blocks above this one, and also find the Y coordinate above the "plant stack"
			while (event.world.getBlock(event.x, ++y, event.z) == event.block) {
				height++;
			}

			if (height < 3 && event.world.isAirBlock(event.x, y, event.z)) {
				event.world.setBlock(event.x, y, event.z, event.block);
				event.block.onNeighborBlockChange(event.world, event.x, y, event.z, event.block);
			} else {
				return;
			}
		} else if (event.block == Blocks.melon_stem || event.block == Blocks.pumpkin_stem) {
			// abort if stem isn't fully-grown (that's handled by the vanilla method)
			if (event.world.getBlockMetadata(event.x, event.y, event.z) < 7) {
				return;
			}

			final BlockStem stem = (BlockStem) event.block;

			if (event.world.getBlock(event.x - 1, event.y, event.z) == stem.field_149877_a) {
				return;
			}

			if (event.world.getBlock(event.x + 1, event.y, event.z) == stem.field_149877_a) {
				return;
			}

			if (event.world.getBlock(event.x, event.y, event.z - 1) == stem.field_149877_a) {
				return;
			}

			if (event.world.getBlock(event.x, event.y, event.z + 1) == stem.field_149877_a) {
				return;
			}

			// 50% chance
			if (event.world.rand.nextInt(2) == 0) {
				int attempts = 0;

				while (true) {
					final int direction = event.world.rand.nextInt(4);
					final int x = event.x + Direction.offsetX[direction];
					final int z = event.z + Direction.offsetZ[direction];

					if (event.world.isAirBlock(x, event.y, z)) {
						final Block soil = event.world.getBlock(x, event.y - 1, z);

						if (soil != null && (soil == Blocks.dirt || soil == Blocks.grass || soil.canSustainPlant(event.world, x, event.y - 1, z, ForgeDirection.UP, stem))) {
							event.world.setBlock(x, event.y, z, stem.field_149877_a);

							break;
						}

					}

					if (++attempts > 9) {
						return;
					}
				}
			}
		} else if (event.block == Blocks.nether_wart) {
			int stage = event.world.getBlockMetadata(event.x, event.y, event.z);

			if (stage >= 3) {
				return;
			}

			// ~29% chance of extra growth; 2-3 applications to go from newly-planted to fully-grown
			// chance of needing three applications is ~sqrt(.5) * ~sqrt(.5) == ~50%
			stage += event.world.rand.nextFloat() < .707 ? 1 : 2;

			event.world.setBlockMetadataWithNotify(event.x, event.y, event.z, Math.min(stage, 3), 2);
		} else if (event.block == Blocks.vine) {
			int y = event.y;

			while (event.world.getBlock(event.x, --y, event.z) == event.block) {
				// find the block below the bottommost vine
			}

			if (!event.world.isAirBlock(event.x, y, event.z) || y < 0) {
				return;
			}

			event.world.setBlock(event.x, y, event.z, event.block, event.world.getBlockMetadata(event.x, y + 1, event.z), 2);
		} else if (event.block == Blocks.waterlily) {
			int count = 0;

			// count the lily pads in a 7x7 square centered on this one
			for (int dX = -3; dX < 4; dX++) {
				for (int dZ = -3; dZ < 4; dZ++) {
					if (event.world.getBlock(event.x + dX, event.y, event.z + dZ) == Blocks.waterlily) {
						count++;
					}

					if (count > 11) { // 12 lily pads in a 7x7 square ~= 25% coverage
						return;
					}
				}
			}

			int attempts = 0;

			// attempt to place a new lily pad in a 7x7 square centered on this one
			while (true) {
				final int x = event.x + event.world.rand.nextInt(7) - 3;
				final int z = event.z + event.world.rand.nextInt(7) - 3;

				if (event.world.isAirBlock(x, event.y, z) && Blocks.waterlily.canBlockStay(event.world, x, event.y, z)) {
					event.world.setBlock(x, event.y, z, Blocks.waterlily, 0, 2);

					break;
				}

				// give up (and don't consume bonemeal) after 10 failed attempts at placement
				if (++attempts > 9) {
					return;
				}
			}
		} else {
			return;
		}

		// unrecognized IDs have already returned
		event.setResult(Result.ALLOW);
	}

	@EventHandler
	public void preInit(@SuppressWarnings("unused") final FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}
}
