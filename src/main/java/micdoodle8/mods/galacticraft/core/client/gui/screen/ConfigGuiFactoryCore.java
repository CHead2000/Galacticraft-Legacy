/*
 * Copyright (c) 2022 Team Galacticraft
 *
 * Licensed under the MIT license.
 * See LICENSE file in the project root for details.
 */

package micdoodle8.mods.galacticraft.core.client.gui.screen;

import java.util.Set;
import micdoodle8.mods.galacticraft.core.Constants;
import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;

public class ConfigGuiFactoryCore implements IModGuiFactory
{

    public static class CoreConfigGUI extends GuiConfig
    {

        public CoreConfigGUI(GuiScreen parent)
        {
            super(parent, ConfigManagerCore.getConfigElements(), Constants.MOD_ID_CORE, false, false, GCCoreUtil.translate("gc.configgui.title"));
        }
    }

    @Override
    public void initialize(Minecraft minecraftInstance)
    {

    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories()
    {
        return null;
    }

    public GuiScreen createConfigGui(GuiScreen arg0)
    {
        // TODO Forge 2282 addition!
        return new CoreConfigGUI(arg0);
    }

    public boolean hasConfigGui()
    {
        return true;
    }
}
