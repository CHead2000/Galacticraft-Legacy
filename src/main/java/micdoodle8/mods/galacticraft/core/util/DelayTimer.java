/*
 * Copyright (c) 2022 Team Galacticraft
 *
 * Licensed under the MIT license.
 * See LICENSE file in the project root for details.
 */

package micdoodle8.mods.galacticraft.core.util;

import net.minecraft.world.World;

public class DelayTimer
{

    private long lastMark = Long.MIN_VALUE;
    private long internalDelay = 1;

    public DelayTimer(long delay)
    {
        internalDelay = delay;
    }

    public boolean markTimeIfDelay(World world)
    {
        return markTimeIfDelay(world, internalDelay);
    }

    public boolean markTimeIfDelay(World world, long delay)
    {
        if (world == null)
        {
            return false;
        }

        long currentTime = world.getTotalWorldTime();

        if (currentTime < lastMark)
        {
            lastMark = currentTime;
            return false;
        } else if (lastMark + delay <= currentTime)
        {
            lastMark = currentTime;
            return true;
        } else
        {
            return false;
        }
    }
}
