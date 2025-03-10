/*
 * Copyright (c) 2022 Team Galacticraft
 *
 * Licensed under the MIT license.
 * See LICENSE file in the project root for details.
 */

package micdoodle8.mods.galacticraft.api.power;

public interface IEnergyStorageGC
{

    /**
     * Add energy to the storage.
     *
     * @param amount Maximum amount of energy to receive
     * @param simulate If true, the transfer will only be simulated.
     * @return The amount of energy that was successfully received (or would
     *         have been, if simulated).
     */
    float receiveEnergyGC(float amount, boolean simulate);

    /**
     * Remove energy from the storage.
     *
     * @param amount Maximum amount of energy to extract
     * @param simulate If true, the transfer will only be simulated.
     * @return The amount of energy that was successfully extracted (or would
     *         have been, if simulated).
     */
    float extractEnergyGC(float amount, boolean simulate);

    /**
     * Returns the amount of energy stored
     */
    float getEnergyStoredGC();

    /**
     * Returns the maximum amount of energy stored
     */
    float getCapacityGC();
}
