/*
 * Copyright (c) 2022 Team Galacticraft
 *
 * Licensed under the MIT license.
 * See LICENSE file in the project root for details.
 */

package micdoodle8.mods.galacticraft.planets.venus.world.gen.layer;

import micdoodle8.mods.miccore.IntCache;
import net.minecraft.world.gen.layer.GenLayer;

public class GenLayerVenusSurround extends GenLayer
{

    public GenLayerVenusSurround(long l, GenLayer parent)
    {
        super(l);
        this.parent = parent;
    }

    public GenLayerVenusSurround(long l)
    {
        super(l);
    }

    @Override
    public int[] getInts(int x, int z, int width, int depth)
    {
        int nx = x - 1;
        int nz = z - 1;
        int nwidth = width + 2;
        int ndepth = depth + 2;
        int input[] = parent.getInts(nx, nz, nwidth, ndepth);
        int output[] = IntCache.getIntCache(width * depth);
        for (int dz = 0; dz < depth; dz++)
        {
            for (int dx = 0; dx < width; dx++)
            {
                output[dx + dz * width] = input[dx + 1 + (dz + 1) * nwidth];
            }
        }

        return output;
    }

    boolean surrounded(int biome, int center, int right, int left, int up, int down)
    {
        if (center != biome)
        {
            return false;
        }

        if (right != biome)
        {
            return false;
        }
        if (left != biome)
        {
            return false;
        }
        if (up != biome)
        {
            return false;
        }
        if (down != biome)
        {
            return false;
        }

        return true;
    }
}
