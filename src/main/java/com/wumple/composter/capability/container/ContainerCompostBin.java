package com.wumple.composter.capability.container;

import java.util.ArrayList;
import java.util.List;

import com.wumple.composter.capability.CompostBinCap;
import com.wumple.composter.capability.ICompostBinCap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerCompostBin extends Container
{
    private static final int InventoryX = 8;
    private static final int InventoryY = 84;
    private static final int HotbarY = 142;

    private ICompostBinCap tileCompost;

    private Slot outputSlot;
    private List<Slot> compostSlots;
    private List<Slot> playerSlots;
    private List<Slot> hotbarSlots;

    public ContainerCompostBin(InventoryPlayer inventory, ICompostBinCap tileEntity)
    {
        tileCompost = tileEntity;

        compostSlots = new ArrayList<Slot>();
        for (int y = 0; y < 3; y++)
        {
            for (int x = 0; x < 3; x++)
                compostSlots.add(addSlotToContainer(new SlotCompost(tileEntity, x + y * 3, 30 + x * 18, 17 + y * 18)));
        }

        outputSlot = addSlotToContainer(new SlotCompostOutput(inventory.player, tileEntity, 9, 123, 34));

        playerSlots = new ArrayList<Slot>();
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
                playerSlots.add(addSlotToContainer(
                        new Slot(inventory, j + i * 9 + 9, InventoryX + j * 18, InventoryY + i * 18)));
        }

        hotbarSlots = new ArrayList<Slot>();
        for (int i = 0; i < 9; i++)
            hotbarSlots.add(addSlotToContainer(new Slot(inventory, i, InventoryX + i * 18, HotbarY)));
    }

    @Override
    public void addListener(IContainerListener listener)
    {
        super.addListener(listener);
        listener.sendAllWindowProperties(this, this.tileCompost);
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();
        tileCompost.detectAndSendChanges(this, listeners);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void updateProgressBar(int id, int value)
    {
        tileCompost.updateProgressBar(id, value);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return tileCompost.isUsableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex)
    {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot) inventorySlots.get(slotIndex);

        int compostStart = compostSlots.get(0).slotNumber;
        int compostEnd = compostSlots.get(compostSlots.size() - 1).slotNumber + 1;

        // Assume inventory and hotbar slot IDs are contiguous
        int inventoryStart = playerSlots.get(0).slotNumber;
        int hotbarStart = hotbarSlots.get(0).slotNumber;
        int hotbarEnd = hotbarSlots.get(hotbarSlots.size() - 1).slotNumber + 1;

        if (slot != null && slot.getHasStack())
        {
            ItemStack slotStack = slot.getStack();
            itemStack = slotStack.copy();

            // Try merge output into inventory and signal change
            if (slotIndex == outputSlot.slotNumber)
            {
                if (!mergeItemStack(slotStack, inventoryStart, hotbarEnd, true))
                {
                    return ItemStack.EMPTY;
                }

                slot.onSlotChange(slotStack, itemStack);
                tileCompost.updateBlockState();
            }

            // Try merge stacks within inventory and hotbar spaces
            else if (slotIndex >= inventoryStart && slotIndex < hotbarEnd)
            {
                if (!CompostBinCap.isItemDecomposable(slotStack)
                        || !mergeItemStack(slotStack, compostStart, compostEnd, false))
                {
                    if (slotIndex >= inventoryStart && slotIndex < hotbarStart)
                    {
                        if (!mergeItemStack(slotStack, hotbarStart, hotbarEnd, false))
                        {
                            // return null;
                            return ItemStack.EMPTY;
                        }
                    }
                    else if (slotIndex >= hotbarStart && slotIndex < hotbarEnd
                            && !this.mergeItemStack(slotStack, inventoryStart, hotbarStart, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
            }

            // Try merge stack into inventory
            else if (!mergeItemStack(slotStack, inventoryStart, hotbarEnd, false))
            {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty())
            {
                slot.putStack(ItemStack.EMPTY);
            }
            else
            {
                slot.onSlotChanged();
            }

            if (slotStack.getCount() == itemStack.getCount())
            {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return itemStack;
    }
}
