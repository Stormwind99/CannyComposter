package com.wumple.cannycomposter.things;

import com.wumple.cannycomposter.ConfigManager;
import com.wumple.util.xcomposter.XBoneMealItem;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class CompostItem extends XBoneMealItem
{
	public CompostItem(Item.Properties properties)
	{
		super(properties);
	}

	public CompostItem()
	{
		this(new Item.Properties());
	}

	protected double getUseChance()
	{
		return ConfigManager.General.compostChance.get();
	}

	@Override
	protected boolean tryUse(World worldIn)
	{
		boolean result = (worldIn.rand.nextDouble() <= getUseChance());

		return result;
	}

	@Override
	protected void spawnSuccessParticles(World worldIn, BlockPos pos)
	{
		if (!worldIn.isRemote)
		{
			spawnParticles2(ParticleTypes.HAPPY_VILLAGER, (ServerWorld) worldIn, pos, NUM_PARTICLES, 0.5D, 0.1D, 0.5D, 0.5D, 0.5D);
		}
	}

	@Override
	protected void spawnTryFailureParticles(World worldIn, BlockPos pos)
	{
		if (!worldIn.isRemote)
		{
			spawnParticles2(ParticleTypes.CAMPFIRE_COSY_SMOKE, (ServerWorld) worldIn, pos, NUM_PARTICLES/4, 0.1D, 0.2D, 0.1D, 0.01D, 0.1D);
		}
	}

	public <T extends IParticleData> void spawnParticles2(T particleType, ServerWorld worldIn, BlockPos posIn, int amount, double dx, double dy, double dz, double speed, double yaddIn)
	{
		if (amount == 0)
		{
			amount = 15;
		}
	
		BlockState blockstate = worldIn.getBlockState(posIn);
	
		double yadd = yaddIn;
		if (blockstate.getMaterial() != Material.AIR)
		{
			yadd += blockstate.getShape(worldIn, posIn).getEnd(Direction.Axis.Y);
			// OLD yadd += iblockstate.getCollisionShape(worldIn, pos).getBoundingBox().maxY;
		}
		double x = (double) posIn.getX() + 0.5D;
		double y = (double) posIn.getY() + yadd;
		double z = (double) posIn.getZ() + 0.5D;
	
		// was:  public void spawnParticle(EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord, int numberOfParticles, double xOffset, double yOffset, double zOffset, double particleSpeed, int... particleArguments)
		// now:  public <T extends IParticleData> int spawnParticle(T type, double posX, double posY, double posZ, int particleCount, double xOffset, double yOffset, double zOffset, double speed) {
		worldIn.spawnParticle(particleType, x, y, z, amount, dx, dy, dz, speed);
	}
	
}
