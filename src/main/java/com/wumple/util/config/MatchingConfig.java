package com.wumple.util.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.wumple.util.Util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

/*
 * Wrapper around Forge HashMap<String, ?> configs for itemstack->value configs
 */
public class MatchingConfig<T>
{
	protected final Map<String, T> config;
	public final T FALSE_VALUE;
    public static final String FOOD_TAG = "minecraft:food";

	public MatchingConfig(Map<String, T> configIn, T falseValueIn)
	{
		config = configIn;
		FALSE_VALUE = falseValueIn;
	}
	
	public void init()
	{
		// add all defaults, then
		// ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
	}
	
	// ----------------------------------------------------------------------
	// Add default properties to config
	
	// --- add by String
	
    public boolean addDefaultProperty(String name, T amountIn)
    {
        if (name == null)
        {
            name = "";
        }

        config.putIfAbsent(name, amountIn);

        return true;
    }
    
    public boolean addDefaultProperty(String[] items, T amountIn)
    {
    	boolean success = true;
    	
        for (String item : items)
        {
            success &= addDefaultProperty(item, amountIn);
        }
        
        return success;
    }
    
    // --- add by Item
    
    public boolean addDefaultProperty(Item item, T amount)
    {
        // check for null Item in case another mod removes a vanilla item
        if (item != null)
        {
            ResourceLocation resLoc = Item.REGISTRY.getNameForObject(item);
            if (resLoc != null)
            {
                String name = resLoc.toString();
                return addDefaultProperty(name, amount);
            }
        }

        return false;
    }

    public boolean addDefaultProperty(Item item, String backup, T amount)
    {
        String name = backup;

        // check for null Item in case another mod removes a vanilla item
        if (item != null)
        {
            ResourceLocation resLoc = Item.REGISTRY.getNameForObject(item);
            if (resLoc != null)
            {
                name = resLoc.toString();
            }
        }

        return addDefaultProperty(name, amount);
    }
    
    // ----------------------------------------------------------------------
    // getPropertyBase - for internal use

    protected T getPropertyBase(String key)
    {
        T amount = null;
        
        if ((key != null) && config.containsKey(key))
        {
            amount = config.get(key);
        }

        return amount;
    }
    
    @Nullable
    protected T getPropertyBase(List<String> keys)
    {
        T amount = null;

        for (String key : keys)
        {
            amount = getPropertyBase(key);
            if (amount != null)
            {
                break;
            }
        }

        return amount;
    }
    
    // ----------------------------------------------------------------------
    // Get value for different types

    /**
     * Get the highest priority value we match for stack
     * Checks all keys for stack - expands to multiple keys in defined order: id@meta, id, minecraft:food
     * @return highest priority value for stack, or null if key not found (not FALSE_VALUE)
     */
    @Nullable
    public T getProperty(ItemStack itemStack)
    {
        if (itemStack == null)
        {
            return null;
        }

        ArrayList<String> nameKeys = new ArrayList<String>();

        Item item = itemStack.getItem();

        String key2 = Item.REGISTRY.getNameForObject(item).toString();

        nameKeys.add(key2 + "@" + itemStack.getMetadata());
        nameKeys.add(key2);

        if (!itemStack.isEmpty())
        {
            int oreIds[] = OreDictionary.getOreIDs(itemStack);
            for (int oreId : oreIds)
            {
                nameKeys.add(OreDictionary.getOreName(oreId));
            }
        }

        if (item instanceof ItemFood)
        {
            nameKeys.add(FOOD_TAG);
        }

        return getPropertyBase(nameKeys);
    }
    
    public T getProperty(Entity entity)
    {
    	String name = (entity == null) ? null : EntityList.getEntityString(entity);
    	return getPropertyBase(name);
    }
    
    public T getProperty(ResourceLocation loc)
    {
	   String key = (loc == null) ? null : loc.toString();
       return getPropertyBase(key);	
    }
    
    public T getProperty(TileEntity it)
    {
        ResourceLocation loc = (it == null) ? null : TileEntity.getKey(it.getClass());
        return getProperty(loc);
    }
    
    // ----------------------------------------------------------------------
    // get value for different types
    
    /*
     * Get the highest priority value we match for stack
     * Checks all keys for stack - expands to multiple keys in defined order: id@meta, id, minecraft:food
     * @return highest priority value for stack, or FALSE_VALUE if key not found
     */
    public T getValue(ItemStack stack)
    {
    	return Util.getValueOrDefault(getProperty(stack), FALSE_VALUE);
    }

    public T getValue(Entity entity)
    {
    	return Util.getValueOrDefault(getProperty(entity), FALSE_VALUE);
    }

    public T getValue(TileEntity entity)
    {
    	return Util.getValueOrDefault(getProperty(entity), FALSE_VALUE);
    }
    
    public T getValue(ResourceLocation loc)
    {
    	return Util.getValueOrDefault(getProperty(loc), FALSE_VALUE);
    }
    
    // ----------------------------------------------------------------------
    // check for non-FALSE_VALUE for different types
   
    /**
     * Does stack not match FALSE_VALUE?  
     * aka does stack have no entry or the default value as the entry?
     * @returns true if stack doesn't match FALSE_VALUE, false if it does
     */
    public boolean doesIt(ItemStack stack)
    {
        return getProperty(stack) != FALSE_VALUE;
    } 
    
    public boolean doesIt(Entity entity)
    {
        return getProperty(entity) != FALSE_VALUE;
    } 

    public boolean doesIt(TileEntity entity)
    {
        return getProperty(entity) != FALSE_VALUE;
    } 

    public boolean doesIt(ResourceLocation loc)
    {
        return getProperty(loc) != FALSE_VALUE;
    }
    
    // ----------------------------------------------------------------------
    // TODO
    
    public static class Identifier
    {
        public String id = null;
        public Integer meta = null;

        Identifier()
        {
        }

        Identifier(String idIn)
        {
            setID(idIn);
        }
        
        Identifier(String idIn, Integer metaIn)
        {
            setID(idIn);
            meta = metaIn;
        }

        public void setID(String key)
        {
            // metadata support - class:name@metadata
            int length = (key != null) ? key.length() : 0;
            if ((length >= 2) && (key.charAt(length - 2) == '@'))
            {
                String metastring = key.substring(length - 1);
                meta = Integer.valueOf(metastring);
                id = key.substring(0, length - 2);
            }
            else if ((length >= 3) && (key.charAt(length - 3) == '@'))
            {
                String metastring = key.substring(length - 2);
                meta = Integer.valueOf(metastring);
                id = key.substring(0, length - 3);
            }
            else
            {
                id = key;
                meta = null;
            }
        }
    }
}