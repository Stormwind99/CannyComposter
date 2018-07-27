package com.wumple.composter.bin;

import java.util.List;

import javax.annotation.Nonnull;

import com.wumple.composter.Reference;
import com.wumple.composter.config.ConfigHandler;
import com.wumple.composter.config.ModConfig;
import com.wumple.util.adapter.IThing;
import com.wumple.util.adapter.TileEntityThing;
import com.wumple.util.capability.tickingthing.TickingThingCap;
import com.wumple.util.misc.SUtil;
import com.wumple.util.misc.TypeIdentifier;
import com.wumple.util.misc.Util;

import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

public class CompostBinCap /*extends TileEntity*/ extends TickingThingCap<IThing> implements IInventory, ICompostBinCap
{
    // The {@link Capability} instance
    @CapabilityInject(ICompostBinCap.class)
    public static final Capability<ICompostBinCap> CAPABILITY = null;
    public static final EnumFacing DEFAULT_FACING = null;

    // IDs of the capability
    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "compost_bin");
    
    public static void register()
    {
        CapabilityManager.INSTANCE.register(ICompostBinCap.class, new CompostBinCapStorage(), () -> new CompostBinCap());
    }
    
    /// CompostBinCap
	public static final int COMPOSTING_SLOTS = 9;
	public static final int TOTAL_SLOTS = COMPOSTING_SLOTS + 1;
	public static final int OUTPUT_SLOT = TOTAL_SLOTS - 1; 
	public static final int STACK_LIMIT = 64;
	public static final int NO_SLOT = -1;
	public static final int NO_DECOMPOSE_TIME = -1;
	public static final double USE_RANGE = 64.0D;
	public static final int PARTICLE_INTERVAL = 640;
	//public static final int DECOMPOSE_TIME_MAX = 200;
	
	private NonNullList<ItemStack> itemStacks = NonNullList.<ItemStack>withSize(TOTAL_SLOTS, ItemStack.EMPTY);

    // The number of units the current item has been decomposing
    public int currentItemProgress = NO_DECOMPOSE_TIME;

    // The slot actively being decomposed
    private int currentItemSlot = NO_SLOT;

    // The number of units that a fresh copy of the currently-decomposing item would decompose for
    public int currentItemDecomposeTime = NO_DECOMPOSE_TIME;

    // number of units decomposed since last compost generation
    public int binDecomposeProgress = 0;
    
    // ticks existed during current session, for visuals
    long ticksExisted = 0;

    private String customName;
    
    // ----------------------------------------------------------------------
    // TileEntityCompostBin
    
    public int getCurrentItemProgress()
    {
        return currentItemProgress;
    }

    public int getCurrentItemDecompTime()
    {
        return currentItemDecomposeTime;
    }

    public boolean isDecomposing()
    {
        // currentItemSlot should also > NO_SLOT
        return currentItemDecomposeTime > NO_DECOMPOSE_TIME;
    }
    
    public boolean isActive()
    {
        return (currentItemSlot > NO_SLOT) || (binDecomposeProgress > NO_DECOMPOSE_TIME) ;
    }
    
    protected int getDecomposeUnitsNeeded()
    {
    	return ModConfig.binDecomposeUnitsNeeded;
    }
    
    @SideOnly(Side.CLIENT)
    public int getDecomposeTimeRemainingScaled(int scale)
    {
        double ratio = (double)(currentItemProgress + binDecomposeProgress) / ModConfig.binDecomposeUnitsNeeded;
        return (int)(ratio * scale);
    }
    
    /// ICompostBinCap
    
    private int lastDecompTime;
    private int lastItemDecompTime;
    private int lastDecompUnits;
    
    @Override
    public void detectAndSendChanges(Container containerIn, List<IContainerListener> listeners)
    {
        for (IContainerListener icontainerlistener : listeners)
        {
            if (lastDecompTime != currentItemProgress)
                icontainerlistener.sendWindowProperty(containerIn, 0, currentItemProgress);
            if (lastItemDecompTime != currentItemDecomposeTime)
                icontainerlistener.sendWindowProperty(containerIn, 1, currentItemDecomposeTime);
            if (lastDecompUnits != binDecomposeProgress)
                icontainerlistener.sendWindowProperty(containerIn, 2, binDecomposeProgress);
        }

        lastDecompTime = currentItemProgress;
        lastItemDecompTime = currentItemDecomposeTime;
        lastDecompUnits = binDecomposeProgress;
    }
    
    @Override
    public void updateProgressBar(int id, int value)
    {
        if (id == 0)
            currentItemProgress = value;
        if (id == 1)
            currentItemDecomposeTime = value;
        if (id == 2)
            binDecomposeProgress = value;        
    }
    
    /// ITickingThing
    
    @Override
    protected void cache()
    {
    }
    
    @Override
    protected long getEvaluationInterval()
    {
        return ModConfig.binEvaluateTicks;
    }
    
    /*
     * Evaluate fast things for a tick - visual effects
     */
    @Override
    public void always()
    {
        ticksExisted++;
        if (!getWorld().isRemote)
        {             
            checkParticles();
        }
    }
    
    /*
     * Evaluate slower things less frequently - ticks have passed
     */
    @Override
    public void doIt(long ticks)
    {
        boolean isDecomposing = isDecomposing();

        boolean shouldUpdate = false;

        if (isDecomposing)
        {
            currentItemProgress+=ticks;
        }

        if (!getWorld().isRemote)
        {             
            int decompCount = binDecomposeProgress;
            int filledSlotCount = getFilledSlots();
            
            if ( isDecomposing || (filledSlotCount > 0) )
            {
                // decompose current item if it is done
                if (currentItemProgress > currentItemDecomposeTime)
                {
                    if (canDecompose())
                    {
                        // will forget current item
                        decomposeItem();
                        shouldUpdate = true;
                    }
                }

                // try to select compostable item if not currently composting one
                if (currentItemSlot == NO_SLOT)
                {
                    // select item if non currently selected
                    if (!hasOutputItems() || (itemStacks.get(OUTPUT_SLOT).getCount() < STACK_LIMIT) )
                    {
                        currentItemSlot = selectRandomFilledSlot();
                        if (currentItemSlot > NO_SLOT)
                        {
                            currentItemDecomposeTime = getItemDecomposeTime(itemStacks.get(currentItemSlot));
                            // used to set currentItemProgress = 0, but now
                            // since ticks passed in we save any old ticks
                        }
                        else
                        {
                            // if no item to compost, we forget any extra units to avoid oddness or possible cheating
                            currentItemProgress = 0;
                        }
    
                        if (currentItemDecomposeTime > NO_DECOMPOSE_TIME)
                        {
                            shouldUpdate = true;
                        }
                    }
                }
            }

            if (isDecomposing != isDecomposing() || (decompCount != binDecomposeProgress) )
            {
                shouldUpdate = true;
            }
        }

        if (shouldUpdate)
        {
            updateBlockState();
        }
    }

    private boolean canDecompose()
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
        
        // if we assume output slot is empty or already has a valid compost output item,
        //   then we can avoid we evaluating compost item and allow random selection of compost item creation time
        //   if a multiple item oreDict name used
        // Used to:
        //   ItemStack newStack = getCompostItem(1); 
        //   return SUtil.canGrow(outputSlotStack, 1);
        
        return SUtil.canAddOrGrowCount(outputSlotStack, 1);
    }
    
    protected void forgetCurrentItem()
    {
        currentItemSlot = NO_SLOT;
        currentItemDecomposeTime = NO_DECOMPOSE_TIME;
        
        // use to currentItemProgress = NO_DECOMPOSE_TIME;
        // now we remember extra ticks to apply to next item
    }
    
    /*
     * Decompose current slot's item, and if threshold reached generate compost
     * 
     * Preconditions: 
     *   - current selected slot's item is decomposable
     *   - output slot is empty or has compost item in it
     */
    public void decomposeItem()
    {
    	int decomposeUnitsNeeded = getDecomposeUnitsNeeded();
    	
        binDecomposeProgress += currentItemDecomposeTime;

        if (binDecomposeProgress >= decomposeUnitsNeeded)
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

            binDecomposeProgress -= decomposeUnitsNeeded;
        }

        SUtil.shrink(itemStacks, currentItemSlot, 1);
        
        // remember any extra ticks for next item
        currentItemProgress -= currentItemDecomposeTime;

        // forget current slot so another can be selected
        forgetCurrentItem();
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

        int index = getWorld().rand.nextInt(filledSlotCount);
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
    
    protected World getWorld()
    {
        return owner.getWorld();
    }
    
    protected BlockPos getPos()
    {
        return owner.getPos();
    }
    
    @Override
    public void updateBlockState()
    {
    	World world = getWorld();
    	
    	if (!world.isRemote)
    	{
    	    setContentsLevel(world);
    	}
    	
        markDirty();
    }
    
    public static final int NUM_LEVELS = 3;
    public static final PropertyInteger LEVEL = PropertyInteger.create("level", 0, NUM_LEVELS);
    
    public void setContentsLevel(World worldIn)
    {
        BlockPos pos = this.getPos();
        
        IBlockState state = worldIn.getBlockState(pos);
        Block block = state.getBlock();
        float amount = getFilledRatio();
        
        float floatLevel = amount * (float) NUM_LEVELS;
        int level = Math.round(floatLevel);

        // make sure at least level 1 if anything is in the block
        if (floatLevel > 0.0F)
        {
            level = Math.max(1, level);
        }

        // safety - clamp within range
        int chunkedLevel = MathHelper.clamp(level, 0, NUM_LEVELS);

        worldIn.setBlockState(pos, state.withProperty(LEVEL, chunkedLevel), 2);
        worldIn.updateComparatorOutputLevel(pos, block);
    }

    protected void updateInternalState(int index)
    {
        if (index == OUTPUT_SLOT)
        {
            updateBlockState();
        }  
        else if ((index == currentItemSlot) && !isItemDecomposable(itemStacks.get(index)) )
        {
            forgetCurrentItem();
            updateBlockState();
        }
    }
    
    protected void checkParticles()
    {
    	// show some steam particles when composting
    	if (isDecomposing() && (ticksExisted % PARTICLE_INTERVAL == 0))
    	{
    		float ratio = getFilledRatio();
    		BlockPos pos = getPos();
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
    
    public NBTBase writeNBT(NBTTagCompound compound)
    {
        compound.setInteger("decompItemProgress", currentItemProgress);
        compound.setByte("decompBinSlot", (byte)currentItemSlot);
        compound.setInteger("decompBinProgress", binDecomposeProgress);

        ItemStackHelper.saveAllItems(compound, itemStacks);
 
        if (hasCustomName())
        {
            compound.setString("CustomName", customName);
        }      
        
        return compound;
    }
    
    public void readNBT(NBTTagCompound compound)
    {
        ItemStackHelper.loadAllItems(compound, itemStacks);

        currentItemProgress = compound.getInteger("decompItemProgress");
        currentItemSlot = compound.getByte("decompBinSlot");
        binDecomposeProgress = compound.getInteger("decompBinProgress");

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
            customName = compound.getString("CustomName");
        }  
    }
    
    // ----------------------------------------------------------------------
    // TileEntity
    
    public static class CompostBinCapStorage implements IStorage<ICompostBinCap>
    {
        @Override
        public NBTBase writeNBT(Capability<ICompostBinCap> capability, ICompostBinCap instance, EnumFacing side)
        {
            NBTTagCompound compound = new NBTTagCompound();

            if (instance != null)
            {
                instance.writeNBT(compound);
            }

            return compound;
        }

        @Override
        public void readNBT(Capability<ICompostBinCap> capability, ICompostBinCap instance, EnumFacing side, NBTBase nbt)
        {
            NBTTagCompound compound = (NBTTagCompound) nbt;

            if ((compound != null) && (instance != null))
            {
                instance.readNBT(compound);
            }
        }
    }

    /**
    * This controls whether the tile entity gets replaced whenever the block state 
    * is changed. Normally only want this when block actually is replaced.
    */
    /*
    // TODO Must be in TileEntity
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
    {
    	return (oldState.getBlock() != newState.getBlock());
    }
    */
    
    /**
     * invalidates a tile entity
     */
    public void invalidate()
    {
        owner.invalidate();
        TileEntityThing tet = Util.as(owner, TileEntityThing.class);
        if (tet != null) { tet.owner.updateContainingBlockInfo(); }
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
    
    @Override
    public ITextComponent getDisplayName()
    {
        return new TextComponentString(getName());
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
        BlockPos pos = getPos();
        
        /*
        if (! owner.sameAs( TileEntityThing(getWorld().getTileEntity(pos)) ) )
        {
            return false;
        }
        else
        */
        {
            return player.getDistanceSq((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D) <= USE_RANGE;
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
    
    @Override
    public void markDirty()
    {
        owner.markDirty();
    }
    
    // ----------------------------------------------------------------------
    // IItemHandler
  
    private IItemHandlerModifiable itemHandler;
    
    protected IItemHandlerModifiable handler()
    {
        if (itemHandler == null)
        {
            itemHandler = new InvWrapper(this);
        }
        
        return itemHandler;
    }
  
    @Override
    public int getSlots()
    {
        return handler().getSlots();
    }
    
    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
    {
        return handler().insertItem(slot, stack, simulate);
    }
        
    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        return handler().extractItem(slot, amount, simulate);
    }
    
    @Override
    public int getSlotLimit(int slot)
    {
        return handler().getSlotLimit(slot);
    }
    
    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack)
    {
        handler().setStackInSlot(slot, stack);
    }
}
