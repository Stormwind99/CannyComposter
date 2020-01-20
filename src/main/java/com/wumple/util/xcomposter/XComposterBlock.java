package com.wumple.util.xcomposter;

import javax.annotation.Nullable;

import com.wumple.util.misc.TimeUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

/*
 * Extendible version of ComposterBlock
 * Replaces static methods with non-static, makes private parts protected, and splits methods
 * Forced to copy parts of base class, unfortunately
 */
public class XComposterBlock extends ComposterBlock
{
	public XComposterBlock()
	{
		super(Block.Properties.create(Material.WOOD).hardnessAndResistance(0.6F).sound(SoundType.WOOD));
	}
	
	public XComposterBlock(Block.Properties properties)
	{
		super(properties);
	}
	
	// ------------------------------------------------------------------------
	// For extension
	
	protected boolean isCompostable(ItemStack itemstack)
	{
		return CHANCES.containsKey(itemstack.getItem());
	}

	protected float getCompostLevelOf(ItemStack itemstack)
	{
		return CHANCES.getFloat(itemstack.getItem());
	}

	protected ItemStack makeCompost()
	{
		return new ItemStack(Items.BONE_MEAL);
	}
	
	protected int getWaitTicks(IWorld worldIn)
	{
		return TimeUtil.TICKS_PER_SECOND;
	}
	
	// ------------------------------------------------------------------------
	// From ComposterBlock

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit)
	{
		int i = state.get(LEVEL);
		ItemStack itemstack = player.getHeldItem(handIn);
		if (i < 8 && isCompostable(itemstack))
		{
			if (i < 7 && !worldIn.isRemote)
			{
				boolean flag = addItem(state, worldIn, pos, itemstack);
				worldIn.playEvent(1500, pos, flag ? 1 : 0);
				if (!player.abilities.isCreativeMode)
				{
					itemstack.shrink(1);
				}
			}

			return true;
		}
		else if (i == 8)
		{
			if (!worldIn.isRemote)
			{
				float f = 0.7F;
				double d0 = (double) (worldIn.rand.nextFloat() * f) + (double) 0.15F;
				double d1 = (double) (worldIn.rand.nextFloat() * f) + (double) 0.060000002F + 0.6D;
				double d2 = (double) (worldIn.rand.nextFloat() * f) + (double) 0.15F;
				ItemEntity itementity = new ItemEntity(worldIn, (double) pos.getX() + d0, (double) pos.getY() + d1,
						(double) pos.getZ() + d2, makeCompost());
				itementity.setDefaultPickupDelay();
				worldIn.addEntity(itementity);
			}

			clear(state, worldIn, pos);
			worldIn.playSound((PlayerEntity) null, pos, SoundEvents.BLOCK_COMPOSTER_EMPTY, SoundCategory.BLOCKS, 1.0F,
					1.0F);
			return true;
		}
		else
		{
			return false;
		}
	}

	protected boolean addItem(BlockState blockstateIn, IWorld worldIn, BlockPos posIn, ItemStack stackIn)
	{
		int i = blockstateIn.get(LEVEL);
		float f = getCompostLevelOf(stackIn);
		if ((i != 0 || !(f > 0.0F)) && !(worldIn.getRandom().nextDouble() < (double) f))
		{
			return false;
		}
		else
		{
			int j = i + 1;
			worldIn.setBlockState(posIn, blockstateIn.with(LEVEL, Integer.valueOf(j)), 3);
			if (j == 7)
			{
				worldIn.getPendingBlockTicks().scheduleTick(posIn, blockstateIn.getBlock(), getWaitTicks(worldIn));
			}

			return true;
		}
	}

	protected void clear(BlockState p_220294_0_, IWorld p_220294_1_, BlockPos p_220294_2_)
	{
		p_220294_1_.setBlockState(p_220294_2_, p_220294_0_.with(LEVEL, Integer.valueOf(0)), 3);
	}

	// ------------------------------------------------------------------------
	// ARG no access to private inner classes of ComposterBlock, so re-implement new version here

	@Override
	public ISidedInventory createInventory(BlockState p_219966_1_, IWorld p_219966_2_, BlockPos p_219966_3_)
	{
		int i = p_219966_1_.get(LEVEL);
		if (i == 8)
		{
			return new XComposterBlock.XFullInventory(p_219966_1_, p_219966_2_, p_219966_3_,
					new ItemStack(Items.BONE_MEAL));
		}
		else
		{
			return (ISidedInventory) (i < 7
					? new XComposterBlock.XPartialInventory(p_219966_1_, p_219966_2_, p_219966_3_)
					: new XComposterBlock.XEmptyInventory());
		}
	}

	static class XEmptyInventory extends Inventory implements ISidedInventory
	{
		public XEmptyInventory()
		{
			super(0);
		}

		public int[] getSlotsForFace(Direction side)
		{
			return new int[0];
		}

		/**
		 * Returns true if automation can insert the given item in the given slot from
		 * the given side.
		 */
		public boolean canInsertItem(int index, ItemStack itemStackIn, @Nullable Direction direction)
		{
			return false;
		}

		/**
		 * Returns true if automation can extract the given item in the given slot from
		 * the given side.
		 */
		public boolean canExtractItem(int index, ItemStack stack, Direction direction)
		{
			return false;
		}
	}

	static protected class XFullInventory extends Inventory implements ISidedInventory
	{
		private final BlockState state;
		private final IWorld world;
		private final BlockPos pos;
		private boolean extracted;

		public XFullInventory(BlockState p_i50463_1_, IWorld p_i50463_2_, BlockPos p_i50463_3_, ItemStack p_i50463_4_)
		{
			super(p_i50463_4_);
			this.state = p_i50463_1_;
			this.world = p_i50463_2_;
			this.pos = p_i50463_3_;
		}

		/**
		 * Returns the maximum stack size for a inventory slot. Seems to always be 64,
		 * possibly will be extended.
		 */
		public int getInventoryStackLimit()
		{
			return 1;
		}

		public int[] getSlotsForFace(Direction side)
		{
			return side == Direction.DOWN ? new int[] { 0 } : new int[0];
		}

		/**
		 * Returns true if automation can insert the given item in the given slot from
		 * the given side.
		 */
		public boolean canInsertItem(int index, ItemStack itemStackIn, @Nullable Direction direction)
		{
			return false;
		}

		/**
		 * Returns true if automation can extract the given item in the given slot from
		 * the given side.
		 */
		public boolean canExtractItem(int index, ItemStack stack, Direction direction)
		{
			return !this.extracted && direction == Direction.DOWN && stack.getItem() == Items.BONE_MEAL;
		}

		/**
		 * For tile entities, ensures the chunk containing the tile entity is saved to
		 * disk later - the game won't think it hasn't changed and skip it.
		 */
		public void markDirty()
		{
			Block block = this.world.getBlockState(pos).getBlock();
			if (block instanceof XComposterBlock)
			{
				XComposterBlock xblock = (XComposterBlock)block;
				xblock.clear(this.state, this.world, this.pos);
			}
			this.extracted = true;
		}
	}

	static class XPartialInventory extends Inventory implements ISidedInventory
	{
		private final BlockState state;
		private final IWorld world;
		private final BlockPos pos;
		private boolean inserted;

		public XPartialInventory(BlockState p_i50464_1_, IWorld p_i50464_2_, BlockPos p_i50464_3_)
		{
			super(1);
			this.state = p_i50464_1_;
			this.world = p_i50464_2_;
			this.pos = p_i50464_3_;
		}

		/**
		 * Returns the maximum stack size for a inventory slot. Seems to always be 64,
		 * possibly will be extended.
		 */
		public int getInventoryStackLimit()
		{
			return 1;
		}

		public int[] getSlotsForFace(Direction side)
		{
			return side == Direction.UP ? new int[] { 0 } : new int[0];
		}

		/**
		 * Returns true if automation can insert the given item in the given slot from
		 * the given side.
		 */
		public boolean canInsertItem(int index, ItemStack itemStackIn, @Nullable Direction direction)
		{
			Block block = this.world.getBlockState(pos).getBlock();
			if (block instanceof XComposterBlock)
			{
				XComposterBlock xblock = (XComposterBlock)block;
				return !this.inserted && direction == Direction.UP
						&& xblock.isCompostable(itemStackIn);			
			}

			return false;
		}

		/**
		 * Returns true if automation can extract the given item in the given slot from
		 * the given side.
		 */
		public boolean canExtractItem(int index, ItemStack stack, Direction direction)
		{
			return false;
		}

		/**
		 * For tile entities, ensures the chunk containing the tile entity is saved to
		 * disk later - the game won't think it hasn't changed and skip it.
		 */
		public void markDirty()
		{
			ItemStack itemstack = this.getStackInSlot(0);
			if (!itemstack.isEmpty())
			{
				this.inserted = true;
				Block block = this.world.getBlockState(pos).getBlock();
				if (block instanceof XComposterBlock)
				{
					XComposterBlock xblock = (XComposterBlock)block;
					xblock.addItem(this.state, this.world, this.pos, itemstack);
				}
				this.removeStackFromSlot(0);
			}

		}
	}
}
