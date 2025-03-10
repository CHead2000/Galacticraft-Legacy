/*
 * Copyright (c) 2022 Team Galacticraft
 *
 * Licensed under the MIT license.
 * See LICENSE file in the project root for details.
 */

package micdoodle8.mods.galacticraft.api;

import com.google.common.collect.Lists;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import micdoodle8.mods.galacticraft.api.client.IGameScreen;
import micdoodle8.mods.galacticraft.api.item.EnumExtendedInventorySlot;
import micdoodle8.mods.galacticraft.api.recipe.INasaWorkbenchRecipe;
import micdoodle8.mods.galacticraft.api.recipe.SpaceStationRecipe;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.api.world.ITeleportType;
import micdoodle8.mods.galacticraft.api.world.SpaceStationType;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderSurface;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GalacticraftRegistry
{

    private static Map<Class<? extends WorldProvider>, ITeleportType> teleportTypeMap = new HashMap<Class<? extends WorldProvider>, ITeleportType>();
    private static List<SpaceStationType> spaceStations = new ArrayList<SpaceStationType>();
    private static List<INasaWorkbenchRecipe> rocketBenchT1Recipes = new ArrayList<INasaWorkbenchRecipe>();
    private static List<INasaWorkbenchRecipe> buggyBenchRecipes = new ArrayList<INasaWorkbenchRecipe>();
    private static List<INasaWorkbenchRecipe> rocketBenchT2Recipes = new ArrayList<INasaWorkbenchRecipe>();
    private static List<INasaWorkbenchRecipe> cargoRocketRecipes = new ArrayList<INasaWorkbenchRecipe>();
    private static List<INasaWorkbenchRecipe> rocketBenchT3Recipes = new ArrayList<INasaWorkbenchRecipe>();
    private static List<INasaWorkbenchRecipe> astroMinerRecipes = new ArrayList<INasaWorkbenchRecipe>();
    private static Map<Class<? extends WorldProvider>, ResourceLocation> rocketGuiMap = new HashMap<Class<? extends WorldProvider>, ResourceLocation>();
    private static Map<Integer, List<ItemStack>> dungeonLootMap = new HashMap<Integer, List<ItemStack>>();
    private static List<Integer> dimensionTypeIDs = new ArrayList<Integer>();
    private static List<IGameScreen> gameScreens = new ArrayList<IGameScreen>();
    private static int maxScreenTypes;
    private static Map<Integer, List<Object>> gearMap = new HashMap<>();
    private static Map<Integer, List<EnumExtendedInventorySlot>> gearSlotMap = new HashMap<>();
    private static Method gratingRegister = null;

    /**
     * Register a new Teleport type for the world provider passed
     *
     * @param clazz the world provider class that you wish to customize
     *        teleportation for
     * @param type an ITeleportType-implemented class that will be used for the
     *        provided world type
     */
    public static void registerTeleportType(Class<? extends WorldProvider> clazz, ITeleportType type)
    {
        if (!GalacticraftRegistry.teleportTypeMap.containsKey(clazz))
        {
            GalacticraftRegistry.teleportTypeMap.put(clazz, type);
        }
    }

    /**
     * Link a world provider to a gui texture. This texture will be shown on the
     * left-side of the screen while the player is in the rocket.
     *
     * @param clazz The World Provider class
     * @param rocketGui Resource Location for the gui texture
     */
    public static void registerRocketGui(Class<? extends WorldProvider> clazz, ResourceLocation rocketGui)
    {
        if (!GalacticraftRegistry.rocketGuiMap.containsKey(clazz))
        {
            GalacticraftRegistry.rocketGuiMap.put(clazz, rocketGui);
        }
    }

    /**
     * Add loot to the list of items that can possibly spawn in dungeon chests,
     * but it is guaranteed that one will always spawn
     *
     * @param tier Tier of dungeon chest to add loot to. For example Moon is 1
     *        and Mars is 2
     * @param loot The itemstack to add to the possible list of items
     */
    public static void addDungeonLoot(int tier, ItemStack loot)
    {
        List<ItemStack> dungeonStacks = null;

        if (GalacticraftRegistry.dungeonLootMap.containsKey(tier))
        {
            dungeonStacks = GalacticraftRegistry.dungeonLootMap.get(tier);
            dungeonStacks.add(loot);
        } else
        {
            dungeonStacks = new ArrayList<ItemStack>();
            dungeonStacks.add(loot);
        }

        GalacticraftRegistry.dungeonLootMap.put(tier, dungeonStacks);
    }

    public static void addT1RocketRecipe(INasaWorkbenchRecipe recipe)
    {
        GalacticraftRegistry.rocketBenchT1Recipes.add(recipe);
    }

    public static void addT2RocketRecipe(INasaWorkbenchRecipe recipe)
    {
        GalacticraftRegistry.rocketBenchT2Recipes.add(recipe);
    }

    public static void addT3RocketRecipe(INasaWorkbenchRecipe recipe)
    {
        GalacticraftRegistry.rocketBenchT3Recipes.add(recipe);
    }

    public static void addCargoRocketRecipe(INasaWorkbenchRecipe recipe)
    {
        GalacticraftRegistry.cargoRocketRecipes.add(recipe);
    }

    public static void addMoonBuggyRecipe(INasaWorkbenchRecipe recipe)
    {
        GalacticraftRegistry.buggyBenchRecipes.add(recipe);
    }

    public static void addAstroMinerRecipe(INasaWorkbenchRecipe recipe)
    {
        GalacticraftRegistry.astroMinerRecipes.add(recipe);
    }

    public static void removeT1RocketRecipe(INasaWorkbenchRecipe recipe)
    {
        GalacticraftRegistry.rocketBenchT1Recipes.remove(recipe);
    }

    public static void removeT2RocketRecipe(INasaWorkbenchRecipe recipe)
    {
        GalacticraftRegistry.rocketBenchT2Recipes.remove(recipe);
    }

    public static void removeT3RocketRecipe(INasaWorkbenchRecipe recipe)
    {
        GalacticraftRegistry.rocketBenchT3Recipes.remove(recipe);
    }

    public static void removeCargoRocketRecipe(INasaWorkbenchRecipe recipe)
    {
        GalacticraftRegistry.cargoRocketRecipes.remove(recipe);
    }

    public static void removeMoonBuggyRecipe(INasaWorkbenchRecipe recipe)
    {
        GalacticraftRegistry.buggyBenchRecipes.remove(recipe);
    }

    public static void removeAstroMinerRecipe(INasaWorkbenchRecipe recipe)
    {
        GalacticraftRegistry.astroMinerRecipes.remove(recipe);
    }

    public static void removeAllT1RocketRecipes()
    {
        GalacticraftRegistry.rocketBenchT1Recipes.clear();
    }

    public static void removeAllT2RocketRecipes()
    {
        GalacticraftRegistry.rocketBenchT2Recipes.clear();
    }

    public static void removeAllT3RocketRecipes()
    {
        GalacticraftRegistry.rocketBenchT3Recipes.clear();
    }

    public static void removeAllCargoRocketRecipes()
    {
        GalacticraftRegistry.cargoRocketRecipes.clear();
    }

    public static void removeAllMoonBuggyRecipes()
    {
        GalacticraftRegistry.buggyBenchRecipes.clear();
    }

    public static void removeAllAstroMinerRecipes()
    {
        GalacticraftRegistry.astroMinerRecipes.clear();
    }

    public static ITeleportType getTeleportTypeForDimension(Class<? extends WorldProvider> clazz)
    {
        if (!IGalacticraftWorldProvider.class.isAssignableFrom(clazz))
        {
            clazz = WorldProviderSurface.class;
        }
        return GalacticraftRegistry.teleportTypeMap.get(clazz);
    }

    public static void registerSpaceStation(SpaceStationType type)
    {
        for (SpaceStationType type1 : GalacticraftRegistry.spaceStations)
        {
            if (type1.getWorldToOrbitID() == type.getWorldToOrbitID())
            {
                throw new RuntimeException("Two space station types registered with the same home planet ID: " + type.getWorldToOrbitID());
            }
        }

        GalacticraftRegistry.spaceStations.add(type);
    }

    public static void replaceSpaceStationRecipe(int spaceStationID, HashMap<Object, Integer> obj)
    {
        for (SpaceStationType type1 : GalacticraftRegistry.spaceStations)
        {
            if (type1.getSpaceStationID() == spaceStationID)
            {
                type1.setRecipeForSpaceStation(new SpaceStationRecipe(obj));
            }
        }
    }

    public SpaceStationType getTypeFromPlanetID(int planetID)
    {
        return GalacticraftRegistry.spaceStations.get(planetID);
    }

    public static List<SpaceStationType> getSpaceStationData()
    {
        return GalacticraftRegistry.spaceStations;
    }

    public static List<INasaWorkbenchRecipe> getRocketT1Recipes()
    {
        return GalacticraftRegistry.rocketBenchT1Recipes;
    }

    public static List<INasaWorkbenchRecipe> getRocketT2Recipes()
    {
        return GalacticraftRegistry.rocketBenchT2Recipes;
    }

    public static List<INasaWorkbenchRecipe> getRocketT3Recipes()
    {
        return GalacticraftRegistry.rocketBenchT3Recipes;
    }

    public static List<INasaWorkbenchRecipe> getCargoRocketRecipes()
    {
        return GalacticraftRegistry.cargoRocketRecipes;
    }

    public static List<INasaWorkbenchRecipe> getBuggyBenchRecipes()
    {
        return GalacticraftRegistry.buggyBenchRecipes;
    }

    public static List<INasaWorkbenchRecipe> getAstroMinerRecipes()
    {
        return GalacticraftRegistry.astroMinerRecipes;
    }

    @SideOnly(Side.CLIENT)
    public static ResourceLocation getResouceLocationForDimension(Class<? extends WorldProvider> clazz)
    {
        if (!IGalacticraftWorldProvider.class.isAssignableFrom(clazz))
        {
            clazz = WorldProviderSurface.class;
        }
        return GalacticraftRegistry.rocketGuiMap.get(clazz);
    }

    public static List<ItemStack> getDungeonLoot(int tier)
    {
        return GalacticraftRegistry.dungeonLootMap.get(tier);
    }

    /***
     * Register a Galacticraft dimension
     */
    public static DimensionType registerDimension(String name, String suffix, int id, Class<? extends WorldProvider> provider, boolean keepLoaded) throws IllegalArgumentException
    {
        for (DimensionType other : DimensionType.values())
        {
            if (other.getId() == id)
            {
                return null;
            }
        }

        DimensionType type = DimensionType.register(name, suffix, id, provider, keepLoaded);
        GalacticraftRegistry.dimensionTypeIDs.add(type == null ? 0 : id);
        if (type == null)
        {
            GalacticraftCore.logger.error("Problem registering dimension type " + id + ".  May be fixable by changing config.");
        }

        return type;
    }

    public static int getDimensionTypeID(int index)
    {
        return GalacticraftRegistry.dimensionTypeIDs.get(index);
    }

    public static boolean isDimensionTypeIDRegistered(int typeId)
    {
        return GalacticraftRegistry.dimensionTypeIDs.contains(typeId);
    }

    /**
     * Register an IGameScreen so the Display Screen can access it
     * 
     * @param screen The IGameScreen to be registered
     * @return The type ID assigned to this screen type
     */
    public static int registerScreen(IGameScreen screen)
    {
        GalacticraftRegistry.gameScreens.add(screen);
        maxScreenTypes++;
        screen.setFrameSize(0.098F);
        return maxScreenTypes - 1;
    }

    public static void registerScreensServer(int maxTypes)
    {
        maxScreenTypes = maxTypes;
    }

    public static int getMaxScreenTypes()
    {
        return maxScreenTypes;
    }

    public static IGameScreen getGameScreen(int type)
    {
        return GalacticraftRegistry.gameScreens.get(type);
    }

    /**
     * Adds a custom item for 'extended inventory' slots
     *
     * Gear IDs must be unique, and should be configurable for user convenience
     *
     * Please do not use values less than 100, to avoid conflicts with future
     * Galacticraft core additions
     *
     * @param gearID Unique ID for this gear item, please use values greater
     *        than 100
     * @param type Slot this item can be placed in
     * @param item Item to register, not metadata-sensitive
     */
    public static void registerGear(int gearID, EnumExtendedInventorySlot type, Item item)
    {
        addGearObject(gearID, type, item);
    }

    /**
     * Adds a custom item for 'extended inventory' slots
     *
     * Gear IDs must be unique, and should be configurable for user convenience
     *
     * Please do not use values less than 100, to avoid conflicts with future
     * Galacticraft core additions
     *
     * @param gearID Unique ID for this gear item, please use values greater
     *        than 100
     * @param type Slot this item can be placed in
     * @param itemStack ItemStack to register, metadata-sensitive
     */
    public static void registerGear(int gearID, EnumExtendedInventorySlot type, ItemStack itemStack)
    {
        addGearObject(gearID, type, itemStack);
    }

    private static void addGearObject(int gearID, EnumExtendedInventorySlot type, Object obj)
    {
        if (GalacticraftRegistry.gearMap.containsKey(gearID))
        {
            if (!GalacticraftRegistry.gearMap.get(gearID).contains(obj))
            {
                GalacticraftRegistry.gearMap.get(gearID).add(obj);
            }
        } else
        {
            List<Object> gear = Lists.newArrayList();
            gear.add(obj);
            GalacticraftRegistry.gearMap.put(gearID, gear);
        }

        if (GalacticraftRegistry.gearSlotMap.containsKey(gearID))
        {
            if (!GalacticraftRegistry.gearSlotMap.get(gearID).contains(type))
            {
                GalacticraftRegistry.gearSlotMap.get(gearID).add(type);
            }
        } else
        {
            List<EnumExtendedInventorySlot> gearType = Lists.newArrayList();
            gearType.add(type);
            GalacticraftRegistry.gearSlotMap.put(gearID, gearType);
        }
    }

    public static List<ItemStack> listAllGearForSlot(EnumExtendedInventorySlot slotType)
    {
        List<ItemStack> result = new LinkedList<>();
        for (Map.Entry<Integer, List<Object>> entry : GalacticraftRegistry.gearMap.entrySet())
        {
            List<EnumExtendedInventorySlot> slotType1 = getSlotType(entry.getKey());
            if (slotType1.contains(slotType))
            {
                List<Object> objectList = entry.getValue();
                for (Object o : objectList)
                {
                    if (o instanceof ItemStack)
                    {
                        result.add((ItemStack) o);
                    } else if (o instanceof Item)
                    {
                        result.add(new ItemStack((Item) o));
                    }
                }
            }
        }
        return result;
    }

    public static int findMatchingGearID(ItemStack stack, EnumExtendedInventorySlot slotType)
    {
        for (Map.Entry<Integer, List<Object>> entry : GalacticraftRegistry.gearMap.entrySet())
        {
            List<EnumExtendedInventorySlot> slotType1 = getSlotType(entry.getKey());
            List<Object> objectList = entry.getValue();

            if (!slotType1.contains(slotType))
            {
                continue;
            }

            for (Object o : objectList)
            {
                if (o instanceof Item)
                {
                    if (stack.getItem() == o)
                    {
                        return entry.getKey();
                    }
                } else if (o instanceof ItemStack)
                {
                    if (stack.getItem() == ((ItemStack) o).getItem() && stack.getItemDamage() == ((ItemStack) o).getItemDamage())
                    {
                        return entry.getKey();
                    }
                }
            }
        }

        return -1;
    }

    public static List<EnumExtendedInventorySlot> getSlotType(int gearID)
    {
        return GalacticraftRegistry.gearSlotMap.get(gearID);
    }

    @Deprecated
    /**
     * Grating will now register fluids automatically if they extend
     * BlockFluidBase
     * 
     */
    public static void registerGratingFluid(Block fluidBlock)
    {
    }
}
