package com.wumple.util.xcomposter;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DeadCoralWallFanBlock;
import net.minecraft.block.IGrowable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;

/*
 * Extendible version of BoneMealItem
 * Replaces static methods with non-static, makes private parts protected, and splits methods
 * Forced to copy parts of base class, unfortunately
 */
public class XBoneMealItem extends BoneMealItem
{
	protected static final int NUM_PARTICLES = 15;
	
	public XBoneMealItem(Item.Properties properties)
	{
		super(properties);
	}

	/**
	 * Called when this item is used when targeting a Block
	 */
	public ActionResultType onItemUse(ItemUseContext context)
	{
		World world = context.getWorld();
		BlockPos blockpos = context.getPos();
		BlockPos blockpos1 = blockpos.offset(context.getFace());

		if (applyEnrichment(context.getItem(), world, blockpos, context.getPlayer()))
		{
			/*
			if (!world.isRemote)
			{
				world.playEvent(2005, blockpos, 0);
			}
			*/

			return ActionResultType.SUCCESS;
		}
		else
		{
			BlockState blockstate = world.getBlockState(blockpos);
			boolean flag = blockstate.func_224755_d(world, blockpos, context.getFace());
			if (flag && growSomeSeagrass(context.getItem(), world, blockpos1, context.getFace()))
			{
				/*
				if (!world.isRemote)
				{
					world.playEvent(2005, blockpos1, 0);
				}
				*/

				return ActionResultType.SUCCESS;
			}
			else
			{
				return ActionResultType.PASS;
			}
		}
	}

	/*
	@Deprecated //Forge: Use Player/Hand version
	public static boolean applyEnrichment(ItemStack stack, World worldIn, BlockPos pos)
	{
		if (worldIn instanceof net.minecraft.world.server.ServerWorld)
			return applyEnrichment(stack, worldIn, pos, net.minecraftforge.common.util.FakePlayerFactory
					.getMinecraft((net.minecraft.world.server.ServerWorld) worldIn));
		return false;
	}
	*/

	protected boolean tryUse(World worldIn)
	{
		return true;
	}

	protected void spawnSuccessParticles(World worldIn, BlockPos pos)
	{
		if (!worldIn.isRemote)
		{
			spawnParticles(ParticleTypes.HAPPY_VILLAGER, (ServerWorld)worldIn, pos, 0);
		}
	}

	protected void spawnTryFailureParticles(World worldIn, BlockPos pos)
	{

	}

	public boolean applyEnrichment(ItemStack stack, World worldIn, BlockPos pos, PlayerEntity player)
	{
		BlockState blockstate = worldIn.getBlockState(pos);
		int hook = net.minecraftforge.event.ForgeEventFactory.onApplyBonemeal(player, worldIn, pos, blockstate, stack);
		if (hook != 0)
			return hook > 0;
		if (blockstate.getBlock() instanceof IGrowable)
		{
			IGrowable igrowable = (IGrowable) blockstate.getBlock();
			if (igrowable.canGrow(worldIn, pos, blockstate, worldIn.isRemote))
			{
				if (!worldIn.isRemote)
				{
					if (tryUse(worldIn))
					{
						if (igrowable.canUseBonemeal(worldIn, worldIn.rand, pos, blockstate))
						{
							igrowable.grow(worldIn, worldIn.rand, pos, blockstate);
						}

						stack.shrink(1);
						spawnSuccessParticles(worldIn, pos);
						return true;
					}
					else
					{
						stack.shrink(1);
						spawnTryFailureParticles(worldIn, pos);
					}
				}

			}
		}

		return false;
	}

	public boolean growSomeSeagrass(ItemStack stack, World worldIn, BlockPos pos, @Nullable Direction side)
	{
		if (worldIn.getBlockState(pos).getBlock() == Blocks.WATER && worldIn.getFluidState(pos).getLevel() == 8)
		{
			if (!worldIn.isRemote)
			{
				if (!tryUse(worldIn))
				{
					stack.shrink(1);
					spawnTryFailureParticles(worldIn, pos);
					return false;
				}

				label79: for (int i = 0; i < 128; ++i)
				{
					BlockPos blockpos = pos;
					Biome biome = worldIn.getBiome(pos);
					BlockState blockstate = Blocks.SEAGRASS.getDefaultState();

					for (int j = 0; j < i / 16; ++j)
					{
						blockpos = blockpos.add(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2,
								random.nextInt(3) - 1);
						biome = worldIn.getBiome(blockpos);
						if (worldIn.getBlockState(blockpos).func_224756_o(worldIn, blockpos))
						{
							continue label79;
						}
					}

					// FORGE: Use BiomeDictionary here to allow modded warm ocean biomes to spawn coral from bonemeal
					if (net.minecraftforge.common.BiomeDictionary.hasType(biome,
							net.minecraftforge.common.BiomeDictionary.Type.OCEAN)
							&& net.minecraftforge.common.BiomeDictionary.hasType(biome,
									net.minecraftforge.common.BiomeDictionary.Type.HOT))
					{
						if (i == 0 && side != null && side.getAxis().isHorizontal())
						{
							blockstate = BlockTags.WALL_CORALS.getRandomElement(worldIn.rand).getDefaultState()
									.with(DeadCoralWallFanBlock.FACING, side);
						}
						else if (random.nextInt(4) == 0)
						{
							blockstate = BlockTags.UNDERWATER_BONEMEALS.getRandomElement(random).getDefaultState();
						}
					}

					if (blockstate.getBlock().isIn(BlockTags.WALL_CORALS))
					{
						for (int k = 0; !blockstate.isValidPosition(worldIn, blockpos) && k < 4; ++k)
						{
							blockstate = blockstate.with(DeadCoralWallFanBlock.FACING,
									Direction.Plane.HORIZONTAL.random(random));
						}
					}

					if (blockstate.isValidPosition(worldIn, blockpos))
					{
						BlockState blockstate1 = worldIn.getBlockState(blockpos);
						if (blockstate1.getBlock() == Blocks.WATER && worldIn.getFluidState(blockpos).getLevel() == 8)
						{
							worldIn.setBlockState(blockpos, blockstate, 3);
						}
						else if (blockstate1.getBlock() == Blocks.SEAGRASS && random.nextInt(10) == 0)
						{
							((IGrowable) Blocks.SEAGRASS).grow(worldIn, random, blockpos, blockstate1);
						}
					}
				}

				stack.shrink(1);
				spawnSuccessParticles(worldIn, pos);
			}

			return true;
		}
		else
		{
			return false;
		}
	}

	public <T extends IParticleData> void spawnParticles(T particleType, ServerWorld worldIn, BlockPos posIn, int data)
	{
		if (data == 0)
		{
			data = NUM_PARTICLES;
		}

		BlockState blockstate = worldIn.getBlockState(posIn);
		if (!blockstate.isAir(worldIn, posIn))
		{
			double d0 = random.nextGaussian() * 0.02D;
			double d1 = random.nextGaussian() * 0.02D;
			double d2 = random.nextGaussian() * 0.02D;
			double x = posIn.getX();
			double y = posIn.getY() + blockstate.getShape(worldIn, posIn).getEnd(Direction.Axis.Y);
			double z = posIn.getZ();
			worldIn.spawnParticle(particleType, x, y, z, data, d0, d1, d2, 0.5D);
		}
	}

}