package com.wumple.composter.bin;

import com.wumple.composter.config.ConfigHandler;
import com.wumple.composter.config.ModConfig;
import com.wumple.util.SUtil;
import com.wumple.util.TypeIdentifier;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityCompostBin extends TileEntity implements IInventory, ITickable
{
	public static final int COMPOSTING_SLOTS = 9;
	public static final int TOTAL_SLOTS = COMPOSTING_SLOTS + 1;
	public static final int OUTPUT_SLOT = TOTAL_SLOTS - 1; 
	public static final int STACK_LIMIT = 64;
	public static final int NO_SLOT = -1;
	public static final int NO_DECOMPOSE_TIME = -1;
	public static final double USE_RANGE = 64.0D;
	public static final int PARTICLE_INTERVAL = 64;
	public static final int DECOMPOSE_TIME_MAX = 200;
	
	private NonNullList<ItemStack> itemStacks = NonNullList.<ItemStack>withSize(TOTAL_SLOTS, ItemStack.EMPTY);

    // The number of ticks remaining to decompose the current item
    public int binDecomposeTime = NO_DECOMPOSE_TIME;

    // The slot actively being decomposed
    private int currentItemSlot = NO_SLOT;

    // The number of ticks that a fresh copy of the currently-decomposing item would decompose for
    public int currentItemDecomposeTime;

    // number of items decomposed since last compost generation
    public int itemDecomposeCount;

    private String customName;
    
    // ----------------------------------------------------------------------
    // TileEntityCompostBin
    
    public int getDecompTime()
    {
        return binDecomposeTime;
    }

    public int getCurrentItemDecompTime()
    {
        return currentItemDecomposeTime;
    }

    public boolean isDecomposing()
    {
        return binDecomposeTime > 0;
    }

    protected int getDecomposeNeeded()
    {
    	return ModConfig.decomposeNeeded;
    }
    
    @SideOnly(Side.CLIENT)
    public int getDecomposeTimeRemainingScaled(int scale)
    {
        if (currentItemDecomposeTime == 0)
        {
            currentItemDecomposeTime = DECOMPOSE_TIME_MAX;
        }

        //return binDecomposeTime * scale / currentItemDecomposeTime;
        return (getDecomposeNeeded() - itemDecomposeCount) * scale / COMPOSTING_SLOTS + (binDecomposeTime * scale / (currentItemDecomposeTime * COMPOSTING_SLOTS));
    }

    private boolean canCompost()
    {
        if ( (currentItemSlot == NO_SLOT) || (!isItemDecomposable(itemStacks.get(currentItemSlot))) )
        {
        	return false;
        }

        if (!hasOutputItems())
        {
            return true;
        }
        
        ItemStack outputSlotStack = itemStacks.get(OUTPUT_SLOT);
        ItemStack newStack = getCompostItem(1);
        
        return SUtil.canStack(outputSlotStack, newStack);
    }
    
    public void compostItem()
    {
        if (canCompost())
        {
        	int decomposeNeeded = getDecomposeNeeded();
        	
            if (itemDecomposeCount < decomposeNeeded)
            {
                itemDecomposeCount++;
            }

            if (itemDecomposeCount >= decomposeNeeded)
            {
                
                if (!hasOutputItems())
                {
                	ItemStack resultStack = getCompostItem(1);
                    itemStacks.set(OUTPUT_SLOT, resultStack);
                }
                else
                {
                    itemStacks.get(OUTPUT_SLOT).grow(1);
                }

                itemDecomposeCount = 0;
            }

            SUtil.shrink(itemStacks, currentItemSlot, 1);

            currentItemSlot = NO_SLOT;
            binDecomposeTime = NO_DECOMPOSE_TIME; 
        }
    }
    
    protected int getFilledSlots()
    {
        int filledSlotCount = 0;
        for (int i = 0; i < COMPOSTING_SLOTS; i++)
        {
            filledSlotCount += (isItemDecomposable(itemStacks.get(i))) ? 1 : 0;
        }

        return filledSlotCount;
    }

    public boolean hasInputItems()
    {
        return getFilledSlots() > 0;
    }

    public boolean hasOutputItems()
    {
        return !SUtil.isEmpty(itemStacks.get(OUTPUT_SLOT));
    }

    private int selectRandomFilledSlot()
    {
        int filledSlotCount = getFilledSlots();
     
        if (filledSlotCount == 0)
        {
            return NO_SLOT;
        }

        int index = this.getWorld().rand.nextInt(filledSlotCount);
        for (int i = 0, c = 0; i < COMPOSTING_SLOTS; i++)
        {
            if (isItemDecomposable(itemStacks.get(i)))
            {
                if (c++ == index)
                {
                    return i;
                }
            }
        }

        return NO_SLOT;
    }

    public static int getItemDecomposeTime(ItemStack itemStack)
    {
        if (SUtil.isEmpty(itemStack))
        {
            return NO_DECOMPOSE_TIME;
        }

        int amount = ConfigHandler.compostAmounts.getValue(itemStack);
        
        return amount;
    }

    public static boolean isItemDecomposable(ItemStack itemStack)
    {
        return getItemDecomposeTime(itemStack) > NO_DECOMPOSE_TIME;
    }

    public boolean isEmpty()
    {
        for (ItemStack itemstack : this.itemStacks)
        {
            if (!SUtil.isEmpty(itemstack))
            {
                return false;
            }
        }

        return true;
    }
    
    public void setName(String name)
    {
        this.customName = name;
    }
    
    public String getRealName()
    {
    	return "container.composter.compost_bin";
    }
    
    public float getFilledRatio()
    {
		int slots = getFilledSlots() + (hasOutputItems() ? 1 : 0);
    	return (float)slots / (float) TOTAL_SLOTS;
    }
    
    protected void updateBlockState()
    {
    	World world = this.getWorld();
    	
    	if (!world.isRemote)
    	{
	    	BlockPos pos = this.getPos();
	    	
	    	IBlockState state = world.getBlockState(pos);
	    	Block block = state.getBlock();
	    	if (block instanceof BlockCompostBin)
	    	{
	    		BlockCompostBin bin = (BlockCompostBin)block;
	    		float ratio = getFilledRatio();
	    		bin.setContentsLevel(world, pos, state, ratio);
	    	}
    	}
    	
        markDirty();
    }

    protected void updateInternalState(int index)
    {
        if (index == OUTPUT_SLOT)
        {
            updateBlockState();
        }  
        else if ((index == currentItemSlot) && !isItemDecomposable(itemStacks.get(index)) )
        {
            currentItemSlot = NO_SLOT;
            binDecomposeTime = NO_DECOMPOSE_TIME;
            currentItemDecomposeTime = 0;
            updateBlockState();
        }
    }
    
    protected void checkParticles()
    {
    	// show some steam particles when composting
    	if (isDecomposing() && (binDecomposeTime % PARTICLE_INTERVAL == 0))
    	{
    		float ratio = getFilledRatio();
    		double x = (double)pos.getX() + 0.5D;
    		// try to align y source of particles to soil level in bin
    		double y = (double)pos.getY() + 0.5D + (0.5D * ratio);
    		double z = (double)pos.getZ() + 0.5D;
    		// more particles the more full the bin is
    		int num = 1 + Math.round(ratio); 
    		// pre-existing particle fx candidates: cloud, spit, poof (explode), townaura, snowballpoof, smoke, large_smoke, firework, falling_dust
        	((WorldServer)this.getWorld()).spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, x, y, z, num, 0.2D, 0.0D, 0.2D, 0.0D);
    	}
    }
    
    protected ItemStack getCompostItem(int count)
    {
    	TypeIdentifier cid = new TypeIdentifier(ModConfig.compostItem);
    	return cid.create(count);
    }
    
    // ----------------------------------------------------------------------
    // TileEntity

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        
        ItemStackHelper.loadAllItems(compound, this.itemStacks);

        binDecomposeTime = compound.getShort("DecompTime");
        currentItemSlot = compound.getByte("DecompSlot");
        itemDecomposeCount = compound.getByte("DecompCount");

        if (currentItemSlot >= 0)
        {
            currentItemDecomposeTime = getItemDecomposeTime(itemStacks.get(currentItemSlot));
        }
        else
        {
            currentItemDecomposeTime = 0;
        }

        if (compound.hasKey("CustomName", 8))
        {
            this.customName = compound.getString("CustomName");
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
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
    
    /**
    * This controls whether the tile entity gets replaced whenever the block state 
    * is changed. Normally only want this when block actually is replaced.
    */
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
    {
    	return (oldState.getBlock() != newState.getBlock());
    }
    
    /**
     * invalidates a tile entity
     */
    public void invalidate()
    {
        super.invalidate();
        this.updateContainingBlockInfo();
    }
    
    // ----------------------------------------------------------------------
    // IWorldNameable

    /**
     * Get the name of this object. For players this returns their username
     */
    @Override
    public String getName()
    {
        return this.hasCustomName() ? this.customName : getRealName();
    }

    /**
     * Returns true if this thing is named
     */
    public boolean hasCustomName()
    {
        return this.customName != null && !this.customName.isEmpty();
    }
    
    // ----------------------------------------------------------------------
    // ITickable
    
    public void update()
    {
        boolean isDecomposing = isDecomposing();

        boolean shouldUpdate = false;

        if (isDecomposing)
        {
            --binDecomposeTime;            
        }

        if (!this.getWorld().isRemote)
        {             
        	checkParticles();

            int decompCount = itemDecomposeCount;
            int filledSlotCount = getFilledSlots();

            if ( isDecomposing || (filledSlotCount > 0) )
            {
                if (binDecomposeTime <= 0)
                {
                    if (canCompost())
                    {
                        compostItem();
                        shouldUpdate = true;
                    }

                    currentItemSlot = selectRandomFilledSlot();
                    currentItemDecomposeTime = 0;

                    if ( (currentItemSlot >= 0) && (!hasOutputItems() || itemStacks.get(OUTPUT_SLOT).getCount() < STACK_LIMIT) )
                    {
                        currentItemDecomposeTime = getItemDecomposeTime(itemStacks.get(currentItemSlot));
                        binDecomposeTime = currentItemDecomposeTime;

                        if (binDecomposeTime > 0)
                        {
                            shouldUpdate = true;
                        }
                    }
                }
            }

            if (isDecomposing != isDecomposing() || (decompCount != itemDecomposeCount) )
            {
                shouldUpdate = true;
            }
        }

        if (shouldUpdate)
        {
        	updateBlockState();
        }
    }
    
    // ----------------------------------------------------------------------
    // IInventory
    
    /**
     * Returns the number of slots in the inventory.
     */
    @Override
    public int getSizeInventory()
    {
        return this.itemStacks.size();
    }
    
    @Override
    public boolean isItemValidForSlot (int slot, ItemStack item)
    {
        if ( (slot >= 0) && (slot < COMPOSTING_SLOTS) )
        {
            return isItemDecomposable(item);
        }

        return false;
    }
    
    /**
     * Returns the stack in the given slot.
     */
    @Override
    public ItemStack getStackInSlot(int index)
    {
        return (index >= 0) && (index < this.itemStacks.size()) ? (ItemStack)this.itemStacks.get(index) : ItemStack.EMPTY;
    }
    
    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    @Override
    public ItemStack decrStackSize(int index, int count)
    {
    	ItemStack stack = ItemStackHelper.getAndSplit(this.itemStacks, index, count);
    	updateInternalState(index);
        return stack;
    }

    /**
     * Removes a stack from the given slot and returns it.
     */
    @Override
    public ItemStack removeStackFromSlot(int index)
    {
    	ItemStack stack = ItemStackHelper.getAndRemove(this.itemStacks, index);
    	updateInternalState(index);    	
        return stack;
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
    	if (stack == null)
    	{
    		stack = ItemStack.EMPTY;
    	}
    	
        if (index >= 0 && index < this.itemStacks.size())
        {
            this.itemStacks.set(index, stack);
        }
    }

    /**
     * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
     */
    @Override
    public int getInventoryStackLimit()
    {
        return STACK_LIMIT;
    }

    /**
     * Don't rename this method to canInteractWith due to conflicts with Container
     */
    @Override
    public boolean isUsableByPlayer(EntityPlayer player)
    {
        if (this.world.getTileEntity(this.pos) != this)
        {
            return false;
        }
        else
        {
            return player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= USE_RANGE;
        }
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
    }

    @Override
    public int getField(int id)
    {
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {
    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    @Override
    public void clear()
    {
        this.itemStacks.clear();
    }
    
    // ----------------------------------------------------------------------
    // IItemHandler
  
    private net.minecraftforge.items.IItemHandler itemHandler;
    
    protected net.minecraftforge.items.IItemHandler createUnSidedHandler()
    {
        return new net.minecraftforge.items.wrapper.InvWrapper(this);
    }
  
    @SuppressWarnings("unchecked")
    @Override
    @javax.annotation.Nullable
    public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @javax.annotation.Nullable net.minecraft.util.EnumFacing facing)
    {
        if (capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return (T) (itemHandler == null ? (itemHandler = createUnSidedHandler()) : itemHandler);
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(net.minecraftforge.common.capabilities.Capability<?> capability, @javax.annotation.Nullable net.minecraft.util.EnumFacing facing)
    {
        return capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }
}
