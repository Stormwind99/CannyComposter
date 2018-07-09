package com.wumple.util;

import net.minecraft.item.ItemStack;

public class SUtil
{
	static public boolean isEmpty(ItemStack stack)
	{
		return ((stack == null) || (stack.isEmpty()));
	}
}
