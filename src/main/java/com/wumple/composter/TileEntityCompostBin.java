package com.wumple.composter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityCompostBin extends TileEntity implements IInventory, ITickable
{
	private NonNullList<ItemStack> itemStacks = NonNullList.<ItemStack>withSize(10, ItemStack.EMPTY);

    // The number of ticks remaining to decompose the current item
    public int binDecomposeTime;

    // The slot actively being decomposed
    private int currentItemSlot;

    // The number of ticks that a fresh copy of the currently-decomposing item would decompose for
    public int currentItemDecomposeTime;

    public int itemDecomposeCount;

    private String customName;
    
    public int getDecompTime () {
        return binDecomposeTime;
    }

    public int getCurrentItemDecompTime () {
        return currentItemDecomposeTime;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        
        ItemStackHelper.loadAllItems(compound, this.itemStacks);

        binDecomposeTime = compound.getShort("DecompTime");
        currentItemSlot = compound.getByte("DecompSlot");
        itemDecomposeCount = compound.getByte("DecompCount");

        if (currentItemSlot >= 0)
            currentItemDecomposeTime = getItemDecomposeTime(itemStacks.get(currentItemSlot));
        else
            currentItemDecomposeTime = 0;

        if (compound.hasKey("CustomName", 8))
        {
            this.customName = compound.getString("CustomName");
        }
    }

    @Override
    public NBTTagCompound writeToNBT (NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setShort("DecompTime", (short)binDecomposeTime);
        compound.setByte("DecompSlot", (byte)currentItemSlot);
        compound.setByte("DecompCount", (byte)itemDecomposeCount);

        ItemStackHelper.saveAllItems(compound, this.itemStacks);
 
        if (this.hasCustomName())
        {
            compound.setString("CustomName", this.customName);
        }
        
        return compound;
    }

    /*
    @Override
    public Packet getDescriptionPacket () {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);

        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 5, tag);
    }

    @Override
    public void onDataPacket (NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBT(pkt.func_148857_g());
        getthis.getWorld()().func_147479_m(xCoord, yCoord, zCoord); // markBlockForRenderUpdate
    }
    */

    public boolean isDecomposing () {
        return binDecomposeTime > 0;
    }

    @SideOnly(Side.CLIENT)
    public int getDecomposeTimeRemainingScaled (int scale) {
        if (currentItemDecomposeTime == 0)
            currentItemDecomposeTime = 200;

        return (8 - itemDecomposeCount) * scale / 9 + (binDecomposeTime * scale / (currentItemDecomposeTime * 9));

        //return binDecomposeTime * scale / currentItemDecomposeTime;
    }

    public void update() {
        boolean isDecomposing = binDecomposeTime > 0;
        int decompCount = itemDecomposeCount;

        boolean shouldUpdate = false;

        if (binDecomposeTime > 0)
            --binDecomposeTime;

        if (!this.getWorld().isRemote) {
            int filledSlotCount = 0;
            for (int i = 0; i < 9; i++)
                filledSlotCount += (itemStacks.get(i) != null) ? 1 : 0;

            if (binDecomposeTime != 0 || filledSlotCount > 0) {
                if (binDecomposeTime == 0) {
                    /*if (currentItemSlot >= 0 && itemStacks.get(currentItemSlot) != null) {
                        --itemStacks.get(currentItemSlot).getCount();
                        shouldUpdate = true;

                        if (itemStacks.get(currentItemSlot).getCount() == 0)
                            itemStacks.get(currentItemSlot) = itemStacks.get(currentItemSlot).getItem().getContainerItem(itemStacks.get(currentItemSlot));
                    }*/
                    if (canCompost()) {
                        compostItem();
                        shouldUpdate = true;
                    }

                    currentItemSlot = selectRandomFilledSlot();
                    currentItemDecomposeTime = 0;

                    if (currentItemSlot >= 0 && (itemStacks.get(9) == null || itemStacks.get(9).getCount() < 64)) {
                        currentItemDecomposeTime = getItemDecomposeTime(itemStacks.get(currentItemSlot));
                        binDecomposeTime = currentItemDecomposeTime;

                        if (binDecomposeTime > 0)
                            shouldUpdate = true;
                    }
                }
            }

            if (isDecomposing != binDecomposeTime > 0 || decompCount != itemDecomposeCount) {
                shouldUpdate = true;
                BlockCompostBin.updateBlockState(this.getWorld(), this.getPos());
            }
        }

        if (shouldUpdate)
            markDirty();
    }

    private boolean canCompost () {
        if (currentItemSlot == -1)
            return false;
        if (itemStacks.get(currentItemSlot) == null)
            return false;
        if (itemStacks.get(currentItemSlot).getCount() == 0)
            return false;

        if ((itemStacks.get(9) == null) || itemStacks.get(9).isEmpty())
            return true;

        int result = itemStacks.get(9).getCount() + 1;
        return result <= getInventoryStackLimit() && result <= itemStacks.get(9).getMaxStackSize();
    }

    public void compostItem () {
        if (canCompost()) {
            if (itemDecomposeCount < 8)
                itemDecomposeCount++;

            if (itemDecomposeCount == 8) {
                ItemStack resultStack = new ItemStack(ObjectHolder.compost);

                if (itemStacks.get(9) == null)
                    itemStacks.set(9, resultStack);
                else if (itemStacks.get(9).getItem() == resultStack.getItem())
                    itemStacks.get(9).setCount( itemStacks.get(9).getCount() + resultStack.getCount() );

                itemDecomposeCount = 0;
            }

            itemStacks.get(currentItemSlot).setCount( itemStacks.get(currentItemSlot).getCount() - 1);
            if (itemStacks.get(currentItemSlot).getCount() == 0)
                itemStacks.set(currentItemSlot, ItemStack.EMPTY);

            currentItemSlot = -1;
        }
    }

    public boolean hasInputItems () {
        int filledSlotCount = 0;
        for (int i = 0; i < 9; i++)
            filledSlotCount += (itemStacks.get(i) != null) ? 1 : 0;

        return filledSlotCount > 0;
    }

    public boolean hasOutputItems () {
    	ItemStack stack = itemStacks.get(9);
        return (stack != null) && (!stack.isEmpty()) ;
    }

    private int selectRandomFilledSlot () {
        int filledSlotCount = 0;
        for (int i = 0; i < 9; i++)
            filledSlotCount += (itemStacks.get(i) != null) ? 1 : 0;

        if (filledSlotCount == 0)
            return -1;

        int index = this.getWorld().rand.nextInt(filledSlotCount);
        for (int i = 0, c = 0; i < 9; i++) {
            if (itemStacks.get(i) != null) {
                if (c++ == index)
                    return i;
            }
        }

        return -1;
    }

    public static int getItemDecomposeTime (ItemStack itemStack) {
        if ((itemStack == null) || itemStack.isEmpty())
            return 0;

        /*
        // TODO
        ICompostMaterial material = GardenAPI.instance().registries().compost().getCompostMaterialInfo(itemStack);
        if (material == null)
            return 0;

        return material.getDecomposeTime();
        */
        
        return (itemStack.getItem() instanceof ItemFood) ? 125 : 0;
    }

    public static boolean isItemDecomposable (ItemStack itemStack) {
        return getItemDecomposeTime(itemStack) > 0;
    }
 
    /*
    @Override
    public ItemStack getStackInSlotOnClosing (int slot) {
        return null;
    }
    */

    /**
     * Get the name of this object. For players this returns their username
     */
    public String getName()
    {
        return this.hasCustomName() ? this.customName : "composter.compost_bin";
    }

    /**
     * Returns true if this thing is named
     */
    public boolean hasCustomName()
    {
        return this.customName != null && !this.customName.isEmpty();
    }

    public void setName(String name)
    {
        this.customName = name;
    }

    /**
     * Returns the number of slots in the inventory.
     */
    public int getSizeInventory()
    {
        return this.itemStacks.size();
    }

    public boolean isEmpty()
    {
        for (ItemStack itemstack : this.itemStacks)
        {
            if ((itemstack != null) && !itemstack.isEmpty())
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isItemValidForSlot (int slot, ItemStack item) {
        if (slot >= 0 && slot < 9)
            return isItemDecomposable(item);

        return false;
    }

    /*
    private int[] accessSlots = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

    @Override
    public int[] getAccessibleSlotsFromSide (int side) {
        return accessSlots;
    }
    
    @Override
    public boolean isItemValidForSlot(int slot, ItemStack item) {
        if (slot == 9)
            return false;

        return isItemValidForSlot(slot, item);
    }

    @Override
    public boolean canExtractItem (int slot, ItemStack item, int side) {
        if (slot != 9)
            return false;

        if (item == null)
            return false;

        return item.getItem() == ModItems.compostPile;
    }
     */
    
    // ----------------------------------------------------------------------
    // IInventory
    
    /**
     * Returns the stack in the given slot.
     */
    public ItemStack getStackInSlot(int index)
    {
        return index >= 0 && index < this.itemStacks.size() ? (ItemStack)this.itemStacks.get(index) : ItemStack.EMPTY;
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    public ItemStack decrStackSize(int index, int count)
    {
    	ItemStack stack = ItemStackHelper.getAndSplit(this.itemStacks, index, count);
    	
        if (index == 9)
        {
            BlockCompostBin.updateBlockState(this.getWorld(), this.getPos());
        }
        
        return stack;
    }

    /**
     * Removes a stack from the given slot and returns it.
     */
    public ItemStack removeStackFromSlot(int index)
    {
        return ItemStackHelper.getAndRemove(this.itemStacks, index);
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        if (index >= 0 && index < this.itemStacks.size())
        {
            this.itemStacks.set(index, stack);
        }
    }

    /**
     * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
     */
    public int getInventoryStackLimit()
    {
        return 64;
    }

    /**
     * Don't rename this method to canInteractWith due to conflicts with Container
     */
    public boolean isUsableByPlayer(EntityPlayer player)
    {
        if (this.world.getTileEntity(this.pos) != this)
        {
            return false;
        }
        else
        {
            return player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
        }
    }

    public void openInventory(EntityPlayer player)
    {
    }

    public void closeInventory(EntityPlayer player)
    {
    }

    public int getField(int id)
    {
        return 0;
    }

    public void setField(int id, int value)
    {
    }

    public int getFieldCount()
    {
        return 0;
    }

    public void clear()
    {
        this.itemStacks.clear();
    }
}
