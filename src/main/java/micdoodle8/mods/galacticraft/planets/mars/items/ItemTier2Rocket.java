/*
 * Copyright (c) 2022 Team Galacticraft
 *
 * Licensed under the MIT license.
 * See LICENSE file in the project root for details.
 */

package micdoodle8.mods.galacticraft.planets.mars.items;

import java.util.List;
import javax.annotation.Nullable;
import micdoodle8.mods.galacticraft.api.entity.IRocketType;
import micdoodle8.mods.galacticraft.api.entity.IRocketType.EnumRocketType;
import micdoodle8.mods.galacticraft.api.item.GCRarity;
import micdoodle8.mods.galacticraft.api.item.IHoldableItem;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityTieredRocket;
import micdoodle8.mods.galacticraft.core.GCBlocks;
import micdoodle8.mods.galacticraft.core.GCFluids;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.items.ISortableItem;
import micdoodle8.mods.galacticraft.core.tile.TileEntityLandingPad;
import micdoodle8.mods.galacticraft.core.util.EnumColor;
import micdoodle8.mods.galacticraft.core.util.EnumSortCategoryItem;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import micdoodle8.mods.galacticraft.planets.mars.entities.EntityCargoRocket;
import micdoodle8.mods.galacticraft.planets.mars.entities.EntityTier2Rocket;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemTier2Rocket extends Item implements IHoldableItem, ISortableItem, GCRarity
{

    public ItemTier2Rocket()
    {
        super();
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        this.setMaxStackSize(1);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public CreativeTabs getCreativeTab()
    {
        return GalacticraftCore.galacticraftItemsTab;
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
    {
        ItemStack stack = player.getHeldItem(hand);
        boolean padFound = false;
        TileEntity tile = null;

        if (world.isRemote)
        {
            return EnumActionResult.PASS;
        } else
        {
            float centerX = -1;
            float centerY = -1;
            float centerZ = -1;

            for (int i = -1; i < 2; i++)
            {
                for (int j = -1; j < 2; j++)
                {
                    BlockPos pos1 = pos.add(i, 0, j);
                    IBlockState state = world.getBlockState(pos1);
                    final Block id = state.getBlock();
                    int meta = id.getMetaFromState(state);

                    if (id == GCBlocks.landingPadFull && meta == 0)
                    {
                        padFound = true;
                        tile = world.getTileEntity(pos1);

                        centerX = pos.getX() + i + 0.5F;
                        centerY = pos.getY() + 0.4F;
                        centerZ = pos.getZ() + j + 0.5F;

                        break;
                    }
                }

                if (padFound)
                {
                    break;
                }
            }

            if (padFound)
            {
                if (!placeRocketOnPad(stack, world, tile, centerX, centerY, centerZ))
                {
                    return EnumActionResult.FAIL;
                }

                if (!player.capabilities.isCreativeMode)
                {
                    stack.shrink(1);
                }
                return EnumActionResult.SUCCESS;
            } else
            {
                return EnumActionResult.PASS;
            }
        }
    }

    public static boolean placeRocketOnPad(ItemStack stack, World world, TileEntity tile, float centerX, float centerY, float centerZ)
    {
        // Check whether there is already a rocket on the pad
        if (tile instanceof TileEntityLandingPad)
        {
            if (((TileEntityLandingPad) tile).getDockedEntity() != null)
            {
                return false;
            }
        } else
        {
            return false;
        }

        EntityAutoRocket rocket;

        if (stack.getItemDamage() < 10)
        {
            rocket = new EntityTier2Rocket(world, centerX, centerY, centerZ, EnumRocketType.values()[stack.getItemDamage()]);
        } else
        {
            rocket = new EntityCargoRocket(world, centerX, centerY, centerZ, EnumRocketType.values()[stack.getItemDamage() - 10]);
        }

        rocket.setPosition(rocket.posX, rocket.posY + rocket.getOnPadYOffset(), rocket.posZ);
        world.spawnEntity(rocket);

        if (((IRocketType) rocket).getType().getPreFueled())
        {
            if (rocket instanceof EntityTieredRocket)
            {
                ((EntityTieredRocket) rocket).fuelTank.fill(new FluidStack(GCFluids.fluidFuel, rocket.getMaxFuel()), true);
            } else
            {
                ((EntityCargoRocket) rocket).fuelTank.fill(new FluidStack(GCFluids.fluidFuel, rocket.getMaxFuel()), true);
            }
        } else if (stack.hasTagCompound() && stack.getTagCompound().hasKey("RocketFuel"))
        {
            rocket.fuelTank.fill(new FluidStack(GCFluids.fluidFuel, stack.getTagCompound().getInteger("RocketFuel")), true);
        }

        return true;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list)
    {
        if (tab == GalacticraftCore.galacticraftItemsTab || tab == CreativeTabs.SEARCH)
        {
            for (int i = 0; i < EnumRocketType.values().length; i++)
            {
                list.add(new ItemStack(this, 1, i));
            }

            for (int i = 11; i < 10 + EnumRocketType.values().length; i++)
            {
                list.add(new ItemStack(this, 1, i));
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack par1ItemStack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        EnumRocketType type;

        if (par1ItemStack.getItemDamage() < 10)
        {
            type = EnumRocketType.values()[par1ItemStack.getItemDamage()];
        } else
        {
            type = EnumRocketType.values()[par1ItemStack.getItemDamage() - 10];
        }

        if (!type.getTooltip().isEmpty())
        {
            tooltip.add(type.getTooltip());
        }

        if (type.getPreFueled())
        {
            tooltip.add(EnumColor.RED + "\u00a7o" + GCCoreUtil.translate("gui.creative_only.desc"));
        }

        if (par1ItemStack.hasTagCompound() && par1ItemStack.getTagCompound().hasKey("RocketFuel"))
        {
            EntityAutoRocket rocket;

            if (par1ItemStack.getItemDamage() < 10)
            {
                rocket = new EntityTier2Rocket(FMLClientHandler.instance().getWorldClient(), 0, 0, 0, EnumRocketType.values()[par1ItemStack.getItemDamage()]);
            } else
            {
                rocket = new EntityCargoRocket(FMLClientHandler.instance().getWorldClient(), 0, 0, 0, EnumRocketType.values()[par1ItemStack.getItemDamage() - 10]);
            }

            tooltip.add(GCCoreUtil.translate("gui.message.fuel.name") + ": " + par1ItemStack.getTagCompound().getInteger("RocketFuel") + " / " + rocket.fuelTank.getCapacity());
        }

        if (par1ItemStack.getItemDamage() >= 10)
        {
            tooltip.add(EnumColor.AQUA + GCCoreUtil.translate("gui.requires_controller.desc"));
        }
    }

    @Override
    public String getTranslationKey(ItemStack par1ItemStack)
    {
        return super.getTranslationKey(par1ItemStack) + (par1ItemStack.getItemDamage() < 10 ? ".t2Rocket" : ".cargo_rocket");
    }

    @Override
    public boolean shouldHoldLeftHandUp(EntityPlayer player)
    {
        return true;
    }

    @Override
    public boolean shouldHoldRightHandUp(EntityPlayer player)
    {
        return true;
    }

    @Override
    public boolean shouldCrouch(EntityPlayer player)
    {
        return true;
    }

    @Override
    public EnumSortCategoryItem getCategory(int meta)
    {
        return EnumSortCategoryItem.ROCKET;
    }
}
