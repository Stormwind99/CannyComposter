package com.wumple.cannycomposter.things;

import java.util.Random;

import com.wumple.cannycomposter.ConfigManager;
import com.wumple.util.misc.TimeUtil;
import com.wumple.util.xcomposter.XComposterBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class CannyComposterBlock extends XComposterBlock
{
	public CannyComposterBlock()
	{
		super();
	}

	public CannyComposterBlock(Block.Properties properties)
	{
		super(properties);
	}

	// ------------------------------------------------------------------------
	// For extension

	@Override
	protected boolean isCompostable(ItemStack itemstack)
	{
		return (ConfigManager.compostablesConfig.getValue(itemstack) != 0);
	}

	@Override
	protected float getCompostLevelOf(ItemStack itemstack)
	{
		Integer ilevel = ConfigManager.compostablesConfig.getValue(itemstack);
		float flevel = ilevel.floatValue() / 100.0F;
		return flevel;
	}

	// remove => WumpleUtil 3.6
	@Override
	protected ItemStack makeCompost()
	{
		return new ItemStack(getCompostItem());
	}
	
	// add => WumpleUtil 3.6
	// @Override
	protected Item getCompostItem()
	{
		IForgeRegistry<Item> reg = GameRegistry.findRegistry(Item.class);
		ResourceLocation res = new ResourceLocation(ConfigManager.General.compost.get());
		Item item = reg.getValue(res);
		return item;
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
			// increase compost level
			int j = i + 1;
			worldIn.setBlockState(posIn, blockstateIn.with(LEVEL, Integer.valueOf(j)), 3);
			
			// schedule smoke and compost generation (if tick not already scheduled)
			worldIn.getPendingBlockTicks().scheduleTick(posIn, blockstateIn.getBlock(), getWaitTicks(worldIn));

			return true;
		}
	}
	
	protected void generateSteam(World worldIn, BlockPos pos, int level)
	{
		double yadd = 0.0D; 		
				
		BlockState blockstate = worldIn.getBlockState(pos);
		
		if (blockstate.getMaterial() != Material.AIR)
		{
			yadd += blockstate.getShape(worldIn, pos).getEnd(Direction.Axis.Y);
		}

		float ratio = level / 8;
		double x = (double) pos.getX() + 0.5D;
		double y = (double) pos.getY() + yadd;
		double z = (double) pos.getZ() + 0.5D;
		// more particles the more full the bin is
		int num = 1 + Math.round(ratio);
		
		//((ServerWorld) worldIn).spawnParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, num, 0.1D, 0.2D, 0.1D, 0.1D);
		((ServerWorld) worldIn).spawnParticle(ParticleTypes.CLOUD, x, y, z, num, 0.0D, 0.0D, 0.0D, 0.0D);
	}
	
	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, Random random)
	{
		if (!worldIn.isRemote())
		{
			int level = state.get(LEVEL);
			if ((level > 0) && (level < 7))
			{
				if (ConfigManager.General.cookSteam.get())
				{
					generateSteam(worldIn, pos, level);
				}
				
				// tick again for more smoke later
				BlockState blockstate = worldIn.getBlockState(pos);
				
				worldIn.getPendingBlockTicks().scheduleTick(pos, blockstate.getBlock(), getWaitTicks(worldIn));
			}
		}

		super.tick(state, worldIn, pos, random);
	}
	
	@Override
	protected int getWaitTicks(IWorld worldIn)
	{
		int randomTime = ConfigManager.General.cookMaxTime.get() - ConfigManager.General.cookMinTime.get(); 
		int seconds = ConfigManager.General.cookMaxTime.get() + worldIn.getRandom().nextInt(randomTime);
		int ticks = Math.max(1, TimeUtil.TICKS_PER_SECOND * seconds);
		return ticks;
	}
}
