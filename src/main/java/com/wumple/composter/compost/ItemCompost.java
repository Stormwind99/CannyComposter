package com.wumple.composter.compost;

import com.wumple.composter.config.ModConfig;
import com.wumple.util.misc.RegistrationHelpers;

import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class ItemCompost extends Item
{
    protected static final int NUM_PARTICLES = 15;
    public static final String ID = "composter:compost";

    public ItemCompost()
    {
        setMaxStackSize(64);
        setCreativeTab(CreativeTabs.MISC);

        RegistrationHelpers.nameHelper(this, ID);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ItemStack itemstack = playerIn.getHeldItem(hand);

        if (!playerIn.canPlayerEdit(pos.offset(facing), facing, itemstack))
        {
            return EnumActionResult.FAIL;
        }

        return applyEnrichment(itemstack, worldIn, pos, playerIn, hand) ? EnumActionResult.SUCCESS
                : EnumActionResult.PASS;
    }

    public boolean applyEnrichment(ItemStack stack, World worldIn, BlockPos pos, EntityPlayer player,
            EnumHand hand)
    {
        if (!ModConfig.enableCompostBonemeal)
            return false;

        if (!(worldIn instanceof net.minecraft.world.WorldServer))
        {
            return false;
        }

        WorldServer worldSIn = (WorldServer) worldIn;

        IBlockState iblockstate = worldIn.getBlockState(pos);

        int hook = net.minecraftforge.event.ForgeEventFactory.onApplyBonemeal(player, worldIn, pos, iblockstate,
                stack, hand);
        if (hook != 0)
            return hook > 0;

        if (iblockstate.getBlock() instanceof IGrowable)
        {
            IGrowable igrowable = (IGrowable) iblockstate.getBlock();

            if (igrowable.canGrow(worldIn, pos, iblockstate, worldIn.isRemote))
            {
                int prob = (ModConfig.compostBonemealStrength == 0) ? 0 : (int) (1 / ModConfig.compostBonemealStrength);
                if (worldIn.rand.nextInt(prob) == 0)
                {
                    if (igrowable.canUseBonemeal(worldIn, worldIn.rand, pos, iblockstate))
                    {
                        igrowable.grow(worldIn, worldIn.rand, pos, iblockstate);
                    }

                    stack.shrink(1);
                    spawnParticles(worldSIn, pos, NUM_PARTICLES, EnumParticleTypes.VILLAGER_HAPPY);
                    return true;
                }
                else
                {
                    // compost works less of the time, so use one up when used and it doesn't work
                    stack.shrink(1);
                    spawnParticles(worldSIn, pos, NUM_PARTICLES * 2, EnumParticleTypes.TOWN_AURA);
                }
            }
        }

        return false;
    }

    public static void spawnParticles(WorldServer worldIn, BlockPos pos, int amount, EnumParticleTypes particleType)
    {
        if (amount == 0)
        {
            amount = 15;
        }

        IBlockState iblockstate = worldIn.getBlockState(pos);

        double yadd = 0.5D;
        if (iblockstate.getMaterial() != Material.AIR)
        {
            yadd += iblockstate.getBoundingBox(worldIn, pos).maxY;
        }
        double x = (double) pos.getX() + 0.5D;
        double y = (double) pos.getY() + yadd;
        double z = (double) pos.getZ() + 0.5D;

        worldIn.spawnParticle(particleType, x, y, z, amount, 0.5D, 0.1D, 0.5D, 0.5D);
    }
}
