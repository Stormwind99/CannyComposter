package com.wumple.util.config;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.wumple.util.base.misc.Util;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

//TODO import com.wumple.util.adapter.IThing;

/*
 * Wrapper around Forge HashMap<String, T> configs for (itemstack, item, entity, string)->value configs
 */
public class MatchingConfig<T> extends NameKeys
{
    protected final Map<String, T> map;
    public final T FALSE_VALUE;

    public MatchingConfig(Map<String, T> configIn, T falseValueIn)
    {
        map = configIn;
        FALSE_VALUE = falseValueIn;
    }

    /*
     * If using this class and including default config properties, still need to
     * save config with them - looks easier to add defaults to config then re-scan
     * config into map afterward
     */

    // ----------------------------------------------------------------------
    // Add default properties to config

    // --- add by String

    public boolean addDefaultProperty(String name, T amountIn)
    {
        if (name == null)
        {
            name = "";
        }

        map.putIfAbsent(name, amountIn);

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
            ResourceLocation resLoc = item.getRegistryName();
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
            ResourceLocation resLoc = item.getRegistryName();
            if (resLoc != null)
            {
                name = resLoc.toString();
            }
        }

        return addDefaultProperty(name, amount);
    }

    // ----------------------------------------------------------------------
    // Get value for different types

    protected T getProperty(String key)
    {
        T amount = null;

        if ((key != null) && map.containsKey(key))
        {
            amount = map.get(key);
        }

        return amount;
    }

    @Nullable
    protected T getProperty(List<String> keys)
    {
        T amount = null;

        for (String key : keys)
        {
            amount = getProperty(key);
            if (amount != null)
            {
                break;
            }
        }

        return amount;
    }

    /**
     * Get the highest priority value we match for stack Checks all keys for stack - expands to multiple keys in defined order: id@meta, id, minecraft:food
     * 
     * @return highest priority value for stack, or null if key not found (not FALSE_VALUE)
     */
    @Nullable
    public T getProperty(ItemStack itemStack)
    {
        return getProperty(getNameKeys(itemStack));
    }
    
    @Nullable
    public T getProperty(Item it)
    {
        return getProperty(getNameKeys(it));
    }
    
    @Nullable
    public T getProperty(Block it)
    {
        return getProperty(getNameKeys(it));
    }

    @Nullable
    public T getProperty(Entity entity)
    {
        return getProperty(getNameKeys(entity));
    }
    
    @Nullable
    public T getProperty(TileEntity it)
    {
        return getProperty(getNameKeys(it));
    }
    
    /*
    @Nullable
    public T getProperty(IThing it)
    {
        return getProperty(it.getNameKeys());
    }
    */

    @Nullable
    public T getProperty(ResourceLocation loc)
    {
        String key = (loc == null) ? null : loc.toString();
        return getProperty(key);
    }

    // ----------------------------------------------------------------------
    // get value for different types

    /*
     * Get the highest priority value we match for stack Checks all keys for stack - expands to multiple keys in defined order: id@meta, id, minecraft:food
     * 
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
    
    /*
    public T getValue(IThing it)
    {
        return Util.getValueOrDefault(getProperty(it), FALSE_VALUE);
    }
    */

    public T getValue(ResourceLocation loc)
    {
        return Util.getValueOrDefault(getProperty(loc), FALSE_VALUE);
    }

    // ----------------------------------------------------------------------
    // check for non-FALSE_VALUE for different types

    /**
     * Does thing not match FALSE_VALUE? aka does thing have no entry or the default value as the entry?
     * 
     * @returns true if thing doesn't match FALSE_VALUE, false if it does
     */
    public boolean doesIt(T value)
    {
        return value != FALSE_VALUE;
    }

    public boolean doesIt(ItemStack stack)
    {
        return doesIt(getValue(stack));
    }

    public boolean doesIt(Entity entity)
    {
        return doesIt(getValue(entity));
    }

    public boolean doesIt(TileEntity entity)
    {
        return doesIt(getValue(entity));
    }
    
    /*
    public boolean doesIt(IThing it)
    {
        return doesIt(getValue(it));
    }
    */

    public boolean doesIt(ResourceLocation loc)
    {
        return getValue(loc) != FALSE_VALUE;
    }
}
