/*
 * Copyright (c) 2022 Team Galacticraft
 *
 * Licensed under the MIT license.
 * See LICENSE file in the project root for details.
 */

package micdoodle8.mods.galacticraft.core.energy.tile;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.ISpecialElectricItem;
import java.util.EnumSet;
import javax.annotation.Nonnull;
import mekanism.api.energy.EnergizedItemManager;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyOutputter;
import micdoodle8.mods.galacticraft.api.item.ElectricItemHelper;
import micdoodle8.mods.galacticraft.api.item.IItemElectric;
import micdoodle8.mods.galacticraft.api.transmission.tile.IConductor;
import micdoodle8.mods.galacticraft.api.transmission.tile.IElectrical;
import micdoodle8.mods.galacticraft.core.energy.EnergyConfigHandler;
import micdoodle8.mods.galacticraft.core.tile.ReceiverMode;
import micdoodle8.mods.galacticraft.core.util.CompatibilityManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.items.CapabilityItemHandler;

@InterfaceList(value = {
    @Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = CompatibilityManager.modidIC2),
    @Interface(iface = "ic2.api.energy.tile.IEnergyAcceptor", modid = CompatibilityManager.modidIC2),
    @Interface(iface = "buildcraft.api.mj.IMjReceiver", modid = CompatibilityManager.modBCraftEnergy),
    @Interface(iface = "mekanism.api.energy.IStrictEnergyOutputter", modid = CompatibilityManager.modidMekanism),
    @Interface(iface = "mekanism.api.energy.IStrictEnergyAcceptor", modid = CompatibilityManager.modidMekanism)
})
public abstract class TileBaseUniversalElectrical extends EnergyStorageTile implements IEnergySink, IEnergyAcceptor, IMjReceiver, IStrictEnergyOutputter, IStrictEnergyAcceptor
{

    protected boolean isAddedToEnergyNet;
    protected Object powerHandlerBC;

    private float IC2surplusInGJ = 0F;

    public TileBaseUniversalElectrical(String tileName)
    {
        super(tileName);
    }

    @Override
    public double getPacketRange()
    {
        return 12.0D;
    }

    @Override
    public int getPacketCooldown()
    {
        return 3;
    }

    @Override
    public boolean isNetworkedTile()
    {
        return true;
    }

    public EnumSet<EnumFacing> getElectricalInputDirections()
    {
        return EnumSet.allOf(EnumFacing.class);
    }

    public EnumSet<EnumFacing> getElectricalOutputDirections()
    {
        return EnumSet.noneOf(EnumFacing.class);
    }

    @Override
    public float getRequest(EnumFacing direction)
    {
        if (this.getElectricalInputDirections().contains(direction) || direction == null)
        {
            return super.getRequest(direction);
        }

        return 0F;
    }

    @Override
    public float receiveElectricity(EnumFacing from, float receive, int tier, boolean doReceive)
    {
        if (this.getElectricalInputDirections().contains(from) || from == null)
        {
            return super.receiveElectricity(from, receive, tier, doReceive);
        }

        return 0F;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        return nbt;
    }

    public void discharge(ItemStack itemStack)
    {
        if (itemStack != null)
        {
            Item item = itemStack.getItem();
            float energyToDischarge = this.getRequest(null);

            if (item instanceof IItemElectric)
            {
                this.storage.receiveEnergyGC(ElectricItemHelper.dischargeItem(itemStack, energyToDischarge));
                this.poweredByTierGC = ((IItemElectric) item).getTierGC(itemStack);
            }
            else if (EnergyConfigHandler.isMekanismLoaded() && item instanceof IEnergizedItem && ((IEnergizedItem) item).canSend(itemStack))
            {
                this.storage.receiveEnergyGC((float) EnergizedItemManager.discharge(itemStack, energyToDischarge / EnergyConfigHandler.MEKANISM_RATIO) * EnergyConfigHandler.MEKANISM_RATIO);
            } else if (EnergyConfigHandler.isIndustrialCraft2Loaded())
            {
                if (item instanceof ISpecialElectricItem && item instanceof IElectricItem)
                {
                    IElectricItem electricItem = (IElectricItem) item;
                    ISpecialElectricItem specialElectricItem = (ISpecialElectricItem) item;
                    if (electricItem.canProvideEnergy(itemStack))
                    {
                        double energyDischargeIC2 = energyToDischarge / EnergyConfigHandler.IC2_RATIO;
                        double result = specialElectricItem.getManager(itemStack).discharge(itemStack, energyDischargeIC2, 4, false, false, false);
                        float energyDischarged = (float) result * EnergyConfigHandler.IC2_RATIO;
                        this.storage.receiveEnergyGC(energyDischarged);
                    }
                } else if (item instanceof IElectricItem)
                {
                    IElectricItem electricItem = (IElectricItem) item;
                    if (electricItem.canProvideEnergy(itemStack))
                    {
                        double energyDischargeIC2 = energyToDischarge / EnergyConfigHandler.IC2_RATIO;
                        double result = ElectricItem.manager.discharge(itemStack, energyDischargeIC2, 4, false, false, false);
                        float energyDischarged = (float) result * EnergyConfigHandler.IC2_RATIO;
                        this.storage.receiveEnergyGC(energyDischarged);
                    }
                }

            }
        }
    }

    @Override
    public void initiate()
    {
        super.initiate();
    }

    @Override
    public void update()
    {
        super.update();

        if (!this.world.isRemote)
        {
            if (!this.isAddedToEnergyNet)
            {
                // Register to the IC2 Network
                this.initIC();
            }

            if (EnergyConfigHandler.isIndustrialCraft2Loaded() && this.IC2surplusInGJ >= 0.001F)
            {
                this.IC2surplusInGJ -= this.storage.receiveEnergyGC(this.IC2surplusInGJ);
                if (this.IC2surplusInGJ < 0.001F)
                {
                    this.IC2surplusInGJ = 0;
                }
            }
        }
    }

    /**
     * IC2 Methods
     */
    @Override
    public void invalidate()
    {
        super.invalidate();
        this.unloadTileIC2();
    }

    @Override
    public void onChunkUnload()
    {
        super.onChunkUnload();
        this.unloadTileIC2();
    }

    protected void initIC()
    {
        if (EnergyConfigHandler.isIndustrialCraft2Loaded())
        {
            try
            {
                Object o = CompatibilityManager.classIC2tileEventLoad.getConstructor(IEnergyTile.class).newInstance(this);

                if (o instanceof Event)
                {
                    MinecraftForge.EVENT_BUS.post((Event) o);
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        this.isAddedToEnergyNet = true;
    }

    private void unloadTileIC2()
    {
        if (this.isAddedToEnergyNet && this.world != null)
        {
            if (EnergyConfigHandler.isIndustrialCraft2Loaded() && !this.world.isRemote)
            {
                try
                {
                    Object o = CompatibilityManager.classIC2tileEventUnload.getConstructor(IEnergyTile.class).newInstance(this);

                    if (o instanceof Event)
                    {
                        MinecraftForge.EVENT_BUS.post((Event) o);
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            this.isAddedToEnergyNet = false;
        }
    }

    @Override
    @Method(modid = CompatibilityManager.modidIC2)
    public double getDemandedEnergy()
    {
        if (EnergyConfigHandler.disableIC2Input)
        {
            return 0.0;
        }

        try
        {
            if (this.IC2surplusInGJ < 0.001F)
            {
                this.IC2surplusInGJ = 0F;
                return Math.ceil((this.storage.receiveEnergyGC(Integer.MAX_VALUE, true)) / EnergyConfigHandler.IC2_RATIO);
            }

            float received = this.storage.receiveEnergyGC(this.IC2surplusInGJ, true);
            if (received == this.IC2surplusInGJ)
            {
                return Math.ceil((this.storage.receiveEnergyGC(Integer.MAX_VALUE, true) - this.IC2surplusInGJ) / EnergyConfigHandler.IC2_RATIO);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return 0D;
    }

    @Override
    @Method(modid = CompatibilityManager.modidIC2)
    public double injectEnergy(EnumFacing direction, double amount, double voltage)
    {
        // IC2 in 1.8.9 seems to have reversed the sense of direction here, but
        // not in acceptsEnergyFrom. (Seriously?!)
        if (!EnergyConfigHandler.disableIC2Input && (direction == null || this.getElectricalInputDirections().contains(direction.getOpposite())))
        {
            float convertedEnergy = (float) amount * EnergyConfigHandler.IC2_RATIO;
            int tierFromIC2 = ((int) voltage > 120) ? (((int) voltage > 256) ? 4 : 2) : 1;
            float receive = this.receiveElectricity(direction == null ? null : direction.getOpposite(), convertedEnergy, tierFromIC2, true);

            if (convertedEnergy > receive)
            {
                this.IC2surplusInGJ = convertedEnergy - receive;
            } else
            {
                this.IC2surplusInGJ = 0F;
            }

            // injectEnergy returns left over energy but all is used or goes
            // into 'surplus'
            return 0D;
        }

        return amount;
    }

    @Override
    @Method(modid = CompatibilityManager.modidIC2)
    public int getSinkTier()
    {
        return 3;
    }

    @Override
    @Method(modid = CompatibilityManager.modidIC2)
    public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing direction)
    {
        if (this.tileEntityInvalid)
            return false;
        //Don't add connection to IC2 grid if it's a Galacticraft tile
        if (emitter instanceof IElectrical || emitter instanceof IConductor || !(emitter instanceof IEnergyTile))
        {
            return false;
        }

        return this.getElectricalInputDirections().contains(direction);
    }

    // BuildCraft
    @Override
    public boolean canReceive()
    {
        return !EnergyConfigHandler.disableBuildCraftInput;
    }

    // Buildcraft 7
    @Override
    @Method(modid = CompatibilityManager.modBCraftEnergy)
    public long getPowerRequested()
    {
        if (EnergyConfigHandler.disableBuildCraftInput)
        {
            return 0L;
        }

        // Boost stated demand by factor of 30, otherwise Buildcraft seems to
        // send only a trickle of power
        return (long) (this.storage.receiveEnergyGC(Integer.MAX_VALUE, true) / EnergyConfigHandler.BC8_INTERNAL_RATIO * 30F);
    }

    // Buildcraft 7
    @Override
    @Method(modid = CompatibilityManager.modBCraftEnergy)
    public long receivePower(long microJoules, boolean simulate)
    {
        if (EnergyConfigHandler.disableBuildCraftInput)
        {
            return microJoules;
        }
        float receiveGC = microJoules * EnergyConfigHandler.BC8_INTERNAL_RATIO;
        float sentGC = receiveGC - super.receiveElectricity(null, receiveGC, 1, !simulate);
        return (long) (sentGC / EnergyConfigHandler.BC8_INTERNAL_RATIO);
    }

    // Buildcraft 7
    @Override
    @Method(modid = CompatibilityManager.modBCraftEnergy)
    public boolean canConnect(@Nonnull IMjConnector other)
    {
        return true;
    }

    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate)
    {
        if (EnergyConfigHandler.disableRFInput)
        {
            return 0;
        }

        if (!this.getElectricalInputDirections().contains(from))
        {
            return 0;
        }

        return MathHelper.floor(super.receiveElectricity(from, maxReceive * EnergyConfigHandler.RF_RATIO, 1, !simulate) / EnergyConfigHandler.RF_RATIO);
    }

    public boolean canConnectEnergy(EnumFacing from)
    {
        return this.getElectricalInputDirections().contains(from) || this.getElectricalOutputDirections().contains(from);
    }

    public int getEnergyStored(EnumFacing from)
    {
        return MathHelper.floor(this.getEnergyStoredGC() / EnergyConfigHandler.RF_RATIO);
    }

    public int getMaxEnergyStored(EnumFacing from)
    {
        return MathHelper.floor(this.getMaxEnergyStoredGC() / EnergyConfigHandler.RF_RATIO);
    }

    @Method(modid = CompatibilityManager.modidMekanism)
    public double transferEnergyToAcceptor(EnumFacing from, double amount)
    {
        if (EnergyConfigHandler.disableMekanismInput)
        {
            return 0;
        }

        if (!this.getElectricalInputDirections().contains(from))
        {
            return 0;
        }

        return this.receiveElectricity(from, (float) amount * EnergyConfigHandler.MEKANISM_RATIO, 1, true) / EnergyConfigHandler.MEKANISM_RATIO;
    }

    @Override
    @Method(modid = CompatibilityManager.modidMekanism)
    public boolean canReceiveEnergy(EnumFacing side)
    {
        return this.getElectricalInputDirections().contains(side);
    }

    @Override
    @Method(modid = CompatibilityManager.modidMekanism)
    public double acceptEnergy(EnumFacing side, double amount, boolean simulate)
    {
        if (EnergyConfigHandler.disableMekanismInput)
        {
            return 0.0;
        }

        if (!this.getElectricalInputDirections().contains(side))
        {
            return 0;
        }

        return this.receiveElectricity(side, (float) amount * EnergyConfigHandler.MEKANISM_RATIO, 1, simulate) / EnergyConfigHandler.MEKANISM_RATIO;
    }

    @Method(modid = CompatibilityManager.modidMekanism)
    public void setEnergy(double energy)
    {
        if (EnergyConfigHandler.disableMekanismInput)
        {
            return;
        }

        this.storage.setEnergyStored((float) energy * EnergyConfigHandler.MEKANISM_RATIO);
    }

    @Method(modid = CompatibilityManager.modidMekanism)
    public double getMaxEnergy()
    {
        if (EnergyConfigHandler.disableMekanismInput)
        {
            return 0.0;
        }

        return this.getMaxEnergyStoredGC() / EnergyConfigHandler.MEKANISM_RATIO;
    }

    @Override
    public boolean canOutputEnergy(EnumFacing side)
    {
        return this.getElectricalOutputDirections().contains(side);
    }

    @Override
    @Method(modid = CompatibilityManager.modidMekanism)
    public double pullEnergy(EnumFacing side, double amount, boolean simulate)
    {
        return 0D;
    }

    @Override
    public ReceiverMode getModeFromDirection(EnumFacing direction)
    {
        if (this.getElectricalInputDirections().contains(direction))
        {
            return ReceiverMode.RECEIVE;
        } else if (this.getElectricalOutputDirections().contains(direction))
        {
            return ReceiverMode.EXTRACT;
        }

        return null;
    }

    /*
     * Compatibility: call this if the facing metadata is updated
     */
    public void updateFacing()
    {
        if (EnergyConfigHandler.isIndustrialCraft2Loaded() && !this.world.isRemote)
        {
            // This seems the only method to tell IC2 the connection sides have
            // changed
            // (Maybe there is an internal refresh() method but it's not in the
            // API)
            this.unloadTileIC2();
            // This will do an initIC2 on next tick update.
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        if (capability == CapabilityEnergy.ENERGY || (EnergyConfigHandler.isBuildcraftLoaded() && (capability == MjAPI.CAP_RECEIVER || capability == MjAPI.CAP_CONNECTOR)))
        {
            return this.getElectricalInputDirections().contains(facing);
        }
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (capability == CapabilityEnergy.ENERGY || (EnergyConfigHandler.isBuildcraftLoaded() && (capability == MjAPI.CAP_RECEIVER || capability == MjAPI.CAP_CONNECTOR)))
        {
            return this.getElectricalInputDirections().contains(facing) ? (T) new ForgeReceiver(this) : null;
        }
        return super.getCapability(capability, facing);
    }

    @Interface(modid = CompatibilityManager.modBCraftEnergy, iface = "buildcraft.api.mj.IMjReceiver")
    private static class ForgeReceiver implements net.minecraftforge.energy.IEnergyStorage, IMjReceiver
    {

        private TileBaseUniversalElectrical tile;

        public ForgeReceiver(TileBaseUniversalElectrical tileElectrical)
        {
            this.tile = tileElectrical;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate)
        {
            if (EnergyConfigHandler.disableFEInput)
                return 0;

            return MathHelper.floor(tile.receiveElectricity(null, maxReceive * EnergyConfigHandler.RF_RATIO, 1, !simulate) / EnergyConfigHandler.RF_RATIO);
        }

        @Override
        public boolean canReceive()
        {
            return !EnergyConfigHandler.disableFEInput;
        }

        @Override
        public int getEnergyStored()
        {
            if (EnergyConfigHandler.disableFEInput)
                return 0;

            return MathHelper.floor(tile.getEnergyStoredGC() / EnergyConfigHandler.RF_RATIO);
        }

        @Override
        public int getMaxEnergyStored()
        {
            if (EnergyConfigHandler.disableFEInput)
                return 0;

            return MathHelper.floor(tile.getMaxEnergyStoredGC() / EnergyConfigHandler.RF_RATIO);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate)
        {
            return 0;
        }

        @Override
        public boolean canExtract()
        {
            return false;
        }

        @Override
        @Method(modid = CompatibilityManager.modBCraftEnergy)
        public boolean canConnect(@Nonnull IMjConnector other)
        {
            return true;
        }

        @Override
        @Method(modid = CompatibilityManager.modBCraftEnergy)
        public long getPowerRequested()
        {
            return tile.getPowerRequested();
        }

        @Override
        @Method(modid = CompatibilityManager.modBCraftEnergy)
        public long receivePower(long microJoules, boolean simulate)
        {
            return tile.receivePower(microJoules, simulate);
        }
    }
}
