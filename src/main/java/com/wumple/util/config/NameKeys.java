package com.wumple.util.config;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IProperty;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

/*
 * Generate a set of strings that represent the queried object
 */
public class NameKeys
{
    // ----------------------------------------------------------------------
    // Utility
    
    /**
     * Generate a set of strings that represent the queried ItemStack
     * @see TypeIdentifier for opposite direction but similiar code
     * @param itemStack for which to get namekeys for lookup
     * @return namekeys to search config for, in order
     */
    static public ArrayList<String> getNameKeys(ItemStack itemStack)
    {
        ArrayList<String> nameKeys = new ArrayList<String>();
        
        if (itemStack == null)
        {
            return nameKeys;
        }

        Item item = itemStack.getItem();
        
        addNameKeysResLoc(nameKeys, item);
        addNameKeysItemTags(nameKeys, item);
        addNameKeysObject(nameKeys, (Object)itemStack);
        addNameKeysObject(nameKeys, (Object)item);
        
        return nameKeys;
    }
    
    static public ArrayList<String> getNameKeys(Item item)
    {
        ArrayList<String> nameKeys = new ArrayList<String>();
        
        if (item == null)
        {
            return nameKeys;
        }

        addNameKeysResLoc(nameKeys, item);
        addNameKeysItemTags(nameKeys, item);
        addNameKeysObject(nameKeys, (Object)item);
        
        return nameKeys;
    }
    
    static public ArrayList<String> getNameKeys(Block block)
    {
        ArrayList<String> nameKeys = new ArrayList<String>();
        
        addNameKeysResLoc(nameKeys, block);
        addNameKeysBlockTags(nameKeys, block);
        addNameKeysObject(nameKeys, (Object)block);
        
        return nameKeys;
    }

    static public ArrayList<String> getNameKeys(Entity entity)
    {
        ArrayList<String> nameKeys = new ArrayList<String>();
        
        if (entity == null)
        {
            return nameKeys;
        }

        addNameKeysResLoc(nameKeys, entity.getType());
        addNameKeysEntityTags(nameKeys, entity);
        addNameKeysObject(nameKeys, (Object)entity.getType());
        
        return nameKeys;
    }
    
    static public ArrayList<String> getNameKeys(TileEntity entity)
    {
        ArrayList<String> nameKeys = new ArrayList<String>();
        
        if (entity == null)
        {
            return nameKeys;
        }

        // OLD: addNameKeysProperty(nameKeys, entity, BlockCrops.AGE);
        addNameKeysResLoc(nameKeys, entity.getType());
        addNameKeysObject(nameKeys, (Object)entity);
        
        return nameKeys;
    }
    
    // ------------------------------------------------------------------------
    // builders
    
    static public ArrayList<String> addNameKeysResLoc(ArrayList<String> nameKeys, IForgeRegistryEntry<?> entry)
    {
        String name = (entry == null) ? null : entry.getRegistryName().toString();

        if (name != null)
        {
            nameKeys.add(name);
        }
        
        return nameKeys;
    }

    static public <T extends Comparable<T>> ArrayList<String> addNameKeysProperty(ArrayList<String> nameKeys, TileEntity te, IProperty<T> property)
    {
        ResourceLocation loc = (te == null) ? null : te.getType().getRegistryName();
        if (loc == null)
        {
            return nameKeys;
        }
        
        BlockPos pos = te.getPos();
        World world = te.getWorld();
        BlockState state = (world != null) ? world.getBlockState(pos) : null;
        Collection<IProperty<?>> props = (state != null) ? state.getProperties() : null;
        if ((props != null) && (props.contains(property)))
        {
            T value = state.get(property);
            nameKeys.add(loc.toString() + "[" + property.getName() + "=" + value.toString() + "]" );
        }
            
        return nameKeys;
    }
    
    static public ArrayList<String> addNameKeysItemTags(ArrayList<String> nameKeys, Item item)
    {
        if (item != null)
        {
        	Collection<ResourceLocation> tags = ItemTags.getCollection().getOwningTags(item);
        	
        	for (ResourceLocation tag : tags)
            {
                nameKeys.add("#" + tag.toString());
            }
        }
        
        return nameKeys;
    }
    
    static public ArrayList<String> addNameKeysBlockTags(ArrayList<String> nameKeys, Block block)
    {
    	Collection<ResourceLocation> tags = BlockTags.getCollection().getOwningTags(block);
    	
    	for (ResourceLocation tag : tags)
        {
            nameKeys.add("#" + tag.toString());
        }
        
        return nameKeys;
    }
    
    static public ArrayList<String> addNameKeysEntityTags(ArrayList<String> nameKeys, Entity it)
    {
    	Collection<ResourceLocation> tags = EntityTypeTags.getCollection().getOwningTags(it.getType());
    	
    	for (ResourceLocation tag : tags)
        {
            nameKeys.add("#" + tag.toString());
        }
        
        return nameKeys;
    }
    
    static protected boolean ConfigAddClassNames()
    {
    	// TODO
    	return false;
    }
    
    static public ArrayList<String> addNameKeysObject(ArrayList<String> nameKeys, Object object)
    {   
        addNameKeysSpecial(nameKeys, object);
        
        if (ConfigAddClassNames())
        {
            addNameKeysClasses(nameKeys, object);
        }
        
        return nameKeys;
    }
    
    static public ArrayList<String> addNameKeysClasses(ArrayList<String> nameKeys, Object object)
    {   
        // class names for dynamic matching
        Class<?> c = object.getClass();
        while (c != null)
        {
            String classname = c.getName();
            nameKeys.add(classname);
            c = c.getSuperclass();
        }
        
        return nameKeys;
    }

    static public ArrayList<String> addNameKeysSpecial(ArrayList<String> nameKeys, Object object)
    {   
        // special tags for backwards compatibility 
    	// no longer needed!
        
        return nameKeys;
    }
}
