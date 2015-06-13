package com.five35.minecraft.greenthumb;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStem;
import net.minecraft.util.Direction;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.BonemealEvent;

@Mod(modid = "GreenThumb")
public class GreenThumb {
	@EventHandler
	public void init(@SuppressWarnings("unused") final FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@ForgeSubscribe
	@SuppressWarnings("static-method")
	public void onBonemeal(final BonemealEvent event) {
		if (event.world.isRemote) {
			return;
		}

		if (event.ID == Block.cactus.blockID || event.ID == Block.reed.blockID) {
			int height = 1;
			int y = event.Y;

			// count plant blocks below this one
			while (event.world.getBlockId(event.X, --y, event.Z) == event.ID) {
				height++;
			}

			y = event.Y;

			// count plant blocks above this one, and also find the Y coordinate above the "plant stack"
			while (event.world.getBlockId(event.X, ++y, event.Z) == event.ID) {
				height++;
			}

			if (height < 3 && event.world.isAirBlock(event.X, y, event.Z)) {
				event.world.setBlock(event.X, y, event.Z, event.ID);
				Block.blocksList[event.ID].onNeighborBlockChange(event.world, event.X, y, event.Z, event.ID);
			} else {
				return;
			}
		} else if (event.ID == Block.melonStem.blockID || event.ID == Block.pumpkinStem.blockID) {
			// abort if stem isn't fully-grown (that's handled by the vanilla method)
			if (event.world.getBlockMetadata(event.X, event.Y, event.Z) < 7) {
				return;
			}

			final BlockStem stem = (BlockStem) Block.blocksList[event.ID];

			if (event.world.getBlockId(event.X - 1, event.Y, event.Z) == stem.fruitType.blockID) {
				return;
			}

			if (event.world.getBlockId(event.X + 1, event.Y, event.Z) == stem.fruitType.blockID) {
				return;
			}

			if (event.world.getBlockId(event.X, event.Y, event.Z - 1) == stem.fruitType.blockID) {
				return;
			}

			if (event.world.getBlockId(event.X, event.Y, event.Z + 1) == stem.fruitType.blockID) {
				return;
			}

			// 50% chance
			if (event.world.rand.nextInt(2) == 0) {
				int attempts = 0;

				while (true) {
					final int direction = event.world.rand.nextInt(4);
					final int x = event.X + Direction.offsetX[direction];
					final int z = event.Z + Direction.offsetZ[direction];

					if (event.world.isAirBlock(x, event.Y, z)) {
						final Block soil = Block.blocksList[event.world.getBlockId(x, event.Y - 1, z)];

						if (soil != null && (soil == Block.dirt || soil == Block.grass || soil.canSustainPlant(event.world, x, event.Y - 1, z, ForgeDirection.UP, stem))) {
							event.world.setBlock(x, event.Y, z, stem.fruitType.blockID);

							break;
						}

					}

					if (++attempts > 9) {
						return;
					}
				}
			}
		} else if (event.ID == Block.netherStalk.blockID) {
			int stage = event.world.getBlockMetadata(event.X, event.Y, event.Z);

			if (stage >= 3) {
				return;
			}

			// ~29% chance of extra growth; 2-3 applications to go from newly-planted to fully-grown
			// chance of needing three applications is ~sqrt(.5) * ~sqrt(.5) == ~50%
			stage += event.world.rand.nextFloat() < .707 ? 1 : 2;

			event.world.setBlockMetadataWithNotify(event.X, event.Y, event.Z, Math.min(stage, 3), 2);
		} else if (event.ID == Block.vine.blockID) {
			int y = event.Y;

			while (event.world.getBlockId(event.X, --y, event.Z) == event.ID) {
				// find the block below the bottommost vine
			}

			if (!event.world.isAirBlock(event.X, y, event.Z) || y < 0) {
				return;
			}

			event.world.setBlock(event.X, y, event.Z, event.ID, event.world.getBlockMetadata(event.X, y + 1, event.Z), 2);
		} else if (event.ID == Block.waterlily.blockID) {
			int count = 0;

			// count the lily pads in a 7x7 square centered on this one
			for (int dX = -3; dX < 4; dX++) {
				for (int dZ = -3; dZ < 4; dZ++) {
					if (event.world.getBlockId(event.X + dX, event.Y, event.Z + dZ) == Block.waterlily.blockID) {
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
				final int x = event.X + event.world.rand.nextInt(7) - 3;
				final int z = event.Z + event.world.rand.nextInt(7) - 3;

				if (event.world.isAirBlock(x, event.Y, z) && Block.waterlily.canBlockStay(event.world, x, event.Y, z)) {
					event.world.setBlock(x, event.Y, z, Block.waterlily.blockID, 0, 2);

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
}
