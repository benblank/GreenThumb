package com.five35.minecraft.greenthumb;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
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
	static boolean easyMode;

	private final Random random = new Random();

	public static boolean applyBonemeal(final ItemStack stack, final World world, final int x, final int y, final int z, final EntityPlayer player) {
		if (GreenThumb.easyMode) {
			return ItemDye.applyBonemeal(stack, world, x, y, z, player);
		}

		return false;
	}

	@EventHandler
	public static void preInit(final FMLPreInitializationEvent event) {
		GreenThumb.config = new Configuration(event.getSuggestedConfigurationFile());
		GreenThumb.config.load();

		GreenThumb.easyMode = GreenThumb.config.get(Configuration.CATEGORY_GENERAL, "easy_mode", false, "if false (the default), you cannot grow plants with bonemeal and must use fertilizer instead").isBooleanValue();

		GreenThumb.config.save(); // if config file was missing, this will write the defaults
	}

	@EventHandler
	public void init(@SuppressWarnings("unused") final FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@ForgeSubscribe
	public void onBonemeal(final BonemealEvent event) {
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
		} else if (event.ID == Block.vine.blockID) {
			int y = event.Y;

			while (event.world.getBlockId(event.X, --y, event.Z) == event.ID) {
				// find the block below the bottommost vine
			}

			if (!event.world.isAirBlock(event.X, y, event.Z) || y < 0) {
				return;
			}

			event.world.setBlock(event.X, y, event.Z, event.ID, event.world.getBlockMetadata(event.X, event.Y, event.Z), 2);
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
			if (!event.world.isRemote) {
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
			}
		} else {
			return;
		}

		// unrecognized IDs have already returned
		event.setResult(Result.ALLOW);
	}
}
