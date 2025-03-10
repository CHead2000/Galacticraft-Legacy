/*
 * Copyright (c) 2022 Team Galacticraft
 *
 * Licensed under the MIT license.
 * See LICENSE file in the project root for details.
 */

package micdoodle8.mods.galacticraft.planets.mars.client.jei.methanesynth;

import java.util.List;
import javax.annotation.Nonnull;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import micdoodle8.mods.galacticraft.core.client.jei.RecipeCategories;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import micdoodle8.mods.galacticraft.planets.GalacticraftPlanets;
import micdoodle8.mods.galacticraft.planets.asteroids.items.AsteroidsItems;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class MethaneSynthRecipeCategory implements IRecipeCategory
{

    private static final ResourceLocation refineryGuiTex = new ResourceLocation(GalacticraftPlanets.ASSET_PREFIX, "textures/gui/methane_synthesizer_recipe.png");
    private static final ResourceLocation gasesTex = new ResourceLocation(GalacticraftPlanets.ASSET_PREFIX, "textures/gui/gases_methane_oxygen_nitrogen.png");

    @Nonnull private final IDrawable background;
    @Nonnull private final String localizedName;
    @Nonnull private final IDrawableAnimated hydrogenBarInput;
    @Nonnull private final IDrawableAnimated carbonDioxideBarInput;
    @Nonnull private final IDrawableAnimated methaneBarOutput;

    boolean fillAtmos = false;

    public MethaneSynthRecipeCategory(IGuiHelper guiHelper)
    {
        this.background = guiHelper.createDrawable(refineryGuiTex, 3, 4, 168, 66);
        this.localizedName = GCCoreUtil.translate("tile.mars_machine.5.name");

        IDrawableStatic hydrogenBar = guiHelper.createDrawable(gasesTex, 35, 0, 16, 38);
        this.hydrogenBarInput = guiHelper.createAnimatedDrawable(hydrogenBar, 70, IDrawableAnimated.StartDirection.TOP, true);
        IDrawableStatic carbonDioxideBar = guiHelper.createDrawable(gasesTex, 35, 0, 16, 20);
        this.carbonDioxideBarInput = guiHelper.createAnimatedDrawable(carbonDioxideBar, 70, IDrawableAnimated.StartDirection.TOP, true);
        IDrawableStatic methaneBar = guiHelper.createDrawable(gasesTex, 1, 0, 16, 38);
        this.methaneBarOutput = guiHelper.createAnimatedDrawable(methaneBar, 70, IDrawableAnimated.StartDirection.BOTTOM, false);
    }

    @Nonnull
    @Override
    public String getUid()
    {
        return RecipeCategories.METHANE_SYNTHESIZER_ID;
    }

    @Nonnull
    @Override
    public String getTitle()
    {
        return this.localizedName;
    }

    @Nonnull
    @Override
    public IDrawable getBackground()
    {
        return this.background;
    }

    @Override
    public void drawExtras(@Nonnull Minecraft minecraft)
    {
        if (this.fillAtmos)
        {
            this.hydrogenBarInput.draw(minecraft, 29, 24);
            this.carbonDioxideBarInput.draw(minecraft, 50, 24);
        } else
        {
            this.hydrogenBarInput.draw(minecraft, 29, 24);
        }
        this.methaneBarOutput.draw(minecraft, 114, 24);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients)
    {
        IGuiItemStackGroup itemstacks = recipeLayout.getItemStacks();

        itemstacks.init(0, true, 28, 2);
        itemstacks.init(1, true, 49, 2);
        itemstacks.init(2, true, 49, 48);
        itemstacks.init(3, false, 113, 2);

        if (recipeWrapper instanceof MethaneSynthRecipeWrapper)
        {
            MethaneSynthRecipeWrapper gasLiquefierRecipeWrapper = (MethaneSynthRecipeWrapper) recipeWrapper;
            List<ItemStack> input = ingredients.getInputs(ItemStack.class).get(0);

            Item inputItem = input.get(0).getItem();
            if (inputItem == AsteroidsItems.atmosphericValve)
            {
                this.fillAtmos = true;
                itemstacks.set(1, input);
            } else
            {
                this.fillAtmos = false;
                itemstacks.set(2, input);
            }

            itemstacks.set(3, ingredients.getOutputs(ItemStack.class).get(0));
        }
    }

    @Override
    public String getModName()
    {
        return GalacticraftPlanets.NAME;
    }
}
