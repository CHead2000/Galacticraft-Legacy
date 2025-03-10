/*
 * Copyright (c) 2022 Team Galacticraft
 *
 * Licensed under the MIT license.
 * See LICENSE file in the project root for details.
 */

package micdoodle8.mods.galacticraft.core.recipe;

import java.util.HashMap;
import java.util.Map.Entry;
import micdoodle8.mods.galacticraft.api.recipe.INasaWorkbenchRecipe;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class NasaWorkbenchRecipe implements INasaWorkbenchRecipe
{

    private ItemStack output;
    private HashMap<Integer, ItemStack> input;

    public NasaWorkbenchRecipe(ItemStack output, HashMap<Integer, ItemStack> input)
    {
        this.output = output;
        this.input = input;

        for (Entry<Integer, ItemStack> entry : this.input.entrySet())
        {
            if (entry.getValue() == null)
            {
                throw new IllegalArgumentException("Recipe contains null ingredient!");
            }
        }
    }

    @Override
    public boolean matches(IInventory inventory)
    {
        for (Entry<Integer, ItemStack> entry : this.input.entrySet())
        {
            ItemStack stackAt = inventory.getStackInSlot(entry.getKey());

            if (!this.checkItemEquals(stackAt, entry.getValue()))
            {
                return false;
            }
        }

        return true;
    }

    private boolean checkItemEquals(ItemStack target, ItemStack input)
    {
        if (input.isEmpty() && !target.isEmpty() || !input.isEmpty() && target.isEmpty())
        {
            return false;
        }
        return target.isEmpty() && input.isEmpty()
            || target.getItem() == input.getItem() && (target.getItemDamage() == OreDictionary.WILDCARD_VALUE || target.getItemDamage() == input.getItemDamage());
    }

    @Override
    public int getRecipeSize()
    {
        return this.input.size();
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return this.output.copy();
    }

    @Override
    public HashMap<Integer, ItemStack> getRecipeInput()
    {
        return this.input;
    }
}
