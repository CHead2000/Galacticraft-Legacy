/*
 * Copyright (c) 2022 Team Galacticraft
 *
 * Licensed under the MIT license.
 * See LICENSE file in the project root for details.
 */

package micdoodle8.mods.galacticraft.api.client;

import net.minecraft.world.WorldProvider;

public interface IScreenManager
{

    /**
     * Used by screen renderers to figure out which world they are in
     * 
     * @return The WorldProvider of the world where the screen driver is located
     */
    WorldProvider getWorldProvider();
}
