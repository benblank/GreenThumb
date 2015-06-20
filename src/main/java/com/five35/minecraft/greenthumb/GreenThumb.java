package com.five35.minecraft.greenthumb;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.BlockStem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;

@Mod(modid = "GreenThumb")
public class GreenThumb {
	public static Config config;

	@Instance
	private static GreenThumb instance;

	private static Logger logger;

	// BonemealTransformer redirects applyBonemeal calls within ItemDye here.
	public static boolean applyBonemeal(final ItemStack stack, final World world, final BlockPos position, final EntityPlayer player) {
		if (stack.getItem() instanceof ItemDye && GreenThumb.config.getBoolean("enable_fertilizer")) {
			GreenThumb.logger.debug("Can't apply bonemeal when fertilizer is enabled.");

			return false;
		}

		GreenThumb.logger.debug("Applying bonemeal.");

		return ItemDye.applyBonemeal(stack, world, position, player);
	}

	public static GreenThumb getInstance() {
		return GreenThumb.instance;
	}

	public static Logger getLogger() {
		return GreenThumb.logger;
	}

	private static void readConfig(final File configFile) {
		final Config defaults = ConfigFactory.load("GreenThumb");
		final Config config;

		if (configFile.canRead()) {
			config = ConfigFactory.parseFile(configFile).withFallback(defaults);
		} else if (configFile.exists()) {
			GreenThumb.logger.warn("Cannot read settings file '%s', will use default values.", configFile.getAbsolutePath());

			config = defaults;
		} else {
			try (
				final InputStream input = GreenThumb.class.getResourceAsStream("/GreenThumb.conf");
				final OutputStream output = new FileOutputStream(configFile)) {

				IOUtils.copy(input, output);
			} catch (final IOException ex) {
				GreenThumb.logger.warn("Cannot create new settings file '%s', will use default values.", ex, configFile.getAbsolutePath());
			}

			config = defaults;
		}

		GreenThumb.config = config.resolve().getConfig("greenthumb");
	}

	@SubscribeEvent
	@SuppressWarnings("static-method")
	public void onBonemeal(final BonemealEvent event) {
		if (event.world.isRemote) {
			return;
		}

		final Block block = event.block.getBlock();

		if (block == Blocks.cactus || block == Blocks.reeds) {
			int height = 1;
			BlockPos position = event.pos;

			// count plant blocks below this one
			while (event.world.getBlockState(position = position.offsetDown()).getBlock() == block) {
				height++;
			}

			position = event.pos;

			// count plant blocks above this one, and also find the Y coordinate above the "plant stack"
			while (event.world.getBlockState(position = position.offsetUp()).getBlock() == block) {
				height++;
			}

			if (height < 3 && event.world.isAirBlock(position)) {
				event.world.setBlockState(position, event.block);
				event.world.notifyBlockOfStateChange(position, block);
			} else {
				return;
			}
		} else if (block == Blocks.melon_stem || block == Blocks.pumpkin_stem) {
			// abort if stem isn't fully-grown (that's handled by the vanilla method)
			if ((Integer) event.world.getBlockState(event.pos).getValue(BlockStem.AGE_PROP) < 7) {
				return;
			}

			final BlockStem stem = (BlockStem) block;

			if (event.world.getBlockState(event.pos.offsetWest()).getBlock() == stem.cropBlock) {
				return;
			}

			if (event.world.getBlockState(event.pos.offsetEast()).getBlock() == stem.cropBlock) {
				return;
			}

			if (event.world.getBlockState(event.pos.offsetNorth()).getBlock() == stem.cropBlock) {
				return;
			}

			if (event.world.getBlockState(event.pos.offsetSouth()).getBlock() == stem.cropBlock) {
				return;
			}

			// 50% chance
			if (event.world.rand.nextInt(2) == 0) {
				int attempts = 0;

				while (true) {
					final EnumFacing facing = EnumFacing.getHorizontal(event.world.rand.nextInt(4));
					final BlockPos position = event.pos.offset(facing);

					if (event.world.isAirBlock(position)) {
						final Block soil = event.world.getBlockState(position.offsetDown()).getBlock();

						if (soil != null && (soil == Blocks.dirt || soil == Blocks.grass || soil.canSustainPlant(event.world, position.offsetDown(), EnumFacing.UP, stem))) {
							event.world.setBlockState(position, stem.cropBlock.getDefaultState());

							break;
						}

					}

					if (++attempts > 9) {
						return;
					}
				}
			}
		} else if (block == Blocks.nether_wart) {
			final IBlockState state = event.world.getBlockState(event.pos);
			Integer age = (Integer) state.getValue(BlockNetherWart.AGE_PROP);

			if (age >= 3) {
				return;
			}

			// ~29% chance of extra growth; 2-3 applications to go from newly-planted to fully-grown
			// chance of needing three applications is ~sqrt(.5) * ~sqrt(.5) == ~50%
			age += event.world.rand.nextFloat() < .707 ? 1 : 2;

			event.world.setBlockState(event.pos, event.block.withProperty(BlockNetherWart.AGE_PROP, age));
		} else if (block == Blocks.vine) {
			BlockPos position = event.pos;

			while (event.world.getBlockState(position = position.offsetDown()).getBlock() == block) {
				// find the block below the bottommost vine
			}

			if (!event.world.isAirBlock(position)) {
				return;
			}

			event.world.setBlockState(position, event.world.getBlockState(position.offsetUp()));
		} else if (block == Blocks.waterlily) {
			int count = 0;

			// count the lily pads in a 7x7 square centered on this one
			for (int dX = -3; dX < 4; dX++) {
				for (int dZ = -3; dZ < 4; dZ++) {
					if (event.world.getBlockState(event.pos.add(dX, 0, dZ)).getBlock() == Blocks.waterlily) {
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
				final BlockPos position = event.pos.add(event.world.rand.nextInt(7) - 3, 0, event.world.rand.nextInt(7) - 3);

				if (event.world.isAirBlock(position) && ((BlockBush) Blocks.waterlily).canBlockStay(event.world, position, Blocks.waterlily.getDefaultState())) {
					event.world.setBlockState(position, Blocks.waterlily.getDefaultState());

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

		// unrecognized blocks have already returned
		event.setResult(Result.ALLOW);
	}

	@EventHandler
	public void preInit(final FMLPreInitializationEvent event) {
		GreenThumb.logger = event.getModLog();
		GreenThumb.readConfig(event.getSuggestedConfigurationFile());

		GameRegistry.registerItem(Fertilizer.getInstance(), Fertilizer.getInstance().getUnlocalizedName());
		BlockDispenser.dispenseBehaviorRegistry.putObject(Fertilizer.getInstance(), new FertilizerDispenserBehavior());

		if (GreenThumb.config.getBoolean("enable_fertilizer")) {
			GameRegistry.addShapelessRecipe(new ItemStack(Fertilizer.getInstance()), new ItemStack(Items.dye, 1, 15), new ItemStack(Items.rotten_flesh));
		}

		MinecraftForge.EVENT_BUS.register(this);
	}
}
