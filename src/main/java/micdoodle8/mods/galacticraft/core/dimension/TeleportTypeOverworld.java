/*
 * Copyright (c) 2022 Team Galacticraft
 *
 * Licensed under the MIT license.
 * See LICENSE file in the project root for details.
 */

package micdoodle8.mods.galacticraft.core.dimension;

import java.util.Random;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.api.world.ITeleportType;
import micdoodle8.mods.galacticraft.core.entities.player.GCPlayerStats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class TeleportTypeOverworld implements ITeleportType
{

    @Override
    public boolean useParachute()
    {
        return true;
    }

    @Override
    public Vector3 getPlayerSpawnLocation(WorldServer world, EntityPlayerMP player)
    {
        if (player != null)
        {
            GCPlayerStats stats = GCPlayerStats.get(player);
            return new Vector3(stats.getCoordsTeleportedFromX(), 250.0, stats.getCoordsTeleportedFromZ());
        }

        return null;
    }

    @Override
    public Vector3 getEntitySpawnLocation(WorldServer world, Entity entity)
    {
        return new Vector3(entity.posX, 250.0, entity.posZ);
    }

    @Override
    public Vector3 getParaChestSpawnLocation(WorldServer world, EntityPlayerMP player, Random rand)
    {
        final double x = (rand.nextDouble() * 2 - 1.0D) * 5.0D;
        final double z = (rand.nextDouble() * 2 - 1.0D) * 5.0D;

        return new Vector3(player.posX + x, 230.0D, player.posZ + z);
    }

    @Override
    public void onSpaceDimensionChanged(World newWorld, EntityPlayerMP player, boolean ridingAutoRocket)
    {
    }

    @Override
    public void setupAdventureSpawn(EntityPlayerMP player)
    {
        // TODO Auto-generated method stub

    }
}
