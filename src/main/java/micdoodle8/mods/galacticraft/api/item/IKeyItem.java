/*
 * Copyright (c) 2022 Team Galacticraft
 *
 * Licensed under the MIT license.
 * See LICENSE file in the project root for details.
 */

package micdoodle8.mods.galacticraft.api.item;

import net.minecraft.item.ItemStack;

/**
 * Implement into a key Item class to allow @IKeyable tile entities to get
 * activated while holding this item <p> Nothing here (yet)
 */
public interface IKeyItem
{

    /**
     * Gets the tier of this object
     *
     * @return - The item's tier
     */
    int getTier(ItemStack stack);
}
