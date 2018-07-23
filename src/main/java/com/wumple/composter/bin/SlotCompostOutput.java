package com.wumple.composter.bin;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotCompostOutput extends Slot
{
    // private final IInventory inputInventory;
    private EntityPlayer player;
    private int amountCrafted;

    public SlotCompostOutput(EntityPlayer player, IInventory inputInventory, int par2, int par3, int par4)
    {
        super(inputInventory, par2, par3, par4);

        this.player = player;
        // this.inputInventory = inputInventory;
    }

    @Override
    public boolean isItemValid(ItemStack itemStack)
    {
        return false;
    }

    @Override
    public ItemStack decrStackSize(int count)
    {
        if (getHasStack())
        {
            amountCrafted += Math.min(count, getStack().getCount());
        }

        return super.decrStackSize(count);
    }

    @Override
    protected void onCrafting(ItemStack itemStack, int count)
    {
        amountCrafted += count;
        super.onCrafting(itemStack, count);
    }

    @Override
    protected void onCrafting(ItemStack itemStack)
    {
        itemStack.onCrafting(player.getEntityWorld(), player, amountCrafted);
        amountCrafted = 0;

        // FMLCommonHandler.instance().firePlayerCraftingEvent(thePlayer, stack, inputInventory);
    }

    @Override
    public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack)
    {
        // FMLCommonHandler.instance().firePlayerCraftingEvent(thePlayer, stack, inputInventory);

        onCrafting(stack);

        return super.onTake(thePlayer, stack);
    }
}