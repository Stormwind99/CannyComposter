package com.wumple.composter;

import com.wumple.composter.config.ModConfig;
import com.wumple.util.RegistrationHelpers;

import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemCompost extends Item
{
    public ItemCompost()
    {
        setMaxStackSize(64);
        setCreativeTab(CreativeTabs.MISC);

        RegistrationHelpers.nameHelper(this, "composter:compost");
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ItemStack itemstack = playerIn.getHeldItem(hand);

        if (!playerIn.canPlayerEdit(pos.offset(facing), facing, itemstack))
        {
            return EnumActionResult.FAIL;
        }

        return applyEnrichment(itemstack, worldIn, pos, playerIn, hand) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
    }

    public boolean applyEnrichment (ItemStack itemStack, World worldIn, BlockPos target, EntityPlayer player, EnumHand hand)
    {
        if (!ModConfig.enableCompostBonemeal)
            return false;

        IBlockState iblockstate = worldIn.getBlockState(target);
        
        if (iblockstate.getBlock() instanceof IGrowable)
        {
            IGrowable igrowable = (IGrowable)iblockstate.getBlock();

            if (igrowable.canGrow(worldIn, target, iblockstate, worldIn.isRemote))
            {
            	int prob = (ModConfig.compostBonemealStrength == 0) ? 0 : (int)(1 / ModConfig.compostBonemealStrength);
            	if (worldIn.rand.nextInt(prob) == 0)
            	{
            		return ItemDye.applyBonemeal(itemStack, worldIn, target, player, hand);
            	}
            	else
            	{
            		// compost works less of the time, so use one up when used and it doesn't work
            		itemStack.shrink(1);
            	}
            }
        }
   
        return false;
    }
}
