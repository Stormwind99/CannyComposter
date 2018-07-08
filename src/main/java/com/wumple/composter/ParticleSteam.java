package com.wumple.composter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleSmokeNormal;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleSteam extends ParticleSmokeNormal
{
	private ParticleSteam(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn)
	{
        super(worldIn, xCoordIn, yCoordIn, xCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, 1.0F);
	}
	
	protected ParticleSteam(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, float p_i46348_14_)
	{
        super(worldIn, xCoordIn, yCoordIn, xCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, p_i46348_14_);

        float color = .7f + (float)Math.random() * .3f;
        setRBGColorF(color, color, color);
    }

    public static ParticleSteam spawnParticle(World worldIn, double xCoord, double yCoord, double zCoord)
    {
    	ParticleSteam particle = new ParticleSteam(worldIn, xCoord, yCoord, zCoord, 0, 0, 0);
    	Minecraft.getMinecraft().effectRenderer.addEffect(particle);
    	return particle;
    }

	/*
    public static EntityFX spawnParticle (World world, double x, double y, double z) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.renderViewEntity != null && mc.effectRenderer != null) {
            int setting = mc.gameSettings.particleSetting;
            if (setting == 1 && mc.theWorld.rand.nextInt(3) == 0)
                setting = 2;

            double dx = mc.renderViewEntity.posX - x;
            double dy = mc.renderViewEntity.posY - y;
            double dz = mc.renderViewEntity.posZ - z;

            if (dx * dx + dy * dy + dz * dz > 16 * 16)
                return null;
            if (setting > 1)
                return null;

            EntityFX effect = new EntitySteamFX(world, x, y, z);
            mc.effectRenderer.addEffect(effect);

            return effect;
        }

        return null;
    }
    */
    
    @SideOnly(Side.CLIENT)
    public static class Factory implements IParticleFactory
        {
            public Particle createParticle(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_)
            {
                return new ParticleSteam(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
            }
        }
}