package com.wumple.composter;

import com.wumple.util.SUtil;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucketMilk;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
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
	static final int COMPOSTING_SLOTS = 9;
	static final int TOTAL_SLOTS = COMPOSTING_SLOTS + 1;
	static final int OUTPUT_SLOT = TOTAL_SLOTS - 1; 
	static final int STACK_LIMIT = 64;
	static final int NO_SLOT = -1;
	static final int NO_DECOMPOSE_TIME = -1;
	static final double USE_RANGE = 64.0D;
	static final int PARTICLE_INTERVAL = 64;
	static final int DECOMPOST_COUNT_MAX = 8;
	static final int DECOMPOSE_TIME_MAX = 200;
	
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

    @SideOnly(Side.CLIENT)
    public int getDecomposeTimeRemainingScaled(int scale)
    {
        if (currentItemDecomposeTime == 0)
        {
            currentItemDecomposeTime = DECOMPOSE_TIME_MAX;
        }

        //return binDecomposeTime * scale / currentItemDecomposeTime;
        return (DECOMPOST_COUNT_MAX - itemDecomposeCount) * scale / COMPOSTING_SLOTS + (binDecomposeTime * scale / (currentItemDecomposeTime * COMPOSTING_SLOTS));
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
        
        if ( (outputSlotStack.getItem() != ObjectHolder.compost) && !outputSlotStack.isEmpty() )
        {
        	return false;
        }

        int result = outputSlotStack.getCount() + 1;
        
        return (result <= getInventoryStackLimit()) && (result <= outputSlotStack.getMaxStackSize());
    }

    public void compostItem()
    {
        if (canCompost())
        {
            if (itemDecomposeCount < DECOMPOST_COUNT_MAX)
            {
                itemDecomposeCount++;
            }

            if (itemDecomposeCount >= DECOMPOST_COUNT_MAX)
            {
                
                if (!hasOutputItems())
                {
                	ItemStack resultStack = new ItemStack(ObjectHolder.compost);
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

        /*
        // TODO make non-food compost, and different compost rates
        ICompostMaterial material = GardenAPI.instance().registries().compost().getCompostMaterialInfo(itemStack);
        if (material == null)
            return 0;

        return material.getDecomposeTime();
        */
        
        Item item = itemStack.getItem();
        
        if (item instanceof ItemFood) 
        {
        	return 125;
        }
        
        if (item instanceof ItemEgg)
        {
        	return 100;
        }
        
        if (item instanceof ItemBucketMilk)
        {
        	return 200;
        }
        
        if (item instanceof ItemSeeds)
        {
        	return 125;
        }
        
        if (item instanceof ItemPotion)
        {
        	return 200;
        }
        
        if (item instanceof ItemTool)
        {
        	ItemTool tool = (ItemTool)item;
        	if (tool.getToolMaterialName().equalsIgnoreCase("WOOD"))
        	{
        		// MAYBE vary amount on item damage, and damage item as it composts
        		return 300;
        	}
        }
        
        if (item == Items.ROTTEN_FLESH)
        {
        	return 100;
        }

        /*
        // foodfunk:spoiled_milk
        // foodfunk:rotten_food
        // foodfunk:rotten_item
        // MAYBE more items
        // ItemArmor material leather
        // ItemArrow
        // ItemBanner
        // ItemBed
        // ItemBoat
        // ItemBook
        // ItemCarrotOnAStick
        // ItemCloth
        // ItemCoal
        // ItemDoor if wood (how?)
        // ItemDye
        // MAPBASE: ItemEmptyMap
        // ItemFishingRod
        // ItemHoe if wood
        // ItemKnowledgeBook
        // ItemLead
        // ItemLilyPad
        // ItemMapBase
        // MAPBASE: ItemMap
        // ItemSaddle
        // ItemSeedFood (?)
        // ItemSign
        // ItemSkull
        // ItemSlab if wood
        // ItemSnow
        // ItemSnowball
        // ItemSoup
        // ItemSword if wood
        // TOOL: ItemAxe if wood
        // TOOL: ItemSpade if wood
        // TOOL: ItemPickaxe if wood
        // ItemWritableBook
        // ItemWrittenBook
        // ItemEnchantedBook
        // ItemBow
        // ItemHoe if wood

        // STICK
        // Stairs if wood
        // Plate if wood
        // Gear if wood
        
        public static final Item APPLE;
        public static final ItemBow BOW;
        public static final Item ARROW;
        public static final Item COAL;
        public static final Item WOODEN_SWORD;
        public static final Item WOODEN_SHOVEL;
        public static final Item WOODEN_PICKAXE;
        public static final Item WOODEN_AXE;
        public static final Item STICK;
        public static final Item BOWL;
        public static final Item MUSHROOM_STEW;
        public static final Item STRING;
        public static final Item FEATHER;
        public static final Item GUNPOWDER;
        public static final Item WOODEN_HOE;
        public static final Item WHEAT_SEEDS;
        public static final Item WHEAT;
        public static final Item BREAD;
        public static final ItemArmor LEATHER_HELMET;
        public static final ItemArmor LEATHER_CHESTPLATE;
        public static final ItemArmor LEATHER_LEGGINGS;
        public static final ItemArmor LEATHER_BOOTS;
        public static final Item PORKCHOP;
        public static final Item COOKED_PORKCHOP;
        public static final Item PAINTING;
        public static final Item GOLDEN_APPLE;
        public static final Item SIGN;
        public static final Item OAK_DOOR;
        public static final Item SPRUCE_DOOR;
        public static final Item BIRCH_DOOR;
        public static final Item JUNGLE_DOOR;
        public static final Item ACACIA_DOOR;
        public static final Item DARK_OAK_DOOR;
        public static final Item WATER_BUCKET;
        public static final Item SADDLE;
        public static final Item REDSTONE;
        public static final Item SNOWBALL;
        public static final Item BOAT;
        public static final Item SPRUCE_BOAT;
        public static final Item BIRCH_BOAT;
        public static final Item JUNGLE_BOAT;
        public static final Item ACACIA_BOAT;
        public static final Item DARK_OAK_BOAT;
        public static final Item LEATHER;
        public static final Item MILK_BUCKET;
        public static final Item CLAY_BALL;
        public static final Item REEDS;
        public static final Item PAPER;
        public static final Item BOOK;
        public static final Item SLIME_BALL;
        public static final Item CHEST_MINECART;
        public static final Item EGG;
        public static final ItemFishingRod FISHING_ROD;
        public static final Item GLOWSTONE_DUST;
        public static final Item FISH;
        public static final Item COOKED_FISH;
        public static final Item DYE;
        public static final Item BONE;
        public static final Item SUGAR;
        public static final Item CAKE;
        public static final Item BED;
        public static final Item COOKIE;
        public static final ItemMap FILLED_MAP;
        public static final Item MELON;
        public static final Item PUMPKIN_SEEDS;
        public static final Item MELON_SEEDS;
        public static final Item BEEF;
        public static final Item COOKED_BEEF;
        public static final Item CHICKEN;
        public static final Item COOKED_CHICKEN;
        public static final Item MUTTON;
        public static final Item COOKED_MUTTON;
        public static final Item RABBIT;
        public static final Item COOKED_RABBIT;
        public static final Item RABBIT_STEW;
        public static final Item RABBIT_FOOT;
        public static final Item RABBIT_HIDE;
        public static final Item ROTTEN_FLESH;
        public static final Item GHAST_TEAR;
        public static final Item NETHER_WART;
        public static final ItemPotion POTIONITEM;
        public static final ItemPotion SPLASH_POTION;
        public static final ItemPotion LINGERING_POTION;
        public static final Item GLASS_BOTTLE;
        public static final Item DRAGON_BREATH;
        public static final Item SPIDER_EYE;
        public static final Item FERMENTED_SPIDER_EYE;
        public static final Item BLAZE_POWDER;
        public static final Item MAGMA_CREAM;
        public static final Item SPECKLED_MELON;
        public static final Item SPAWN_EGG;
        public static final Item WRITABLE_BOOK;
        public static final Item WRITTEN_BOOK;
        public static final Item ITEM_FRAME;
        public static final Item CARROT;
        public static final Item POTATO;
        public static final Item BAKED_POTATO;
        public static final Item POISONOUS_POTATO;
        public static final ItemEmptyMap MAP;
        public static final Item GOLDEN_CARROT;
        public static final Item SKULL;
        public static final Item CARROT_ON_A_STICK;
        public static final Item PUMPKIN_PIE;
        public static final Item FIREWORKS;
        public static final Item FIREWORK_CHARGE;
        public static final Item ENCHANTED_BOOK;
        public static final ItemArmorStand ARMOR_STAND;
        public static final Item LEAD;
        public static final Item BANNER;
        public static final Item SHIELD;
        public static final Item CHORUS_FRUIT;
        public static final Item CHORUS_FRUIT_POPPED;
        public static final Item BEETROOT_SEEDS;
        public static final Item BEETROOT;
        public static final Item BEETROOT_SOUP;
        public static final Item SHULKER_SHELL;
        public static final Item KNOWLEDGE_BOOK;
        
                    registerOre("logWood",     new ItemStack(Blocks.LOG, 1, WILDCARD_VALUE));
            registerOre("logWood",     new ItemStack(Blocks.LOG2, 1, WILDCARD_VALUE));
            registerOre("plankWood",   new ItemStack(Blocks.PLANKS, 1, WILDCARD_VALUE));
            registerOre("slabWood",    new ItemStack(Blocks.WOODEN_SLAB, 1, WILDCARD_VALUE));
            registerOre("stairWood",   Blocks.OAK_STAIRS);
            registerOre("stairWood",   Blocks.SPRUCE_STAIRS);
            registerOre("stairWood",   Blocks.BIRCH_STAIRS);
            registerOre("stairWood",   Blocks.JUNGLE_STAIRS);
            registerOre("stairWood",   Blocks.ACACIA_STAIRS);
            registerOre("stairWood",   Blocks.DARK_OAK_STAIRS);
            registerOre("fenceWood", Blocks.OAK_FENCE);
            registerOre("fenceWood", Blocks.SPRUCE_FENCE);
            registerOre("fenceWood", Blocks.BIRCH_FENCE);
            registerOre("fenceWood", Blocks.JUNGLE_FENCE);
            registerOre("fenceWood", Blocks.DARK_OAK_FENCE);
            registerOre("fenceWood", Blocks.ACACIA_FENCE);
            registerOre("fenceGateWood", Blocks.OAK_FENCE_GATE);
            registerOre("fenceGateWood", Blocks.SPRUCE_FENCE_GATE);
            registerOre("fenceGateWood", Blocks.BIRCH_FENCE_GATE);
            registerOre("fenceGateWood", Blocks.JUNGLE_FENCE_GATE);
            registerOre("fenceGateWood", Blocks.DARK_OAK_FENCE_GATE);
            registerOre("fenceGateWood", Blocks.ACACIA_FENCE_GATE);
            registerOre("stickWood",   Items.STICK);
            registerOre("treeSapling", new ItemStack(Blocks.SAPLING, 1, WILDCARD_VALUE));
            registerOre("treeLeaves",  new ItemStack(Blocks.LEAVES, 1, WILDCARD_VALUE));
            registerOre("treeLeaves",  new ItemStack(Blocks.LEAVES2, 1, WILDCARD_VALUE));
            registerOre("vine",        Blocks.VINE);
            
                        // crops
            registerOre("cropWheat",   Items.WHEAT);
            registerOre("cropPotato",  Items.POTATO);
            registerOre("cropCarrot",  Items.CARROT);
            registerOre("cropNetherWart", Items.NETHER_WART);
            registerOre("sugarcane",   Items.REEDS);
            registerOre("blockCactus", Blocks.CACTUS);

            // misc materials
            registerOre("dye",         new ItemStack(Items.DYE, 1, WILDCARD_VALUE));
            registerOre("paper",       new ItemStack(Items.PAPER));

            // mob drops
            registerOre("slimeball",   Items.SLIME_BALL);
            
            registerOre("bone",        Items.BONE);
            registerOre("gunpowder",   Items.GUNPOWDER);
            registerOre("string",      Items.STRING);
            registerOre("leather",     Items.LEATHER);
            registerOre("feather",     Items.FEATHER);
            registerOre("egg",         Items.EGG);
            
                 registerOre("grass",       Blocks.GRASS);
                 
                    registerOre("torch",       Blocks.TORCH);
            registerOre("workbench",   Blocks.CRAFTING_TABLE);
            registerOre("blockSlime",    Blocks.SLIME_BLOCK);
                        registerOre("chestWood",    Blocks.CHEST);
         */
        
        return NO_DECOMPOSE_TIME;
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
