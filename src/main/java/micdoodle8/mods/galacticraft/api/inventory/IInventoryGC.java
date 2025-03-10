/*
 * Copyright (c) 2022 Team Galacticraft
 *
 * Licensed under the MIT license.
 * See LICENSE file in the project root for details.
 */

package micdoodle8.mods.galacticraft.api.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

/**
 * An interface for other mods to access the Galacticraft extended inventory
 * slots.
 * 
 * (All normal IInventory methods will work)
 */
public interface IInventoryGC extends IInventory
{

    /**
     * Drop only the Galacticraft items from the player's inventory.
     * 
     * @param player
     */
    void dropExtendedItems(EntityPlayer player);

    /**
     * Make the implementing inventory a copy of the specified extended
     * inventory.
     * 
     * @param par1InventoryPlayer The inventory to copy
     */
    void copyInventory(IInventoryGC par1InventoryPlayer);
}
