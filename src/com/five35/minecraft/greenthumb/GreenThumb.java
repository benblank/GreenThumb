package com.five35.minecraft.greenthumb;

import net.minecraft.block.BlockStem;
import net.minecraftforge.common.ForgeDirection;
import net.minecraft.util.Direction;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.BonemealEvent;

@Mod(modid = "GreenThumb")
public class GreenThumb {
	@Instance
	public static GreenThumb instance;

	static Configuration config;
	static int fertilizerID;
	public static Fertilizer fertilizer;

	private final Random random = new Random();

	// this method is called within ItemDye wherever it would otherwise call ItemDye.applyBonemeal
	public static boolean applyBonemeal(final ItemStack stack, final World world, final int x, final int y, final int z, final EntityPlayer player) {
		if (GreenThumb.fertilizerID == 0) {
			return ItemDye.applyBonemeal(stack, world, x, y, z, player);
		}

		return false;
	}

	@EventHandler
	public static void preInit(final FMLPreInitializationEvent event) {
		GreenThumb.config = new Configuration(event.getSuggestedConfigurationFile());
		GreenThumb.config.load();

		GreenThumb.fertilizerID = GreenThumb.config.getItem("fertilizer", 20732, "set to 0 to disable fertilizer and use bonemeal to grow plants").getInt();

		GreenThumb.config.save(); // if config file was missing, this will write the defaults
	}

	@EventHandler
	public void init(@SuppressWarnings("unused") final FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);

		if (GreenThumb.fertilizerID > 0) {
			GreenThumb.fertilizer = new Fertilizer(GreenThumb.fertilizerID);

			LanguageRegistry.addName(GreenThumb.fertilizer, "Crude Fertilizer");
			GameRegistry.addShapelessRecipe(new ItemStack(GreenThumb.fertilizer), new ItemStack(Item.dyePowder, 1, 15), new ItemStack(Item.rottenFlesh));
			BlockDispenser.dispenseBehaviorRegistry.putObject(GreenThumb.fertilizer, new FertilizerDispenserBehavior());
		}
	}

	@ForgeSubscribe
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
			// 95% chance, same as saplings
			// abort if stem isn't fully-grown (that's handled by the vanilla method)
			if (this.random.nextInt(20) > 9 || event.world.getBlockMetadata(event.X, event.Y, event.Z) < 7) {
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

			final int direction = this.random.nextInt(4);
			final int x = event.X + Direction.offsetX[direction];
			final int z = event.Z + Direction.offsetZ[direction];

			if (!event.world.isAirBlock(x, event.Y, z)) {
				return;
			}

			final Block soil = Block.blocksList[event.world.getBlockId(x, event.Y - 1, z)];

			if (soil == null || !(soil == Block.dirt || soil == Block.grass || soil.canSustainPlant(event.world, x, event.Y - 1, z, ForgeDirection.UP, stem))) {
				return;
			}

			event.world.setBlock(x, event.Y, z, stem.fruitType.blockID);
		} else if (event.ID == Block.netherStalk.blockID) {
			int stage = event.world.getBlockMetadata(event.X, event.Y, event.Z);

			if (stage >= 3) {
				return;
			}

			// 50% chance of extra growth; 2-3 applications to go from newly-planted to fully-grown
			stage += this.random.nextInt(2) == 0 ? 1 : 2;

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
				final int x = event.X + this.random.nextInt(7) - 3;
				final int z = event.Z + this.random.nextInt(7) - 3;

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
