/*
 * Copyright (c) 2022 Team Galacticraft
 *
 * Licensed under the MIT license.
 * See LICENSE file in the project root for details.
 */

package micdoodle8.mods.galacticraft.core;

import static micdoodle8.mods.galacticraft.core.proxy.ClientProxyCore.PLAYER_Y_OFFSET;
import static micdoodle8.mods.galacticraft.core.proxy.ClientProxyCore.submergedTextures;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import micdoodle8.mods.galacticraft.api.entity.IAntiGrav;
import micdoodle8.mods.galacticraft.api.entity.ICameraZoomEntity;
import micdoodle8.mods.galacticraft.api.item.IArmorGravity;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntitySpaceshipBase;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.api.world.IOrbitDimension;
import micdoodle8.mods.galacticraft.api.world.IWeatherProvider;
import micdoodle8.mods.galacticraft.api.world.IZeroGDimension;
import micdoodle8.mods.galacticraft.core.blocks.BlockGrating;
import micdoodle8.mods.galacticraft.core.client.BubbleRenderer;
import micdoodle8.mods.galacticraft.core.client.FootprintRenderer;
import micdoodle8.mods.galacticraft.core.client.SkyProviderOverworld;
import micdoodle8.mods.galacticraft.core.dimension.WorldProviderMoon;
import micdoodle8.mods.galacticraft.core.dimension.WorldProviderSpaceStation;
import micdoodle8.mods.galacticraft.core.entities.player.EnumGravity;
import micdoodle8.mods.galacticraft.core.entities.player.GCPlayerStats;
import micdoodle8.mods.galacticraft.core.entities.player.GCPlayerStatsClient;
import micdoodle8.mods.galacticraft.core.proxy.ClientProxyCore;
import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;
import micdoodle8.mods.galacticraft.core.util.FluidUtil;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import micdoodle8.mods.galacticraft.core.util.OxygenUtil;
import micdoodle8.mods.galacticraft.core.util.WorldUtil;
import micdoodle8.mods.galacticraft.planets.venus.VenusItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * These methods are called from vanilla minecraft through bytecode injection done in MicdoodleCore See https://github.com/micdoodle8/MicdoodleCore
 */
public class TransformerHooks
{

    private static List<IWorldGenerator>     otherModGeneratorsWhitelist = new LinkedList<>();
    private static IWorldGenerator           generatorTCAuraNodes        = null;
    private static Method                    generateTCAuraNodes         = null;
    private static boolean                   generatorsInitialised       = false;
    public static List<Block>                spawnListAE2_GC             = new LinkedList<>();
    public static ThreadLocal<BufferBuilder> renderBuilder               = new ThreadLocal<>();
    private static int                       rainSoundCounter            = 0;
    private static Random                    random                      = new Random();

    public static double getGravityForEntity(Entity entity)
    {
        if (entity.world.provider instanceof IGalacticraftWorldProvider)
        {
            if (entity instanceof EntityChicken && !OxygenUtil.isAABBInBreathableAirBlock(entity.world, entity.getEntityBoundingBox()))
            {
                return 0.08D;
            }

            final IGalacticraftWorldProvider customProvider = (IGalacticraftWorldProvider) entity.world.provider;
            if (entity instanceof EntityPlayer)
            {
                EntityPlayer player = (EntityPlayer) entity;
                if (player.inventory != null)
                {
                    int armorModLowGrav = 100;
                    int armorModHighGrav = 100;
                    for (ItemStack armorPiece : player.getArmorInventoryList())
                    {
                        if (armorPiece != null && armorPiece.getItem() instanceof IArmorGravity)
                        {
                            armorModLowGrav -= ((IArmorGravity) armorPiece.getItem()).gravityOverrideIfLow(player);
                            armorModHighGrav -= ((IArmorGravity) armorPiece.getItem()).gravityOverrideIfHigh(player);
                        }
                    }
                    if (armorModLowGrav > 100)
                    {
                        armorModLowGrav = 100;
                    }
                    if (armorModHighGrav > 100)
                    {
                        armorModHighGrav = 100;
                    }
                    if (armorModLowGrav < 0)
                    {
                        armorModLowGrav = 0;
                    }
                    if (armorModHighGrav < 0)
                    {
                        armorModHighGrav = 0;
                    }
                    if (customProvider.getGravity() > 0)
                    {
                        return 0.08D - (customProvider.getGravity() * armorModLowGrav) / 100;
                    }
                    return 0.08D - (customProvider.getGravity() * armorModHighGrav) / 100;
                }
            }
            return 0.08D - customProvider.getGravity();
        } else if (entity instanceof IAntiGrav)
        {
            return 0;
        } else
        {
            return 0.08D;
        }
    }

    public static double getItemGravity(EntityItem e)
    {
        if (e.world.provider instanceof IGalacticraftWorldProvider)
        {
            final IGalacticraftWorldProvider customProvider = (IGalacticraftWorldProvider) e.world.provider;
            return Math.max(0.002D, 0.03999999910593033D - (customProvider instanceof IOrbitDimension ? 0.05999999910593033D : customProvider.getGravity()) / 1.75D);
        } else
        {
            return 0.03999999910593033D;
        }
    }

    public static float getArrowGravity(EntityArrow e)
    {
        if (e.world.provider instanceof IGalacticraftWorldProvider)
        {
            return ((IGalacticraftWorldProvider) e.world.provider).getArrowGravity();
        } else
        {
            return 0.05F;
        }
    }

    public static float getRainStrength(World world, float partialTicks)
    {
        if (world.isRemote)
        {
            if (world.provider.getSkyRenderer() instanceof SkyProviderOverworld)
            {
                return 0.0F;
            }
        }

        return world.prevRainingStrength + (world.rainingStrength - world.prevRainingStrength) * partialTicks;
    }

    public static void otherModGenerate(int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider)
    {
        if (world.provider instanceof WorldProviderSpaceStation)
        {
            return;
        }

        if (!(world.provider instanceof IGalacticraftWorldProvider) || ConfigManagerCore.enableOtherModsFeatures)
        {
            try
            {
                net.minecraftforge.fml.common.registry.GameRegistry.generateWorld(chunkX, chunkZ, world, chunkGenerator, chunkProvider);
            } catch (Exception e)
            {
                GalacticraftCore.logger.error("Error in another mod's worldgen.  This is *NOT* a Galacticraft bug, report it to the other mod please.");
                GalacticraftCore.logger.error("Details:- Dimension:" + GCCoreUtil.getDimensionID(world) + "  Chunk cx,cz:" + chunkX + "," + chunkZ + "  Seed:" + world.getSeed());
                e.printStackTrace();
            }
            return;
        }

        if (!generatorsInitialised)
        {
            generatorsInitialised = true;

            if (ConfigManagerCore.whitelistCoFHCoreGen)
            {
                addWorldGenForName("CoFHCore custom oregen", "cofh.cofhworld.init.WorldHandler");
            }
            addWorldGenForName("GalacticGreg oregen", "bloodasp.galacticgreg.GT_Worldgenerator_Space");
            addWorldGenForName("Dense Ores oregen", "com.rwtema.denseores.WorldGenOres");
            addWorldGenForName("AE2 meteorites worldgen", "appeng.worldgen.MeteoriteWorldGen");

            try
            {
                Class genThaumCraft = Class.forName("thaumcraft.common.lib.world.ThaumcraftWorldGenerator");
                if (genThaumCraft != null && ConfigManagerCore.enableThaumCraftNodes)
                {
                    final Field regField = GameRegistry.class.getDeclaredField("worldGenerators");
                    regField.setAccessible(true);
                    Set<IWorldGenerator> registeredGenerators = (Set<IWorldGenerator>) regField.get(null);
                    for (IWorldGenerator gen : registeredGenerators)
                    {
                        if (genThaumCraft.isInstance(gen))
                        {
                            generatorTCAuraNodes = gen;
                            break;
                        }
                    }
                    if (generatorTCAuraNodes != null)
                    {
                        generateTCAuraNodes = genThaumCraft.getDeclaredMethod("generateWildNodes", World.class, Random.class, int.class, int.class, boolean.class, boolean.class);
                        generateTCAuraNodes.setAccessible(true);
                        GalacticraftCore.logger.info("Whitelisting ThaumCraft aura node generation on planets.");
                    }
                }
            } catch (Exception e)
            {
            }
        }

        if (otherModGeneratorsWhitelist.size() > 0 || generateTCAuraNodes != null)
        {
            try
            {
                long worldSeed = world.getSeed();
                Random fmlRandom = new Random(worldSeed);
                long xSeed = fmlRandom.nextLong() >> 2 + 1L;
                long zSeed = fmlRandom.nextLong() >> 2 + 1L;
                long chunkSeed = (xSeed * chunkX + zSeed * chunkZ) ^ worldSeed;
                fmlRandom.setSeed(chunkSeed);

                for (IWorldGenerator gen : otherModGeneratorsWhitelist)
                {
                    gen.generate(fmlRandom, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
                }
                if (generateTCAuraNodes != null)
                {
                    generateTCAuraNodes.invoke(generatorTCAuraNodes, world, fmlRandom, chunkX, chunkZ, false, true);
                }
            } catch (Exception e)
            {
                GalacticraftCore.logger.error("Error in another mod's worldgen.  This is *NOT* a Galacticraft bug, report it to the other mod please.");
                e.printStackTrace();
            }
        }
    }

    private static void addWorldGenForName(String logString, String name)
    {
        try
        {
            Class target = Class.forName(name);
            if (target != null)
            {
                final Field regField = GameRegistry.class.getDeclaredField("worldGenerators");
                regField.setAccessible(true);
                Set<IWorldGenerator> registeredGenerators = (Set<IWorldGenerator>) regField.get(null);
                for (IWorldGenerator gen : registeredGenerators)
                {
                    if (target.isInstance(gen))
                    {
                        otherModGeneratorsWhitelist.add(gen);
                        GalacticraftCore.logger.info("Whitelisting " + logString + " on planets.");
                        return;
                    }
                }
            }
        } catch (Exception e)
        {
        }
    }

    /*
     * Used to supplement the hard-coded blocklist in AE2's MeteoritePlacer class
     */
    public static boolean addAE2MeteorSpawn(Object o, Block b)
    {
        if (o instanceof Collection<?>)
        {
            ((Collection<Block>) o).add(b);
            ((Collection<Block>) o).addAll(spawnListAE2_GC);
            return true;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    public static float getWorldBrightness(World world)
    {
        if (world.provider instanceof WorldProviderMoon)
        {
            float f1 = world.getCelestialAngle(1.0F);
            float f2 = 1.0F - (MathHelper.cos(f1 * Constants.twoPI) * 2.0F + 0.2F);

            if (f2 < 0.0F)
            {
                f2 = 0.0F;
            }

            if (f2 > 1.0F)
            {
                f2 = 1.0F;
            }

            f2 = 1.0F - f2;
            return f2 * 0.8F;
        }

        return world.getSunBrightness(1.0F);
    }

    @SideOnly(Side.CLIENT)
    public static float getColorRed(World world)
    {
        return (float) WorldUtil.getWorldColor(world).x;
    }

    @SideOnly(Side.CLIENT)
    public static float getColorGreen(World world)
    {
        return (float) WorldUtil.getWorldColor(world).y;
    }

    @SideOnly(Side.CLIENT)
    public static float getColorBlue(World world)
    {
        return (float) WorldUtil.getWorldColor(world).z;
    }

    @SideOnly(Side.CLIENT)
    public static Vec3d getFogColorHook(World world)
    {
        EntityPlayerSP player = FMLClientHandler.instance().getClient().player;
        if (world.provider.getSkyRenderer() instanceof SkyProviderOverworld)
        {
            float var20 = ((float) (player.posY) - Constants.OVERWORLD_SKYPROVIDER_STARTHEIGHT) / 1000.0F;
            var20 = MathHelper.sqrt(var20);
            final float var21 = Math.max(1.0F - var20 * 40.0F, 0.0F);

            Vec3d vec = world.getFogColor(1.0F);

            return new Vec3d(vec.x * Math.max(1.0F - var20 * 1.29F, 0.0F), vec.y * Math.max(1.0F - var20 * 1.29F, 0.0F), vec.z * Math.max(1.0F - var20 * 1.29F, 0.0F));
        }

        return world.getFogColor(1.0F);
    }

    @SideOnly(Side.CLIENT)
    public static Vec3d getSkyColorHook(World world)
    {
        EntityPlayerSP player = FMLClientHandler.instance().getClient().player;
        if (world.provider.getSkyRenderer() instanceof SkyProviderOverworld || (player != null && player.posY > Constants.OVERWORLD_CLOUD_HEIGHT && player.getRidingEntity() instanceof EntitySpaceshipBase))
        {
            float f1 = world.getCelestialAngle(1.0F);
            float f2 = MathHelper.cos(f1 * Constants.twoPI) * 2.0F + 0.5F;

            if (f2 < 0.0F)
            {
                f2 = 0.0F;
            }

            if (f2 > 1.0F)
            {
                f2 = 1.0F;
            }

            int i = MathHelper.floor(player.posX);
            int j = MathHelper.floor(player.posY);
            int k = MathHelper.floor(player.posZ);
            BlockPos pos = new BlockPos(i, j, k);
            int l = ForgeHooksClient.getSkyBlendColour(world, pos);
            float f4 = (l >> 16 & 255) / 255.0F;
            float f5 = (l >> 8 & 255) / 255.0F;
            float f6 = (l & 255) / 255.0F;
            f4 *= f2;
            f5 *= f2;
            f6 *= f2;

            if (player.posY <= Constants.OVERWORLD_SKYPROVIDER_STARTHEIGHT)
            {
                Vec3d vec = world.getSkyColor(FMLClientHandler.instance().getClient().getRenderViewEntity(), 1.0F);
                double blend = (player.posY - Constants.OVERWORLD_CLOUD_HEIGHT) / (Constants.OVERWORLD_SKYPROVIDER_STARTHEIGHT - Constants.OVERWORLD_CLOUD_HEIGHT);
                double ablend = 1 - blend;
                return new Vec3d(f4 * blend + vec.x * ablend, f5 * blend + vec.y * ablend, f6 * blend + vec.z * ablend);
            } else
            {
                double blend = Math.min(1.0D, (player.posY - Constants.OVERWORLD_SKYPROVIDER_STARTHEIGHT) / 300.0D);
                double ablend = 1.0D - blend;
                blend /= 255.0D;
                return new Vec3d(f4 * ablend + blend * 31.0D, f5 * ablend + blend * 8.0D, f6 * ablend + blend * 99.0D);
            }
        }

        return world.getSkyColor(FMLClientHandler.instance().getClient().getRenderViewEntity(), 1.0F);
    }

    @SideOnly(Side.CLIENT)
    public static double getRenderPosY(Entity viewEntity, double regular)
    {
        if (viewEntity.posY >= 256)
        {
            return 255.0F;
        } else
        {
            return regular + viewEntity.getEyeHeight();
        }
    }

    @SideOnly(Side.CLIENT)
    public static boolean shouldRenderFire(Entity entity)
    {
        if (entity.world == null || !(entity.world.provider instanceof IGalacticraftWorldProvider))
        {
            return entity.isBurning();
        }

        if (!(entity instanceof EntityLivingBase) && !(entity instanceof EntityArrow))
        {
            return entity.isBurning();
        }

        if (entity.isBurning())
        {
            if (OxygenUtil.noAtmosphericCombustion(entity.world.provider))
            {
                return OxygenUtil.isAABBInBreathableAirBlock(entity.world, entity.getEntityBoundingBox());
            } else
            {
                return true;
            }
            // Disable fire on Galacticraft worlds with no oxygen
        }

        return false;
    }

    @SideOnly(Side.CLIENT)
    public static void orientCamera(float partialTicks)
    {
        EntityPlayerSP player = ClientProxyCore.mc.player;
        GCPlayerStatsClient stats = GCPlayerStatsClient.get(player);

        Entity viewEntity = ClientProxyCore.mc.getRenderViewEntity();

        if (player.getRidingEntity() instanceof ICameraZoomEntity && ClientProxyCore.mc.gameSettings.thirdPersonView == 0)
        {
            Entity entity = player.getRidingEntity();
            float offset = ((ICameraZoomEntity) entity).getRotateOffset();
            if (offset > -10F)
            {
                offset += PLAYER_Y_OFFSET;
                GL11.glTranslatef(0, -offset, 0);
                float anglePitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
                float angleYaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
                GL11.glRotatef(-anglePitch, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(angleYaw, 0.0F, 1.0F, 0.0F);

                GL11.glTranslatef(0, offset, 0);
            }
        }

        if (viewEntity instanceof EntityLivingBase && viewEntity.world.provider instanceof IZeroGDimension && !((EntityLivingBase) viewEntity).isPlayerSleeping())
        {
            float pitch = viewEntity.prevRotationPitch + (viewEntity.rotationPitch - viewEntity.prevRotationPitch) * partialTicks;
            float yaw = viewEntity.prevRotationYaw + (viewEntity.rotationYaw - viewEntity.prevRotationYaw) * partialTicks + 180.0F;
            float eyeHeightChange = viewEntity.width / 2.0F;

            GL11.glRotatef(-yaw, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-pitch, 1.0F, 0.0F, 0.0F);
            GL11.glTranslatef(0.0F, 0.0F, 0.1F);

            EnumGravity gDir = stats.getGdir();
            GL11.glRotatef(180.0F * gDir.getThetaX(), 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(180.0F * gDir.getThetaZ(), 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(pitch * gDir.getPitchGravityX(), 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(pitch * gDir.getPitchGravityY(), 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(yaw * gDir.getYawGravityX(), 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(yaw * gDir.getYawGravityY(), 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(yaw * gDir.getYawGravityZ(), 0.0F, 0.0F, 1.0F);

            GL11.glTranslatef(eyeHeightChange * gDir.getEyeVecX(), eyeHeightChange * gDir.getEyeVecY(), eyeHeightChange * gDir.getEyeVecZ());

            if (stats.getGravityTurnRate() < 1.0F)
            {
                GL11.glRotatef(90.0F * (stats.getGravityTurnRatePrev() + (stats.getGravityTurnRate() - stats.getGravityTurnRatePrev()) * partialTicks), stats.getGravityTurnVecX(), stats.getGravityTurnVecY(), stats.getGravityTurnVecZ());
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static void renderLiquidOverlays(float partialTicks)
    {
        boolean within = false;
        for (Map.Entry<Fluid, ResourceLocation> entry : submergedTextures.entrySet())
        {
            if (FluidUtil.isInsideOfFluid(ClientProxyCore.mc.player, entry.getKey()))
            {
                within = true;
                ClientProxyCore.mc.getTextureManager().bindTexture(entry.getValue());
                break;
            }
        }

        if (!within)
        {
            return;
        }

        Tessellator tessellator = Tessellator.getInstance();
        float f1 = ClientProxyCore.mc.player.getBrightness() / 3.0F;
        GL11.glColor4f(f1, f1, f1, 1.0F);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glPushMatrix();
        float f2 = 4.0F;
        float f3 = -1.1F;
        float f4 = 1.1F;
        float f5 = -1.1F;
        float f6 = 1.1F;
        float f7 = -0.25F;
        float f8 = -ClientProxyCore.mc.player.rotationYaw / 64.0F;
        float f9 = ClientProxyCore.mc.player.rotationPitch / 64.0F;
        BufferBuilder worldRenderer = tessellator.getBuffer();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos(f3, f5, f7).tex(f2 + f8, f2 + f9).endVertex();
        worldRenderer.pos(f4, f5, f7).tex(0.0F + f8, f2 + f9).endVertex();
        worldRenderer.pos(f4, f6, f7).tex(0.0F + f8, 0.0F + f9).endVertex();
        worldRenderer.pos(f3, f6, f7).tex(f2 + f8, 0.0F + f9).endVertex();
        tessellator.draw();
        GL11.glPopMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_BLEND);
    }

    @SideOnly(Side.CLIENT)
    public static void renderFootprints(float partialTicks)
    {
        FootprintRenderer.renderFootprints(ClientProxyCore.mc.player, partialTicks);
        MinecraftForge.EVENT_BUS.post(new ClientProxyCore.EventSpecialRender(partialTicks));
        BubbleRenderer.renderBubbles(ClientProxyCore.mc.player, partialTicks);
    }

    @SideOnly(Side.CLIENT)
    public static double getCameraZoom(double previous)
    {
        if (ConfigManagerCore.disableVehicleCameraChanges)
        {
            return previous;
        }

        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player.getRidingEntity() != null && player.getRidingEntity() instanceof ICameraZoomEntity)
        {
            return ((ICameraZoomEntity) player.getRidingEntity()).getCameraZoom();
        }

        return previous;
    }

    public static double armorDamageHook(EntityLivingBase entity)
    {
        if (entity instanceof EntityPlayer && GalacticraftCore.isPlanetsLoaded)
        {
            GCPlayerStats stats = GCPlayerStats.get(entity);
            if (stats != null)
            {
                ItemStack shield = stats.getShieldControllerInSlot();
                if (shield != null && !shield.isEmpty() && shield.getItem() == VenusItems.basicItem && shield.getItemDamage() == 0)
                {
                    return 0D;
                }
            }
        }
        return 1D;
    }

    public static void setCurrentBuffer(BufferBuilder buffer)
    {
        renderBuilder.set(buffer);
    }

    public static boolean isGrating(boolean orig, Block b)
    {
        return orig || (b instanceof BlockGrating && b != GCBlocks.grating && MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT);
    }

    public static float armorDamageHookF(EntityLivingBase entity)
    {
        return (float) armorDamageHook(entity);
    }

    @SideOnly(Side.CLIENT)
    public static int addRainParticles(int result, int rendererUpdateCount, float f)
    {
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.world;
        if (result == 0 || !(world.provider instanceof IWeatherProvider))
        {
            // Either no rain or it's a vanilla dimension
            return result;
        }
        IWeatherProvider moddedProvider = ((IWeatherProvider) world.provider);

        random.setSeed(rendererUpdateCount * 312987231L);
        Entity entity = mc.getRenderViewEntity();
        BlockPos blockpos = new BlockPos(entity);
        int i = 10;
        double x = 0.0D;
        double y = 0.0D;
        double z = 0.0D;
        double xx = 0.0D;
        double yy = 0.0D;
        double zz = 0.0D;
        int j = 0;
        int k = (int) (100.0F * f * f);

        if (mc.gameSettings.particleSetting == 1)
        {
            k >>= 1;
        } else if (mc.gameSettings.particleSetting == 2)
        {
            k = 0;
        }

        for (int l = 0; l < k; ++l)
        {
            BlockPos blockpos1 = world.getPrecipitationHeight(blockpos.add(random.nextInt(i) - random.nextInt(i), 0, random.nextInt(i) - random.nextInt(i)));
            Biome biome = world.getBiome(blockpos1);

            if (blockpos1.getY() <= blockpos.getY() + i && blockpos1.getY() >= blockpos.getY() - i)
            {
                double xd = random.nextDouble();
                double zd = random.nextDouble();
                BlockPos blockpos2 = blockpos1.down();
                IBlockState iblockstate = world.getBlockState(blockpos2);
                AxisAlignedBB axisalignedbb = iblockstate.getBoundingBox(world, blockpos2);

                if (iblockstate.getMaterial() != Material.LAVA && iblockstate.getBlock() != Blocks.MAGMA)
                {
                    if (iblockstate.getMaterial() != Material.AIR)
                    {
                        ++j;

                        x = blockpos2.getX() + xd;
                        y = blockpos2.getY() + 0.1D + axisalignedbb.maxY;
                        z = blockpos2.getZ() + zd;
                        if (random.nextInt(j) == 0)
                        {
                            xx = x;
                            yy = y - 1.0D;
                            zz = z;
                        }

                        mc.effectRenderer.addEffect(moddedProvider.getParticle(mc.world, x, y, z));
                    }
                } else
                {
                    mc.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, blockpos1.getX() + xd, blockpos1.getY() + 0.1D - axisalignedbb.minY, blockpos1.getZ() + zd, 0.0D, 0.0D, 0.0D, new int[0]);
                }
            }
        }

        if (moddedProvider.getSoundInterval(f) > 0)
        {
            if (j > 0 && random.nextInt(moddedProvider.getSoundInterval(f)) < rainSoundCounter++)
            {
                rainSoundCounter = 0;

                moddedProvider.weatherSounds(j, mc, world, blockpos, xx, yy, zz, random);
            }
        } else if (j > 0 && 0 < rainSoundCounter++)
        {
            rainSoundCounter = 0;

            moddedProvider.weatherSounds(j, mc, world, blockpos, xx, yy, zz, random);
        }

        // Bypass vanilla code after returning from this
        return 0;
    }
}
