/*
 * Copyright (c) 2022 Team Galacticraft
 *
 * Licensed under the MIT license.
 * See LICENSE file in the project root for details.
 */

package micdoodle8.mods.galacticraft.api.transmission.tile;

import net.minecraft.tileentity.TileEntity;

/**
 * Applied to TileEntities.
 *
 * @author Calclavia
 */
public interface INetworkConnection extends IConnector
{

    /**
     * Gets a list of all the connected TileEntities that this conductor is
     * connected to. The array's length should be always the 6 adjacent wires.
     *
     * @return
     */
    TileEntity[] getAdjacentConnections();

    /**
     * Refreshes the conductor
     */
    void refresh();

    void onNetworkChanged();
}
