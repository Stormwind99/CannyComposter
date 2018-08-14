package com.wumple.composter.capability;

import java.util.List;

import com.wumple.composter.Composter;
import com.wumple.composter.Reference;
import com.wumple.composter.capability.container.ComposterGuiHandler;
import com.wumple.composter.capability.container.ContainerCompostBin;
import com.wumple.composter.config.ConfigHandler;
import com.wumple.composter.config.ModConfig;
import com.wumple.util.adapter.IThing;
import com.wumple.util.base.misc.Util;
import com.wumple.util.capability.tickingthing.TickingThingCap;
import com.wumple.util.misc.SUtil;
import com.wumple.util.misc.TypeIdentifier;

import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

public class CompostBinCap extends TickingThingCap<IThing> implements ICompostBinCap, IInteractionObject
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
    public static final int NO_COMPOSTING = 0;
    public static final int DEFAULT_SPEED = 100;

    public static final int COMPOSTING_SLOTS = 9;
    public static final int TOTAL_SLOTS = COMPOSTING_SLOTS + 1;
    public static final int OUTPUT_SLOT = TOTAL_SLOTS - 1;
    public static final int STACK_LIMIT = 64;
    public static final int NO_SLOT = -1;
    public static final int NO_DECOMPOSE_TIME = -1;
    public static final double USE_RANGE = 64.0D;
    public static final int PARTICLE_INTERVAL = 640;

    private NonNullList<ItemStack> itemStacks = NonNullList.<ItemStack> withSize(TOTAL_SLOTS, ItemStack.EMPTY);

    // The number of units the current item has been decomposing
    public int currentItemProgress = NO_DECOMPOSE_TIME;

    // The slot actively being decomposed
    private int currentItemSlot = NO_SLOT;

    // The number of units that a fresh copy of the currently-decomposing item would decompose for
    public int currentItemDecomposeTime = NO_DECOMPOSE_TIME;

    // number of units decomposed since last compost generation
    public int binDecomposeProgress = 0;

    // ticks existed during current session, for visuals
    public long ticksExisted = 0;

    // composting speed modifier
    public int compostingSpeed = DEFAULT_SPEED;

    // ----------------------------------------------------------------------
    // CompostBinCap

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
        return (currentItemDecomposeTime > NO_DECOMPOSE_TIME) && (currentItemSlot > NO_SLOT);
    }

    protected int getDecomposeUnitsNeeded()
    {
        return ModConfig.binDecomposeUnitsNeeded;
    }

    private boolean canDecompose()
    {
        if ((currentItemSlot == NO_SLOT) || !isItemDecomposable(itemStacks.get(currentItemSlot)))
        {
            return false;
        }

        if (!hasOutputItems())
        {
            return true;
        }

        ItemStack outputSlotStack = itemStacks.get(OUTPUT_SLOT);

        // if we assume output slot is empty or already has a valid compost output item,
        // then we can avoid we evaluating compost item and allow random selection of compost item creation time
        // if a multiple item oreDict name used
        // Used to:
        // ItemStack newStack = getCompostItem(1);
        // return SUtil.canGrow(outputSlotStack, 1);

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
     * Preconditions: - current selected slot's item is decomposable - output slot is empty or has compost item in it
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
            // MAYBE assume all itemstacks that made it into compost bin are compostable
            // reduces expense by eliminating config lookup on itemstack
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

    public int getOutputItemCount()
    {
        ItemStack output = itemStacks.get(OUTPUT_SLOT);
        return (output != null) ? output.getCount() : 0;
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

    public float getFilledRatio()
    {
        int slots = getFilledSlots() + (hasOutputItems() ? 1 : 0);
        return (float) slots / (float) TOTAL_SLOTS;
    }

    protected World getWorld()
    {
        return owner.getWorld();
    }

    protected BlockPos getPos()
    {
        return owner.getPos();
    }

    protected void updateInternalState(int index)
    {
        if (index == OUTPUT_SLOT)
        {
            updateBlockState();
        }
        else if ((index == currentItemSlot) && !isItemDecomposable(itemStacks.get(index)))
        {
            forgetCurrentItem();
            updateBlockState();
        }
    }

    protected ItemStack getCompostItem(int count)
    {
        TypeIdentifier cid = new TypeIdentifier(ModConfig.compostItem);
        return cid.create(count);
    }

    // ----------------------------------------------------------------------
    /// For Block appearance

    public static final int NUM_LEVELS = 3;
    public static final PropertyInteger LEVEL = PropertyInteger.create("level", 0, NUM_LEVELS);

    public void setContentsLevel(World worldIn)
    {
        BlockPos pos = this.getPos();

        IBlockState state = worldIn.getBlockState(pos);
        Block block = state.getBlock();

        // set level blockstateproperty only if block has it
        if (state.getProperties().containsKey(LEVEL))
        {
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
        }
        worldIn.updateComparatorOutputLevel(pos, block);
    }

    protected void checkParticles()
    {
        // show some steam particles when composting
        if (isDecomposing() && (ticksExisted % PARTICLE_INTERVAL == 0))
        {
            float ratio = getFilledRatio();
            BlockPos pos = getPos();
            double x = (double) pos.getX() + 0.5D;
            // try to align y source of particles to soil level in bin
            double y = (double) pos.getY() + 0.5D + (0.5D * ratio);
            double z = (double) pos.getZ() + 0.5D;
            // more particles the more full the bin is
            int num = 1 + Math.round(ratio);
            // pre-existing particle fx candidates: cloud, spit, poof (explode), townaura, snowballpoof, smoke, large_smoke, firework, falling_dust
            ((WorldServer) this.getWorld()).spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, x, y, z, num, 0.2D, 0.0D, 0.2D, 0.0D);
        }
    }

    // ----------------------------------------------------------------------
    /// ICompostBinCap

    @Override
    public int getDecomposeTimeRemainingScaled(int scale)
    {
        double ratio = (double) (currentItemProgress + binDecomposeProgress) / ModConfig.binDecomposeUnitsNeeded;
        return (int) (ratio * scale);
    }

    @Override
    public boolean isActive()
    {
        return (currentItemSlot > NO_SLOT) || (binDecomposeProgress > NO_DECOMPOSE_TIME);
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

    @Override
    public NBTBase serializeNBT()
    {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setInteger("decompItemProgress", currentItemProgress);
        compound.setByte("decompBinSlot", (byte) currentItemSlot);
        compound.setInteger("decompBinProgress", binDecomposeProgress);

        ItemStackHelper.saveAllItems(compound, itemStacks);

        return compound;
    }

    @Override
    public void deserializeNBT(NBTBase nbt)
    {
        NBTTagCompound compound = Util.as(nbt, NBTTagCompound.class);

        if (compound == null)
        {
            return;
        }

        ItemStackHelper.loadAllItems(compound, itemStacks);

        currentItemProgress = compound.getInteger("decompItemProgress");
        currentItemSlot = compound.getByte("decompBinSlot");
        binDecomposeProgress = compound.getInteger("decompBinProgress");
        currentItemDecomposeTime = (currentItemSlot >= 0) ? getItemDecomposeTime(itemStacks.get(currentItemSlot)) : 0;
        
        // MAYBE sanity check currentItemSlot
    }

    // ----------------------------------------------------------------------
    /// Event handling via CompostBinHandler

    @Override
    public void onBlockBreak(World worldIn, BlockPos pos)
    {
        InventoryHelper.dropInventoryItems(worldIn, pos, this);
        worldIn.updateComparatorOutputLevel(pos, worldIn.getBlockState(pos).getBlock());
    }

    @Override
    public void onRightBlockClicked(PlayerInteractEvent.RightClickBlock event)
    {
        World worldIn = event.getWorld();
        BlockPos pos = event.getPos();
        EntityPlayer playerIn = event.getEntityPlayer();

        if (worldIn.getBlockState(pos.up()).doesSideBlockChestOpening(worldIn, pos.up(), EnumFacing.DOWN))
        {
            event.setCancellationResult(EnumActionResult.FAIL);
            event.setCanceled(true);
        }
        else if (worldIn.isRemote)
        {
            event.setCanceled(true);
        }
        else
        {
            // custom display names are updated on client via CustomNamedTileEntity.getUpdateTag() and friends
            playerIn.openGui(Composter.instance, ComposterGuiHandler.compostBinGuiID, worldIn, pos.getX(), pos.getY(), pos.getZ());
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.SUCCESS);
        }
    }

    // ----------------------------------------------------------------------
    /// Container via ICompostBinCap

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

    // ----------------------------------------------------------------------
    /// ITickingThing

    @Override
    protected void cache()
    {
        Integer ratio = null;

        TileEntity entity = owner.as(TileEntity.class);
        ratio = (entity != null) ? ConfigHandler.composters.getProperty(entity) : null;

        if (ratio == null)
        {
            Entity entity2 = owner.as(Entity.class);
            ratio = (entity2 != null) ? ConfigHandler.composters.getProperty(entity2) : null;
        }

        // at this point ratio should not be null - probably a bug, maybe throw exception
        compostingSpeed = (ratio != null) ? ratio.intValue() : NO_COMPOSTING;
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

        if (isDecomposing && (compostingSpeed != 0))
        {
            long modTicks = Math.max(1, (ticks * compostingSpeed) / DEFAULT_SPEED);
            currentItemProgress += modTicks;
        }

        if (!getWorld().isRemote)
        {
            int decompCount = binDecomposeProgress;
            int filledSlotCount = getFilledSlots();

            if (isDecomposing || (filledSlotCount > 0))
            {
                // reset if item removed
                if ((currentItemSlot != NO_SLOT) && (itemStacks.get(currentItemSlot).getCount() <= 0))
                {
                    forgetCurrentItem();
                    shouldUpdate = true;   
                }
                
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
                    if (!hasOutputItems() || (itemStacks.get(OUTPUT_SLOT).getCount() < STACK_LIMIT))
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

            if (isDecomposing != isDecomposing() || (decompCount != binDecomposeProgress))
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
    // TileEntity

    /**
     * invalidates a tile entity
     */
    public void invalidate()
    {
        TileEntity te = owner.as(TileEntity.class);
        if (te != null)
        {
            te.updateContainingBlockInfo();
        }
        owner.invalidate();
    }

    // ----------------------------------------------------------------------
    // IWorldNameable

    /**
     * Get the name of this object. For players this returns their username
     */
    @Override
    public String getName()
    {
        IWorldNameable i = owner.as(IWorldNameable.class);
        return (i != null) ? i.getName() : "";
    }

    /**
     * Returns true if this thing is named
     */
    public boolean hasCustomName()
    {
        IWorldNameable i = owner.as(IWorldNameable.class);
        return (i != null) ? i.hasCustomName() : false;
    }

    @Override
    public ITextComponent getDisplayName()
    {
        IWorldNameable i = owner.as(IWorldNameable.class);
        return (i != null) ? i.getDisplayName() : null;
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
    public boolean isItemValidForSlot(int slot, ItemStack item)
    {
        if ((slot >= 0) && (slot < COMPOSTING_SLOTS))
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
        return (index >= 0) && (index < this.itemStacks.size()) ? (ItemStack) this.itemStacks.get(index) : ItemStack.EMPTY;
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

        return player.getDistanceSq((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D) <= USE_RANGE;
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

    // this avoids a lot of boilerplate code, at expense of another object and indirection
    protected IItemHandlerModifiable itemHandler;

    @Override
    public IItemHandlerModifiable handler()
    {
        if (itemHandler == null)
        {
            itemHandler = new InvWrapper(this);
        }

        return itemHandler;
    }

    // ----------------------------------------------------------------------
    /// IInteractionObject

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
    {
        return new ContainerCompostBin(playerInventory, this);
    }

    public String getGuiID()
    {
        return Integer.toString(ComposterGuiHandler.compostBinGuiID);
    }
    
    // ----------------------------------------------------------------------
    /// ITooltipProvider

    @Override
    public void doTooltip(ItemStack stack, EntityPlayer entity, boolean advanced, List<String> tips)
    {
        String key = "misc.composter.tooltip.composting.inactive"; // Inactive
        
        if (isDecomposing())
        {
            key = "misc.composter.tooltip.composting.active"; // Active
        }
        
        tips.add(new TextComponentTranslation(key, getOutputItemCount()).getUnformattedText());
        
        if (advanced)
        {
            tips.add(new TextComponentTranslation("misc.composter.tooltip.advanced.composting.item", currentItemSlot, currentItemProgress, currentItemDecomposeTime).getUnformattedText());
            tips.add(new TextComponentTranslation("misc.composter.tooltip.advanced.composting.output", binDecomposeProgress, getDecomposeUnitsNeeded(), compostingSpeed).getUnformattedText());
        }
    }
}
