/*
 * Copyright (c) 2022 Team Galacticraft
 *
 * Licensed under the MIT license.
 * See LICENSE file in the project root for details.
 */

package micdoodle8.mods.galacticraft.core.network;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialObject;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.Satellite;
import micdoodle8.mods.galacticraft.api.item.EnumExtendedInventorySlot;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntitySpaceshipBase;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntitySpaceshipBase.EnumLaunchPhase;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityTieredRocket;
import micdoodle8.mods.galacticraft.api.recipe.ISchematicPage;
import micdoodle8.mods.galacticraft.api.recipe.SchematicRegistry;
import micdoodle8.mods.galacticraft.api.tile.IDisableableMachine;
import micdoodle8.mods.galacticraft.api.tile.ITileClientUpdates;
import micdoodle8.mods.galacticraft.api.transmission.tile.INetworkProvider;
import micdoodle8.mods.galacticraft.api.vector.BlockVec3;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.core.Constants;
import micdoodle8.mods.galacticraft.core.GCItems;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.advancement.GCTriggers;
import micdoodle8.mods.galacticraft.core.blocks.BlockPanelLighting;
import micdoodle8.mods.galacticraft.core.blocks.BlockSpaceGlass;
import micdoodle8.mods.galacticraft.core.client.FootprintRenderer;
import micdoodle8.mods.galacticraft.core.client.fx.ParticleSparks;
import micdoodle8.mods.galacticraft.core.client.gui.GuiIdsCore;
import micdoodle8.mods.galacticraft.core.client.gui.container.GuiBuggy;
import micdoodle8.mods.galacticraft.core.client.gui.container.GuiParaChest;
import micdoodle8.mods.galacticraft.core.client.gui.screen.GuiCelestialSelection;
import micdoodle8.mods.galacticraft.core.client.sounds.GCSounds;
import micdoodle8.mods.galacticraft.core.command.CommandGCEnergyUnits;
import micdoodle8.mods.galacticraft.core.dimension.SpaceRace;
import micdoodle8.mods.galacticraft.core.dimension.SpaceRaceManager;
import micdoodle8.mods.galacticraft.core.dimension.SpaceStationWorldData;
import micdoodle8.mods.galacticraft.core.dimension.WorldProviderSpaceStation;
import micdoodle8.mods.galacticraft.core.energy.tile.TileBaseConductor;
import micdoodle8.mods.galacticraft.core.entities.EntityBuggy;
import micdoodle8.mods.galacticraft.core.entities.EntityHangingSchematic;
import micdoodle8.mods.galacticraft.core.entities.IBubbleProvider;
import micdoodle8.mods.galacticraft.core.entities.IControllableEntity;
import micdoodle8.mods.galacticraft.core.entities.player.GCEntityPlayerMP;
import micdoodle8.mods.galacticraft.core.entities.player.GCPlayerHandler;
import micdoodle8.mods.galacticraft.core.entities.player.GCPlayerStats;
import micdoodle8.mods.galacticraft.core.entities.player.GCPlayerStatsClient;
import micdoodle8.mods.galacticraft.core.fluid.FluidNetwork;
import micdoodle8.mods.galacticraft.core.inventory.ContainerSchematic;
import micdoodle8.mods.galacticraft.core.inventory.IInventorySettable;
import micdoodle8.mods.galacticraft.core.items.ItemParaChute;
import micdoodle8.mods.galacticraft.core.proxy.ClientProxyCore;
import micdoodle8.mods.galacticraft.core.tick.KeyHandlerClient;
import micdoodle8.mods.galacticraft.core.tick.TickHandlerClient;
import micdoodle8.mods.galacticraft.core.tick.TickHandlerServer;
import micdoodle8.mods.galacticraft.core.tile.TileEntityAirLockController;
import micdoodle8.mods.galacticraft.core.tile.TileEntityFluidPipe;
import micdoodle8.mods.galacticraft.core.tile.TileEntityOxygenSealer;
import micdoodle8.mods.galacticraft.core.tile.TileEntityTelemetry;
import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;
import micdoodle8.mods.galacticraft.core.util.EnumColor;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import micdoodle8.mods.galacticraft.core.util.MapUtil;
import micdoodle8.mods.galacticraft.core.util.PlayerUtil;
import micdoodle8.mods.galacticraft.core.util.WorldUtil;
import micdoodle8.mods.galacticraft.core.wrappers.FlagData;
import micdoodle8.mods.galacticraft.core.wrappers.Footprint;
import micdoodle8.mods.galacticraft.core.wrappers.PlayerGearData;
import micdoodle8.mods.galacticraft.core.wrappers.ScheduledDimensionChange;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketSimple extends PacketBase implements Packet<INetHandler>
{

    public enum EnumSimplePacket
    {

        // SERVER
        S_RESPAWN_PLAYER(Side.SERVER, String.class),
        S_TELEPORT_ENTITY(Side.SERVER, Integer.class),
        S_IGNITE_ROCKET(Side.SERVER),
        S_OPEN_SCHEMATIC_PAGE(Side.SERVER, Integer.class, Integer.class, Integer.class, Integer.class),
        S_OPEN_FUEL_GUI(Side.SERVER, String.class),
        S_UPDATE_SHIP_YAW(Side.SERVER, Float.class),
        S_UPDATE_SHIP_PITCH(Side.SERVER, Float.class),
        S_SET_ENTITY_FIRE(Side.SERVER, Integer.class),
        S_BIND_SPACE_STATION_ID(Side.SERVER, Integer.class),
        S_UNLOCK_NEW_SCHEMATIC(Side.SERVER),
        S_UPDATE_DISABLEABLE_BUTTON(Side.SERVER, BlockPos.class, Integer.class),
        S_ON_FAILED_CHEST_UNLOCK(Side.SERVER, Integer.class),
        S_RENAME_SPACE_STATION(Side.SERVER, String.class, Integer.class),
        S_OPEN_EXTENDED_INVENTORY(Side.SERVER),
        S_ON_ADVANCED_GUI_CLICKED_INT(Side.SERVER, Integer.class, BlockPos.class, Integer.class),
        S_ON_ADVANCED_GUI_CLICKED_STRING(Side.SERVER, Integer.class, BlockPos.class, String.class),
        S_UPDATE_SHIP_MOTION_Y(Side.SERVER, Integer.class, Boolean.class),
        S_START_NEW_SPACE_RACE(Side.SERVER, Integer.class, String.class, FlagData.class, Vector3.class, String[].class),
        S_REQUEST_FLAG_DATA(Side.SERVER, String.class),
        S_INVITE_RACE_PLAYER(Side.SERVER, String.class, Integer.class),
        S_REMOVE_RACE_PLAYER(Side.SERVER, String.class, Integer.class),
        S_ADD_RACE_PLAYER(Side.SERVER, String.class, Integer.class),
        S_COMPLETE_CBODY_HANDSHAKE(Side.SERVER, String[].class),
        S_REQUEST_GEAR_DATA1(Side.SERVER, UUID.class),
        S_REQUEST_GEAR_DATA2(Side.SERVER, UUID.class),
        S_REQUEST_OVERWORLD_IMAGE(Side.SERVER),
        S_REQUEST_MAP_IMAGE(Side.SERVER, Integer.class, Integer.class, Integer.class),
        S_REQUEST_PLAYERSKIN(Side.SERVER, String.class),
        S_BUILDFLAGS_UPDATE(Side.SERVER, Integer.class),
        S_CONTROL_ENTITY(Side.SERVER, Integer.class),
        S_NOCLIP_PLAYER(Side.SERVER, Boolean.class),
        S_REQUEST_DATA(Side.SERVER, Integer.class, BlockPos.class),
        S_UPDATE_CHECKLIST(Side.SERVER, NBTTagCompound.class),
        S_REQUEST_MACHINE_DATA(Side.SERVER, BlockPos.class),
        S_REQUEST_CONTAINER_SLOT_REFRESH(Side.SERVER, Integer.class),
        // CLIENT
        C_AIR_REMAINING(Side.CLIENT, Integer.class, Integer.class, String.class),
        C_UPDATE_DIMENSION_LIST(Side.CLIENT, String.class, String.class, Boolean.class),
        C_SPAWN_SPARK_PARTICLES(Side.CLIENT, BlockPos.class),
        C_UPDATE_GEAR_SLOT(Side.CLIENT, String.class, Integer.class, Integer.class, Integer.class),
        C_CLOSE_GUI(Side.CLIENT),
        C_RESET_THIRD_PERSON(Side.CLIENT),
        C_UPDATE_SPACESTATION_LIST(Side.CLIENT, Integer[].class),
        C_UPDATE_SPACESTATION_DATA(Side.CLIENT, Integer.class, NBTTagCompound.class),
        C_UPDATE_SPACESTATION_CLIENT_ID(Side.CLIENT, String.class),
        C_UPDATE_PLANETS_LIST(Side.CLIENT, Integer[].class),
        C_UPDATE_CONFIGS(
            Side.CLIENT,
            Integer.class,
            Double.class,
            Integer.class,
            Integer.class,
            Integer.class,
            String.class,
            Float.class,
            Float.class,
            Float.class,
            Float.class,
            Integer.class,
            String[].class),
        C_UPDATE_STATS(
            Side.CLIENT,
            Integer.class,
            String.class,
            Integer.class,
            String.class,
            Integer.class,
            String.class,
            Integer.class,
            String.class,
            Integer.class,
            String.class,
            Integer.class,
            Integer.class), // Note:
                            // Integer,
                            // PANELTYPES_LENGTH
                            // *
                            // <String,
                            // Integer>,
                            // Integer
                            // -
                            // see
                            // StatsCapability.getMiscNetworkedStats()
        C_ADD_NEW_SCHEMATIC(Side.CLIENT, Integer.class),
        C_UPDATE_SCHEMATIC_LIST(Side.CLIENT, Integer[].class),
        C_PLAY_SOUND_BOSS_DEATH(Side.CLIENT, Float.class),
        C_PLAY_SOUND_EXPLODE(Side.CLIENT),
        C_PLAY_SOUND_BOSS_LAUGH(Side.CLIENT),
        C_PLAY_SOUND_BOW(Side.CLIENT),
        C_UPDATE_OXYGEN_VALIDITY(Side.CLIENT, Boolean.class),
        C_OPEN_PARACHEST_GUI(Side.CLIENT, Integer.class, Integer.class, Integer.class),
        C_UPDATE_WIRE_BOUNDS(Side.CLIENT, BlockPos.class),
        C_OPEN_SPACE_RACE_GUI(Side.CLIENT),
        C_UPDATE_SPACE_RACE_DATA(Side.CLIENT, Integer.class, String.class, FlagData.class, Vector3.class, String[].class),
        C_OPEN_JOIN_RACE_GUI(Side.CLIENT, Integer.class),
        C_UPDATE_FOOTPRINT_LIST(Side.CLIENT, Long.class, Footprint[].class),
        C_UPDATE_DUNGEON_DIRECTION(Side.CLIENT, Float.class),
        C_FOOTPRINTS_REMOVED(Side.CLIENT, Long.class, BlockVec3.class),
        C_UPDATE_STATION_SPIN(Side.CLIENT, Float.class, Boolean.class),
        C_UPDATE_STATION_DATA(Side.CLIENT, Double.class, Double.class),
        C_UPDATE_STATION_BOX(Side.CLIENT, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class),
        C_UPDATE_THERMAL_LEVEL(Side.CLIENT, Integer.class, Boolean.class),
        C_DISPLAY_ROCKET_CONTROLS(Side.CLIENT),
        C_GET_CELESTIAL_BODY_LIST(Side.CLIENT),
        C_UPDATE_ENERGYUNITS(Side.CLIENT, Integer.class),
        C_RESPAWN_PLAYER(Side.CLIENT, String.class, Integer.class, String.class, Integer.class),
        C_UPDATE_TELEMETRY(Side.CLIENT, BlockPos.class, String.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, String.class),
        C_SEND_PLAYERSKIN(Side.CLIENT, String.class, String.class, String.class, String.class),
        C_SEND_OVERWORLD_IMAGE(Side.CLIENT, Integer.class, Integer.class, byte[].class),
        C_RECOLOR_PIPE(Side.CLIENT, BlockPos.class),
        C_RECOLOR_ALL_GLASS(Side.CLIENT, Integer.class, Integer.class, Integer.class), // Number
                                                                                       // of
                                                                                       // integers
                                                                                       // to
                                                                                       // match
                                                                                       // number
                                                                                       // of
                                                                                       // different
                                                                                       // blocks
                                                                                       // of
                                                                                       // PLAIN
                                                                                       // glass
                                                                                       // individually
                                                                                       // instanced
                                                                                       // and
                                                                                       // registered
                                                                                       // in
                                                                                       // GCBlocks
        C_UPDATE_MACHINE_DATA(Side.CLIENT, BlockPos.class, Integer.class, Integer.class, Integer.class, Integer.class),
        C_SPAWN_HANGING_SCHEMATIC(Side.CLIENT, BlockPos.class, Integer.class, Integer.class, Integer.class),
        C_LEAK_DATA(Side.CLIENT, BlockPos.class, Integer[].class);

        private Side targetSide;
        private Class<?>[] decodeAs;

        EnumSimplePacket(Side targetSide, Class<?>... decodeAs)
        {
            this.targetSide = targetSide;
            this.decodeAs = decodeAs;
        }

        public Side getTargetSide()
        {
            return this.targetSide;
        }

        public Class<?>[] getDecodeClasses()
        {
            return this.decodeAs;
        }
    }

    private EnumSimplePacket type;
    private List<Object> data;
    static private String spamCheckString;
    static private Map<EntityPlayerMP, GameType> savedSettings = new HashMap<>();

    public PacketSimple()
    {
        super();
    }

    public PacketSimple(EnumSimplePacket packetType, int dimID, Object[] data)
    {
        this(packetType, dimID, Arrays.asList(data));
    }

    public PacketSimple(EnumSimplePacket packetType, World world, Object[] data)
    {
        this(packetType, GCCoreUtil.getDimensionID(world), Arrays.asList(data));
    }

    public PacketSimple(EnumSimplePacket packetType, int dimID, List<Object> data)
    {
        super(dimID);
        if (packetType.getDecodeClasses().length != data.size())
        {
            GalacticraftCore.logger.info("Simple Packet Core found data length different than packet type");
            new RuntimeException().printStackTrace();
        }

        this.type = packetType;
        this.data = data;
    }

    @Override
    public void encodeInto(ByteBuf buffer)
    {
        super.encodeInto(buffer);
        buffer.writeInt(this.type.ordinal());

        try
        {
            NetworkUtil.encodeData(buffer, this.data);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void decodeInto(ByteBuf buffer)
    {
        super.decodeInto(buffer);
        this.type = EnumSimplePacket.values()[buffer.readInt()];

        try
        {
            if (this.type.getDecodeClasses().length > 0)
            {
                this.data = NetworkUtil.decodeData(this.type.getDecodeClasses(), buffer);
            }
            if (buffer.readableBytes() > 0 && buffer.writerIndex() < 0xfff00)
            {
                GalacticraftCore.logger.error("Galacticraft packet length problem for packet type " + this.type.toString());
            }
        } catch (Exception e)
        {
            System.err.println("[Galacticraft] Error handling simple packet type: " + this.type.toString() + " " + buffer.toString());
            e.printStackTrace();
            throw e;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleClientSide(EntityPlayer player)
    {
        EntityPlayerSP playerBaseClient = null;
        GCPlayerStatsClient stats = null;

        if (player instanceof EntityPlayerSP)
        {
            playerBaseClient = (EntityPlayerSP) player;
            stats = GCPlayerStatsClient.get(playerBaseClient);
        } else
        {
            if (type != EnumSimplePacket.C_UPDATE_SPACESTATION_LIST && type != EnumSimplePacket.C_UPDATE_PLANETS_LIST && type != EnumSimplePacket.C_UPDATE_CONFIGS)
            {
                return;
            }
        }

        switch (this.type)
        {
            case C_AIR_REMAINING:
                if (String.valueOf(this.data.get(2)).equals(String.valueOf(PlayerUtil.getName(player))))
                {
                    TickHandlerClient.airRemaining = (Integer) this.data.get(0);
                    TickHandlerClient.airRemaining2 = (Integer) this.data.get(1);
                }
                break;
            case C_UPDATE_DIMENSION_LIST:
                if (String.valueOf(this.data.get(0)).equals(PlayerUtil.getName(player)))
                {
                    String dimensionList = (String) this.data.get(1);
                    if (ConfigManagerCore.enableDebug)
                    {
                        if (!dimensionList.equals(PacketSimple.spamCheckString))
                        {
                            GalacticraftCore.logger.info("DEBUG info: " + dimensionList);
                            PacketSimple.spamCheckString = dimensionList;
                        }
                    }
                    final String[] destinations = dimensionList.split("\\?");
                    List<CelestialBody> possibleCelestialBodies = Lists.newArrayList();
                    Map<Integer, Map<String, GuiCelestialSelection.StationDataGUI>> spaceStationData = Maps.newHashMap();
//                Map<String, String> spaceStationNames = Maps.newHashMap();
//                Map<String, Integer> spaceStationIDs = Maps.newHashMap();
//                Map<String, Integer> spaceStationHomes = Maps.newHashMap();

                    for (String str : destinations)
                    {
                        CelestialBody celestialBody = WorldUtil.getReachableCelestialBodiesForName(str);

                        if (celestialBody == null && str.contains("$"))
                        {
                            String[] values = str.split("\\$");

                            int homePlanetID = Integer.parseInt(values[4]);

                            for (Satellite satellite : GalaxyRegistry.getRegisteredSatellites().values())
                            {
                                if (satellite.getParentPlanet().getDimensionID() == homePlanetID)
                                {
                                    celestialBody = satellite;
                                    break;
                                }
                            }

                            if (!spaceStationData.containsKey(homePlanetID))
                            {
                                spaceStationData.put(homePlanetID, new HashMap<String, GuiCelestialSelection.StationDataGUI>());
                            }

                            spaceStationData.get(homePlanetID).put(values[1], new GuiCelestialSelection.StationDataGUI(values[2], Integer.parseInt(values[3])));

//                        spaceStationNames.put(values[1], values[2]);
//                        spaceStationIDs.put(values[1], Integer.parseInt(values[3]));
//                        spaceStationHomes.put(values[1], Integer.parseInt(values[4]));
                        }

                        if (celestialBody != null)
                        {
                            possibleCelestialBodies.add(celestialBody);
                        }
                    }

                    if (FMLClientHandler.instance().getClient().world != null)
                    {
                        if (!(FMLClientHandler.instance().getClient().currentScreen instanceof GuiCelestialSelection))
                        {
                            GuiCelestialSelection gui = new GuiCelestialSelection(false, possibleCelestialBodies, (Boolean) this.data.get(2));
                            gui.spaceStationMap = spaceStationData;
//                        gui.spaceStationNames = spaceStationNames;
//                        gui.spaceStationIDs = spaceStationIDs;
                            FMLClientHandler.instance().getClient().displayGuiScreen(gui);
                        } else
                        {
                            ((GuiCelestialSelection) FMLClientHandler.instance().getClient().currentScreen).possibleBodies = possibleCelestialBodies;
                            ((GuiCelestialSelection) FMLClientHandler.instance().getClient().currentScreen).spaceStationMap = spaceStationData;
//                        ((GuiCelestialSelection) FMLClientHandler.instance().getClient().currentScreen).spaceStationNames = spaceStationNames;
//                        ((GuiCelestialSelection) FMLClientHandler.instance().getClient().currentScreen).spaceStationIDs = spaceStationIDs;
                        }
                    }
                }
                break;
            case C_SPAWN_SPARK_PARTICLES:
                BlockPos pos = (BlockPos) this.data.get(0);
                Minecraft mc = Minecraft.getMinecraft();

                for (int i = 0; i < 4; i++)
                {
                    if (mc.getRenderViewEntity() != null && mc.effectRenderer != null && mc.world != null)
                    {
                        final ParticleSparks fx = new ParticleSparks(mc.world, pos.getX() - 0.15 + 0.5, pos.getY() + 1.2, pos.getZ() + 0.15 + 0.5,
                            mc.world.rand.nextDouble() / 20 - mc.world.rand.nextDouble() / 20, mc.world.rand.nextDouble() / 20 - mc.world.rand.nextDouble() / 20);

                        mc.effectRenderer.addEffect(fx);
                    }
                }
                break;
            case C_UPDATE_GEAR_SLOT:
                int subtype = (Integer) this.data.get(3);
                String gearName = (String) this.data.get(0);

                EntityPlayer gearDataPlayer = player.world.getPlayerEntityByName(gearName);

                if (gearDataPlayer != null)
                {
                    PlayerGearData gearData = ClientProxyCore.playerItemData.get(PlayerUtil.getName(gearDataPlayer));
                    UUID gearDataPlayerID = gearDataPlayer.getUniqueID();

                    if (gearData == null)
                    {
                        gearData = new PlayerGearData(player);
                        if (!ClientProxyCore.gearDataRequests.contains(gearDataPlayerID))
                        {
                            GalacticraftCore.packetPipeline.sendToServer(new PacketSimple(PacketSimple.EnumSimplePacket.S_REQUEST_GEAR_DATA1, getDimensionID(), new Object[]
                            {gearDataPlayerID}));
                            ClientProxyCore.gearDataRequests.add(gearDataPlayerID);
                        }
                    } else
                    {
                        ClientProxyCore.gearDataRequests.remove(gearDataPlayerID);
                    }

                    EnumExtendedInventorySlot type = EnumExtendedInventorySlot.values()[(Integer) this.data.get(2)];
                    GCPlayerHandler.EnumModelPacketType typeChange = GCPlayerHandler.EnumModelPacketType.values()[(Integer) this.data.get(1)];

                    switch (type)
                    {
                        case MASK:
                            gearData.setMask(subtype);
                            break;
                        case GEAR:
                            gearData.setGear(subtype);
                            break;
                        case LEFT_TANK:
                            gearData.setLeftTank(subtype);
                            break;
                        case RIGHT_TANK:
                            gearData.setRightTank(subtype);
                            break;
                        case PARACHUTE:
                            if (typeChange == GCPlayerHandler.EnumModelPacketType.ADD)
                            {
                                String name;

                                if (subtype != -1)
                                {
                                    name = ItemParaChute.names[subtype];
                                    gearData.setParachute(new ResourceLocation(Constants.ASSET_PREFIX, "textures/model/parachute/" + name + ".png"));
                                }
                            } else
                            {
                                gearData.setParachute(null);
                            }
                            break;
                        case FREQUENCY_MODULE:
                            gearData.setFrequencyModule(subtype);
                            break;
                        case THERMAL_HELMET:
                            gearData.setThermalPadding(0, subtype);
                            break;
                        case THERMAL_CHESTPLATE:
                            gearData.setThermalPadding(1, subtype);
                            break;
                        case THERMAL_LEGGINGS:
                            gearData.setThermalPadding(2, subtype);
                            break;
                        case THERMAL_BOOTS:
                            gearData.setThermalPadding(3, subtype);
                            break;
                        case SHIELD_CONTROLLER:
                            gearData.setShieldController(subtype);
                            break;
                        default:
                            break;
                    }

                    ClientProxyCore.playerItemData.put(gearName, gearData);
                }

                break;
            case C_CLOSE_GUI:
                FMLClientHandler.instance().getClient().displayGuiScreen(null);
                break;
            case C_RESET_THIRD_PERSON:
                FMLClientHandler.instance().getClient().gameSettings.thirdPersonView = stats.getThirdPersonView();
                break;
            case C_UPDATE_SPACESTATION_LIST:
                WorldUtil.decodeSpaceStationListClient(data);
                break;
            case C_UPDATE_SPACESTATION_DATA:
                SpaceStationWorldData var4 = SpaceStationWorldData.getMPSpaceStationData(player.world, (Integer) this.data.get(0), player);
                var4.readFromNBT((NBTTagCompound) this.data.get(1));
                break;
            case C_UPDATE_SPACESTATION_CLIENT_ID:
                ClientProxyCore.clientSpaceStationID = WorldUtil.stringToSpaceStationData((String) this.data.get(0));
                break;
            case C_UPDATE_PLANETS_LIST:
                WorldUtil.decodePlanetsListClient(data);
                break;
            case C_UPDATE_CONFIGS:
                ConfigManagerCore.saveClientConfigOverrideable();
                ConfigManagerCore.setConfigOverride(data);
                break;
            case C_ADD_NEW_SCHEMATIC:
                final ISchematicPage page = SchematicRegistry.getMatchingRecipeForID((Integer) this.data.get(0));
                if (!stats.getUnlockedSchematics().contains(page))
                {
                    stats.getUnlockedSchematics().add(page);
                }
                break;
            case C_UPDATE_SCHEMATIC_LIST:
                for (Object o : this.data)
                {
                    Integer schematicID = (Integer) o;

                    if (schematicID != -2)
                    {
                        Collections.sort(stats.getUnlockedSchematics());

                        if (!stats.getUnlockedSchematics().contains(SchematicRegistry.getMatchingRecipeForID(schematicID)))
                        {
                            stats.getUnlockedSchematics().add(SchematicRegistry.getMatchingRecipeForID(schematicID));
                        }
                    }
                }
                break;
            case C_PLAY_SOUND_BOSS_DEATH:
                player.playSound(GCSounds.bossDeath, 10.0F, (Float) this.data.get(0));
                break;
            case C_PLAY_SOUND_EXPLODE:
                player.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 10.0F, 0.7F);
                break;
            case C_PLAY_SOUND_BOSS_LAUGH:
                player.playSound(GCSounds.bossLaugh, 10.0F, 0.2F);
                break;
            case C_PLAY_SOUND_BOW:
                player.playSound(SoundEvents.ENTITY_ARROW_SHOOT, 10.0F, 0.2F);
                break;
            case C_UPDATE_OXYGEN_VALIDITY:
                stats.setOxygenSetupValid((Boolean) this.data.get(0));
                break;
            case C_OPEN_PARACHEST_GUI:
                switch ((Integer) this.data.get(1))
                {
                    case 0:
                        if (player.getRidingEntity() instanceof EntityBuggy)
                        {
                            FMLClientHandler.instance().getClient()
                                .displayGuiScreen(new GuiBuggy(player.inventory, (EntityBuggy) player.getRidingEntity(), ((EntityBuggy) player.getRidingEntity()).getType()));
                            player.openContainer.windowId = (Integer) this.data.get(0);
                        }
                        break;
                    case 1:
                        int entityID = (Integer) this.data.get(2);
                        Entity entity = player.world.getEntityByID(entityID);

                        if (entity != null && entity instanceof IInventorySettable)
                        {
                            FMLClientHandler.instance().getClient().displayGuiScreen(new GuiParaChest(player.inventory, (IInventorySettable) entity));
                        }

                        player.openContainer.windowId = (Integer) this.data.get(0);
                        break;
                }
                GalacticraftCore.packetPipeline.sendToServer(new PacketSimple(PacketSimple.EnumSimplePacket.S_REQUEST_CONTAINER_SLOT_REFRESH, GCCoreUtil.getDimensionID(player.world), new Object[]
                {player.openContainer.windowId}));
                break;
            case C_UPDATE_WIRE_BOUNDS:
                TileEntity tile = player.world.getTileEntity((BlockPos) this.data.get(0));

                if (tile instanceof TileBaseConductor)
                {
                    ((TileBaseConductor) tile).adjacentConnections = null;
                }
                break;
            case C_OPEN_SPACE_RACE_GUI:
                if (Minecraft.getMinecraft().currentScreen == null)
                {
                    TickHandlerClient.spaceRaceGuiScheduled = false;
                    player.openGui(GalacticraftCore.instance, GuiIdsCore.SPACE_RACE_START, player.world, (int) player.posX, (int) player.posY, (int) player.posZ);
                } else
                {
                    TickHandlerClient.spaceRaceGuiScheduled = true;
                }
                break;
            case C_UPDATE_SPACE_RACE_DATA:
                Integer teamID = (Integer) this.data.get(0);
                String teamName = (String) this.data.get(1);
                FlagData flagData = (FlagData) this.data.get(2);
                Vector3 teamColor = (Vector3) this.data.get(3);
                List<String> playerList = new ArrayList<>();

                for (int i = 4; i < this.data.size(); i++)
                {
                    String playerName = (String) this.data.get(i);
                    ClientProxyCore.flagRequestsSent.remove(playerName);
                    playerList.add(playerName);
                }

                SpaceRace race = new SpaceRace(playerList, teamName, flagData, teamColor);
                race.setSpaceRaceID(teamID);
                SpaceRaceManager.addSpaceRace(race);
                break;
            case C_OPEN_JOIN_RACE_GUI:
                stats.setSpaceRaceInviteTeamID((Integer) this.data.get(0));
                player.openGui(GalacticraftCore.instance, GuiIdsCore.SPACE_RACE_JOIN, player.world, (int) player.posX, (int) player.posY, (int) player.posZ);
                break;
            case C_UPDATE_DUNGEON_DIRECTION:
                stats.setDungeonDirection((Float) this.data.get(0));
                break;
            case C_UPDATE_FOOTPRINT_LIST:
                List<Footprint> printList = new ArrayList<>();
                long chunkKey = (Long) this.data.get(0);
                for (int i = 1; i < this.data.size(); i++)
                {
                    Footprint print = (Footprint) this.data.get(i);
                    if (!print.owner.equals(player.getName()))
                    {
                        printList.add(print);
                    }
                }
                FootprintRenderer.setFootprints(chunkKey, printList);
                break;
            case C_FOOTPRINTS_REMOVED:
                long chunkKey0 = (Long) this.data.get(0);
                BlockVec3 position = (BlockVec3) this.data.get(1);
                List<Footprint> footprintList = FootprintRenderer.footprints.get(chunkKey0);
                List<Footprint> toRemove = new ArrayList<>();

                if (footprintList != null)
                {
                    for (Footprint footprint : footprintList)
                    {
                        if (footprint.position.x > position.x && footprint.position.x < position.x + 1 && footprint.position.z > position.z && footprint.position.z < position.z + 1)
                        {
                            toRemove.add(footprint);
                        }
                    }
                }

                if (!toRemove.isEmpty())
                {
                    footprintList.removeAll(toRemove);
                    FootprintRenderer.footprints.put(chunkKey0, footprintList);
                }
                break;
            case C_UPDATE_STATION_SPIN:
                if (playerBaseClient.world.provider instanceof WorldProviderSpaceStation)
                {
                    ((WorldProviderSpaceStation) playerBaseClient.world.provider).getSpinManager().setSpinRate((Float) this.data.get(0), (Boolean) this.data.get(1));
                }
                break;
            case C_UPDATE_STATION_DATA:
                if (playerBaseClient.world.provider instanceof WorldProviderSpaceStation)
                {
                    ((WorldProviderSpaceStation) playerBaseClient.world.provider).getSpinManager().setSpinCentre((Double) this.data.get(0), (Double) this.data.get(1));
                }
                break;
            case C_UPDATE_STATION_BOX:
                if (playerBaseClient.world.provider instanceof WorldProviderSpaceStation)
                {
                    ((WorldProviderSpaceStation) playerBaseClient.world.provider).getSpinManager().setSpinBox((Integer) this.data.get(0), (Integer) this.data.get(1), (Integer) this.data.get(2),
                        (Integer) this.data.get(3), (Integer) this.data.get(4), (Integer) this.data.get(5));
                }
                break;
            case C_UPDATE_THERMAL_LEVEL:
                stats.setThermalLevel((Integer) this.data.get(0));
                stats.setThermalLevelNormalising((Boolean) this.data.get(1));
                break;
            case C_DISPLAY_ROCKET_CONTROLS:
                player.sendMessage(new TextComponentString(GameSettings.getKeyDisplayString(KeyHandlerClient.spaceKey.getKeyCode()) + "  - " + GCCoreUtil.translate("gui.rocket.launch.name")));
                player.sendMessage(new TextComponentString(GameSettings.getKeyDisplayString(KeyHandlerClient.leftKey.getKeyCode()) + " / "
                    + GameSettings.getKeyDisplayString(KeyHandlerClient.rightKey.getKeyCode()) + "  - " + GCCoreUtil.translate("gui.rocket.turn.name")));
                player.sendMessage(new TextComponentString(GameSettings.getKeyDisplayString(KeyHandlerClient.accelerateKey.getKeyCode()) + " / "
                    + GameSettings.getKeyDisplayString(KeyHandlerClient.decelerateKey.getKeyCode()) + "  - " + GCCoreUtil.translate("gui.rocket.updown.name")));
                player.sendMessage(new TextComponentString(GameSettings.getKeyDisplayString(KeyHandlerClient.openFuelGui.getKeyCode()) + "       - " + GCCoreUtil.translate("gui.rocket.inv.name")));
                break;
            case C_GET_CELESTIAL_BODY_LIST:
                String[] objKeys = GalaxyRegistry.getAllTransltionKeys();
                GalacticraftCore.packetPipeline.sendToServer(new PacketSimple(EnumSimplePacket.S_COMPLETE_CBODY_HANDSHAKE, getDimensionID(), new Object[] {objKeys}));
                break;
            case C_UPDATE_ENERGYUNITS:
                CommandGCEnergyUnits.handleParamClientside((Integer) this.data.get(0));
                break;
            case C_RESPAWN_PLAYER:
                final WorldProvider provider = WorldUtil.getProviderForNameClient((String) this.data.get(0));
                final int dimID = GCCoreUtil.getDimensionID(provider);
                if (ConfigManagerCore.enableDebug)
                {
                    GalacticraftCore.logger.info("DEBUG: Client receiving respawn packet for dim " + dimID);
                }
                int par2 = (Integer) this.data.get(1);
                String par3 = (String) this.data.get(2);
                int par4 = (Integer) this.data.get(3);
                WorldUtil.forceRespawnClient(dimID, par2, par3, par4);
                break;
            case C_UPDATE_STATS:
                stats.setBuildFlags((Integer) this.data.get(0));
                BlockPanelLighting.updateClient(this.data);
                break;
            case C_UPDATE_TELEMETRY:
                tile = player.world.getTileEntity((BlockPos) this.data.get(0));
                if (tile instanceof TileEntityTelemetry)
                {
                    ((TileEntityTelemetry) tile).receiveUpdate(data, this.getDimensionID());
                }
                break;
            case C_SEND_PLAYERSKIN:
                String strName = (String) this.data.get(0);
                String s1 = (String) this.data.get(1);
                String s2 = (String) this.data.get(2);
                String strUUID = (String) this.data.get(3);
                GameProfile gp = PlayerUtil.getOtherPlayerProfile(strName);
                if (gp == null)
                {
                    gp = PlayerUtil.makeOtherPlayerProfile(strName, strUUID);
                }
                gp.getProperties().put("textures", new Property("textures", s1, s2));
                break;
            case C_SEND_OVERWORLD_IMAGE:
                try
                {
                    int cx = (Integer) this.data.get(0);
                    int cz = (Integer) this.data.get(1);
                    byte[] bytes = (byte[]) this.data.get(2);
                    MapUtil.receiveOverworldImageCompressed(cx, cz, bytes);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                break;
            case C_RECOLOR_PIPE:
                TileEntity tileEntity = player.world.getTileEntity((BlockPos) this.data.get(0));
                if (tileEntity instanceof TileEntityFluidPipe)
                {
                    TileEntityFluidPipe pipe = (TileEntityFluidPipe) tileEntity;
                    pipe.getNetwork().split(pipe);
                    pipe.setNetwork(null);
                }
                break;
            case C_RECOLOR_ALL_GLASS:
                BlockSpaceGlass.updateGlassColors((Integer) this.data.get(0), (Integer) this.data.get(1), (Integer) this.data.get(2));
                break;
            case C_UPDATE_MACHINE_DATA:
                TileEntity tile3 = player.world.getTileEntity((BlockPos) this.data.get(0));
                if (tile3 instanceof ITileClientUpdates)
                {
                    ((ITileClientUpdates) tile3).updateClient(this.data);
                }
                break;
            case C_LEAK_DATA:
                TileEntity tile4 = player.world.getTileEntity((BlockPos) this.data.get(0));
                if (tile4 instanceof TileEntityOxygenSealer)
                {
                    ((ITileClientUpdates) tile4).updateClient(this.data);
                }
                break;
            case C_SPAWN_HANGING_SCHEMATIC:
                EntityHangingSchematic entity = new EntityHangingSchematic(player.world, (BlockPos) this.data.get(0), EnumFacing.byIndex((Integer) this.data.get(2)), (Integer) this.data.get(3));
                ((WorldClient) player.world).addEntityToWorld((Integer) this.data.get(1), entity);
                break;
            default:
                break;
        }
    }

    @Override
    public void handleServerSide(EntityPlayer player)
    {
        final EntityPlayerMP playerBase = PlayerUtil.getPlayerBaseServerFromPlayer(player, false);

        if (playerBase == null)
        {
            return;
        }

        final MinecraftServer server = playerBase.server;
        final GCPlayerStats stats = GCPlayerStats.get(playerBase);

        switch (this.type)
        {
            case S_RESPAWN_PLAYER:
                playerBase.connection
                    .sendPacket(new SPacketRespawn(player.dimension, player.world.getDifficulty(), player.world.getWorldInfo().getTerrainType(), playerBase.interactionManager.getGameType()));
                break;
            case S_TELEPORT_ENTITY:
                TickHandlerServer.scheduleNewDimensionChange(new ScheduledDimensionChange(playerBase, (int) PacketSimple.this.data.get(0)));
                break;
            case S_IGNITE_ROCKET:
                if (!player.world.isRemote && !player.isDead && player.getRidingEntity() != null && !player.getRidingEntity().isDead && player.getRidingEntity() instanceof EntityTieredRocket)
                {
                    final EntityTieredRocket ship = (EntityTieredRocket) player.getRidingEntity();

                    if (ship.launchPhase != EnumLaunchPhase.LANDING.ordinal())
                    {
                        if (ship.hasValidFuel())
                        {
                            ItemStack stack2 = stats.getExtendedInventory().getStackInSlot(4);

                            if (stack2 != null && stack2.getItem() instanceof ItemParaChute || stats.getLaunchAttempts() > 0)
                            {
                                ship.igniteCheckingCooldown();
                                stats.setLaunchAttempts(0);
                            } else if (stats.getChatCooldown() == 0 && stats.getLaunchAttempts() == 0)
                            {
                                player.sendMessage(new TextComponentString(GCCoreUtil.translate("gui.rocket.warning.noparachute")));
                                stats.setChatCooldown(80);
                                stats.setLaunchAttempts(1);
                            }
                        } else if (stats.getChatCooldown() == 0)
                        {
                            player.sendMessage(new TextComponentString(GCCoreUtil.translate("gui.rocket.warning.nofuel")));
                            stats.setChatCooldown(250);
                        }
                    }
                }
                break;
            case S_OPEN_SCHEMATIC_PAGE:
                if (player != null)
                {
                    final ISchematicPage page = SchematicRegistry.getMatchingRecipeForID((Integer) this.data.get(0));

                    player.openGui(GalacticraftCore.instance, page.getGuiID(), player.world, (Integer) this.data.get(1), (Integer) this.data.get(2), (Integer) this.data.get(3));
                }
                break;
            case S_OPEN_FUEL_GUI:
                if (player.getRidingEntity() instanceof EntityBuggy)
                {
                    GCCoreUtil.openBuggyInv(playerBase, (EntityBuggy) player.getRidingEntity(), ((EntityBuggy) player.getRidingEntity()).getType());
                } else if (player.getRidingEntity() instanceof EntitySpaceshipBase)
                {
                    player.openGui(GalacticraftCore.instance, GuiIdsCore.ROCKET_INVENTORY, player.world, (int) player.posX, (int) player.posY, (int) player.posZ);
                }
                break;
            case S_UPDATE_SHIP_YAW:
                if (player.getRidingEntity() instanceof EntitySpaceshipBase)
                {
                    final EntitySpaceshipBase ship = (EntitySpaceshipBase) player.getRidingEntity();

                    if (ship != null)
                    {
                        ship.rotationYaw = (Float) this.data.get(0);
                    }
                }
                break;
            case S_UPDATE_SHIP_PITCH:
                if (player.getRidingEntity() instanceof EntitySpaceshipBase)
                {
                    final EntitySpaceshipBase ship = (EntitySpaceshipBase) player.getRidingEntity();

                    if (ship != null)
                    {
                        ship.rotationPitch = (Float) this.data.get(0);
                    }
                }
                break;
            case S_SET_ENTITY_FIRE:
                Entity entity = player.world.getEntityByID((Integer) this.data.get(0));

                if (entity instanceof EntityLivingBase)
                {
                    ((EntityLivingBase) entity).setFire(3);
                }
                break;
            case S_BIND_SPACE_STATION_ID:
                int homeID = (Integer) this.data.get(0);
                if ((!stats.getSpaceStationDimensionData().containsKey(homeID) || stats.getSpaceStationDimensionData().get(homeID) == -1 || stats.getSpaceStationDimensionData().get(homeID) == 0)
                    && !ConfigManagerCore.disableSpaceStationCreation)
                {
                    if (playerBase.capabilities.isCreativeMode || WorldUtil.getSpaceStationRecipe(homeID).matches(playerBase, true))
                    {
                        GCTriggers.CREATE_SPACE_STATION.trigger(playerBase);
                        WorldUtil.bindSpaceStationToNewDimension(playerBase.world, playerBase, homeID);
                    }
                }
                break;
            case S_UNLOCK_NEW_SCHEMATIC:
                final Container container = player.openContainer;

                if (container instanceof ContainerSchematic)
                {
                    final ContainerSchematic schematicContainer = (ContainerSchematic) container;

                    ItemStack stack = schematicContainer.craftMatrix.getStackInSlot(0);

                    if (!stack.isEmpty())
                    {
                        final ISchematicPage page = SchematicRegistry.getMatchingRecipeForItemStack(stack);

                        if (page != null)
                        {
                            SchematicRegistry.unlockNewPage(playerBase, stack);
                            SpaceRaceManager.teamUnlockSchematic(playerBase, stack);
                            stack.shrink(1);

                            schematicContainer.craftMatrix.setInventorySlotContents(0, stack);
                            schematicContainer.craftMatrix.markDirty();

                            GalacticraftCore.packetPipeline.sendTo(new PacketSimple(EnumSimplePacket.C_ADD_NEW_SCHEMATIC, getDimensionID(), new Object[]
                            {page.getPageID()}), playerBase);
                        }
                    }
                }
                break;
            case S_UPDATE_DISABLEABLE_BUTTON:
                final TileEntity tileAt = player.world.getTileEntity((BlockPos) this.data.get(0));

                if (tileAt instanceof IDisableableMachine)
                {
                    final IDisableableMachine machine = (IDisableableMachine) tileAt;

                    machine.setDisabled((Integer) this.data.get(1), !machine.getDisabled((Integer) this.data.get(1)));
                }
                break;
            case S_ON_FAILED_CHEST_UNLOCK:
                if (stats.getChatCooldown() == 0)
                {
                    player.sendMessage(new TextComponentString(GCCoreUtil.translateWithFormat("gui.chest.warning.wrongkey", this.data.get(0))));
                    stats.setChatCooldown(100);
                }
                break;
            case S_RENAME_SPACE_STATION:
                final SpaceStationWorldData ssdata = SpaceStationWorldData.getStationData(playerBase.world, (Integer) this.data.get(1), playerBase);

                if (ssdata != null && ssdata.getOwner().equalsIgnoreCase(PlayerUtil.getName(player)))
                {
                    ssdata.setSpaceStationName((String) this.data.get(0));
                    ssdata.setDirty(true);
                }
                break;
            case S_OPEN_EXTENDED_INVENTORY:
                player.openGui(GalacticraftCore.instance, GuiIdsCore.EXTENDED_INVENTORY, player.world, 0, 0, 0);
                break;
            case S_ON_ADVANCED_GUI_CLICKED_INT:
                TileEntity tile1 = player.world.getTileEntity((BlockPos) this.data.get(1));

                switch ((Integer) this.data.get(0))
                {
                    case 0:
                        if (tile1 instanceof TileEntityAirLockController)
                        {
                            TileEntityAirLockController airlockController = (TileEntityAirLockController) tile1;
                            airlockController.redstoneActivation = (Integer) this.data.get(2) == 1;
                        }
                        break;
                    case 1:
                        if (tile1 instanceof TileEntityAirLockController)
                        {
                            TileEntityAirLockController airlockController = (TileEntityAirLockController) tile1;
                            airlockController.playerDistanceActivation = (Integer) this.data.get(2) == 1;
                        }
                        break;
                    case 2:
                        if (tile1 instanceof TileEntityAirLockController)
                        {
                            TileEntityAirLockController airlockController = (TileEntityAirLockController) tile1;
                            airlockController.playerDistanceSelection = (Integer) this.data.get(2);
                        }
                        break;
                    case 3:
                        if (tile1 instanceof TileEntityAirLockController)
                        {
                            TileEntityAirLockController airlockController = (TileEntityAirLockController) tile1;
                            airlockController.playerNameMatches = (Integer) this.data.get(2) == 1;
                        }
                        break;
                    case 4:
                        if (tile1 instanceof TileEntityAirLockController)
                        {
                            TileEntityAirLockController airlockController = (TileEntityAirLockController) tile1;
                            airlockController.invertSelection = (Integer) this.data.get(2) == 1;
                        }
                        break;
                    case 5:
                        if (tile1 instanceof TileEntityAirLockController)
                        {
                            TileEntityAirLockController airlockController = (TileEntityAirLockController) tile1;
                            airlockController.lastHorizontalModeEnabled = airlockController.horizontalModeEnabled;
                            airlockController.horizontalModeEnabled = (Integer) this.data.get(2) == 1;
                        }
                        break;
                    case 6:
                        if (tile1 instanceof IBubbleProvider)
                        {
                            IBubbleProvider distributor = (IBubbleProvider) tile1;
                            distributor.setBubbleVisible((Integer) this.data.get(2) == 1);
                        }
                        break;
                    default:
                        break;
                }
                break;
            case S_ON_ADVANCED_GUI_CLICKED_STRING:
                TileEntity tile2 = player.world.getTileEntity((BlockPos) this.data.get(1));

                switch ((Integer) this.data.get(0))
                {
                    case 0:
                        if (tile2 instanceof TileEntityAirLockController)
                        {
                            TileEntityAirLockController airlockController = (TileEntityAirLockController) tile2;
                            airlockController.playerToOpenFor = (String) this.data.get(2);
                        }
                        break;
                    default:
                        break;
                }
                break;
            case S_UPDATE_SHIP_MOTION_Y:
                int entityID = (Integer) this.data.get(0);
                boolean up = (Boolean) this.data.get(1);

                Entity entity2 = player.world.getEntityByID(entityID);

                if (entity2 instanceof EntityAutoRocket)
                {
                    EntityAutoRocket autoRocket = (EntityAutoRocket) entity2;
                    autoRocket.motionY += up ? 0.02F : -0.02F;
                }

                break;
            case S_START_NEW_SPACE_RACE:
                Integer teamID = (Integer) this.data.get(0);
                String teamName = (String) this.data.get(1);
                FlagData flagData = (FlagData) this.data.get(2);
                Vector3 teamColor = (Vector3) this.data.get(3);
                List<String> playerList = new ArrayList<>();

                for (int i = 4; i < this.data.size(); i++)
                {
                    playerList.add((String) this.data.get(i));
                }

                boolean previousData = SpaceRaceManager.getSpaceRaceFromID(teamID) != null;

                SpaceRace newRace = new SpaceRace(playerList, teamName, flagData, teamColor);

                if (teamID > 0)
                {
                    newRace.setSpaceRaceID(teamID);
                }

                SpaceRaceManager.addSpaceRace(newRace);

                if (previousData)
                {
                    SpaceRaceManager.sendSpaceRaceData(server, null, SpaceRaceManager.getSpaceRaceFromPlayer(PlayerUtil.getName(playerBase)));
                }
                break;
            case S_REQUEST_FLAG_DATA:
                SpaceRaceManager.sendSpaceRaceData(server, playerBase, SpaceRaceManager.getSpaceRaceFromPlayer((String) this.data.get(0)));
                break;
            case S_INVITE_RACE_PLAYER:
                EntityPlayerMP playerInvited = PlayerUtil.getPlayerBaseServerFromPlayerUsername(server, (String) this.data.get(0), true);
                if (playerInvited != null)
                {
                    Integer teamInvitedTo = (Integer) this.data.get(1);
                    SpaceRace race = SpaceRaceManager.getSpaceRaceFromID(teamInvitedTo);

                    if (race != null)
                    {
                        GCPlayerStats.get(playerInvited).setSpaceRaceInviteTeamID(teamInvitedTo);
                        String dA = EnumColor.DARK_AQUA.getCode();
                        String bG = EnumColor.BRIGHT_GREEN.getCode();
                        String dB = EnumColor.PURPLE.getCode();
                        String teamNameTotal = "";
                        String[] teamNameSplit = race.getTeamName().split(" ");
                        for (String teamNamePart : teamNameSplit)
                        {
                            teamNameTotal = teamNameTotal.concat(dB + teamNamePart + " ");
                        }
                        playerInvited.sendMessage(new TextComponentString(dA + GCCoreUtil.translateWithFormat("gui.space_race.chat.invite_received", bG + PlayerUtil.getName(player) + dA) + "  "
                            + GCCoreUtil.translateWithFormat("gui.space_race.chat.to_join", teamNameTotal, EnumColor.AQUA + "/joinrace" + dA))
                                .setStyle(new Style().setColor(TextFormatting.DARK_AQUA)));
                    }
                }
                break;
            case S_REMOVE_RACE_PLAYER:
                Integer teamInvitedTo = (Integer) this.data.get(1);
                SpaceRace race = SpaceRaceManager.getSpaceRaceFromID(teamInvitedTo);

                if (race != null)
                {
                    String playerToRemove = (String) this.data.get(0);

                    if (!race.getPlayerNames().remove(playerToRemove))
                    {
                        player.sendMessage(new TextComponentString(GCCoreUtil.translateWithFormat("gui.space_race.chat.not_found", playerToRemove)));
                    } else
                    {
                        SpaceRaceManager.onPlayerRemoval(server, playerToRemove, race);
                    }
                }
                break;
            case S_ADD_RACE_PLAYER:
                Integer teamToAddPlayer = (Integer) this.data.get(1);
                SpaceRace spaceRaceToAddPlayer = SpaceRaceManager.getSpaceRaceFromID(teamToAddPlayer);

                if (spaceRaceToAddPlayer != null)
                {
                    String playerToAdd = (String) this.data.get(0);

                    if (!spaceRaceToAddPlayer.getPlayerNames().contains(playerToAdd))
                    {
                        SpaceRace oldRace = null;
                        while ((oldRace = SpaceRaceManager.getSpaceRaceFromPlayer(playerToAdd)) != null)
                        {
                            SpaceRaceManager.removeSpaceRace(oldRace);
                        }

                        spaceRaceToAddPlayer.getPlayerNames().add(playerToAdd);
                        SpaceRaceManager.sendSpaceRaceData(server, null, spaceRaceToAddPlayer);

                        for (String member : spaceRaceToAddPlayer.getPlayerNames())
                        {
                            EntityPlayerMP memberObj = PlayerUtil.getPlayerForUsernameVanilla(server, member);

                            if (memberObj != null)
                            {
                                memberObj.sendMessage(new TextComponentString(
                                    EnumColor.DARK_AQUA + GCCoreUtil.translateWithFormat("gui.space_race.chat.add_success", EnumColor.BRIGHT_GREEN + playerToAdd + EnumColor.DARK_AQUA))
                                        .setStyle(new Style().setColor(TextFormatting.DARK_AQUA)));
                            }
                        }
                    } else
                    {
                        player.sendMessage(new TextComponentString(GCCoreUtil.translate("gui.space_race.chat.already_part")).setStyle(new Style().setColor(TextFormatting.DARK_RED)));
                    }
                }
                break;
            case S_COMPLETE_CBODY_HANDSHAKE:
                List<CelestialObject> clientObjects = new ArrayList<>();
                for (int i = 0; i < this.data.size(); i++)
                {
                    clientObjects.add(GalaxyRegistry.getCelestialObjectFromTranslationKey((String) this.data.get(i)));
                }
                List<CelestialObject> serverObjects = GalaxyRegistry.getAllRegisteredObjects();
                String missingObjects = "";

                for (CelestialObject celestialObject : serverObjects)
                {
                    if (!clientObjects.contains(celestialObject))
                    {
                        missingObjects = missingObjects.concat(celestialObject.getName() + "\n");
                    }
                }

                if (missingObjects.length() > 0)
                {
                    playerBase.connection.disconnect(new TextComponentString("Missing Galacticraft Celestial Objects:\n\n " + missingObjects));
                }

                break;
            case S_REQUEST_GEAR_DATA1:
            case S_REQUEST_GEAR_DATA2:
                UUID id = (UUID) this.data.get(0);
                if (id != null)
                {
                    EntityPlayer otherPlayer = player.world.getPlayerEntityByUUID(id);
                    if (otherPlayer instanceof EntityPlayerMP)
                    {
                        GCPlayerHandler.checkGear((EntityPlayerMP) otherPlayer, GCPlayerStats.get(otherPlayer), true);
                    }
                }
                break;
            case S_BUILDFLAGS_UPDATE:
                stats.setBuildFlags((Integer) this.data.get(0));
                break;
            case S_REQUEST_OVERWORLD_IMAGE:
                MapUtil.sendOverworldToClient(playerBase);
                break;
            case S_REQUEST_MAP_IMAGE:
                int dim = (Integer) this.data.get(0);
                int cx = (Integer) this.data.get(1);
                int cz = (Integer) this.data.get(2);
                MapUtil.sendOrCreateMap(WorldUtil.getProviderForDimensionServer(dim).world, cx, cz, playerBase);
                break;
            case S_REQUEST_PLAYERSKIN:
                String strName = (String) this.data.get(0);
                EntityPlayerMP playerRequested = server.getPlayerList().getPlayerByUsername(strName);

                // Player not online
                if (playerRequested == null)
                {
                    return;
                }

                GameProfile gp = playerRequested.getGameProfile();
                if (gp == null)
                {
                    return;
                }

                Property property = (Property) Iterables.getFirst(gp.getProperties().get("textures"), (Object) null);
                if (property == null)
                {
                    return;
                }
                GalacticraftCore.packetPipeline.sendTo(new PacketSimple(EnumSimplePacket.C_SEND_PLAYERSKIN, getDimensionID(), new Object[]
                {strName, property.getValue(), property.getSignature(), playerRequested.getUniqueID().toString()}), playerBase);
                break;
            case S_CONTROL_ENTITY:
                if (player.getRidingEntity() != null && player.getRidingEntity() instanceof IControllableEntity)
                {
                    ((IControllableEntity) player.getRidingEntity()).pressKey((Integer) this.data.get(0));
                }
                break;
            case S_NOCLIP_PLAYER:
                boolean noClip = (Boolean) this.data.get(0);
                if (player instanceof GCEntityPlayerMP)
                {
                    GalacticraftCore.proxy.player.setNoClip((EntityPlayerMP) player, noClip);
                    if (noClip == false)
                    {
                        player.fallDistance = 0.0F;
                        ((EntityPlayerMP) player).connection.floatingTickCount = 0;
                    }
                } else if (player instanceof EntityPlayerMP)
                {
                    EntityPlayerMP emp = ((EntityPlayerMP) player);
                    try
                    {
                        Field f = emp.interactionManager.getClass().getDeclaredField(GCCoreUtil.isDeobfuscated() ? "gameType" : "field_73091_c");
                        f.setAccessible(true);
                        if (noClip == false)
                        {
                            emp.fallDistance = 0.0F;
                            emp.connection.floatingTickCount = 0;
                            GameType gt = savedSettings.get(emp);
                            if (gt != null)
                            {
                                savedSettings.remove(emp);
                                f.set(emp.interactionManager, gt);
                            }
                        } else
                        {
                            savedSettings.put(emp, emp.interactionManager.getGameType());
                            f.set(emp.interactionManager, GameType.SPECTATOR);
                        }
                    } catch (Exception ee)
                    {
                        ee.printStackTrace();
                    }
                }
                break;
            case S_REQUEST_DATA:
                WorldServer worldServer = server.getWorld((Integer) this.data.get(0));
                if (worldServer != null)
                {
                    TileEntity requestedTile = worldServer.getTileEntity((BlockPos) this.data.get(1));
                    if (requestedTile instanceof INetworkProvider)
                    {
                        if (((INetworkProvider) requestedTile).getNetwork() instanceof FluidNetwork)
                        {
                            FluidNetwork network = (FluidNetwork) ((INetworkProvider) requestedTile).getNetwork();
                            network.addUpdate(playerBase);
                        }
                    }
                }
                break;
            case S_UPDATE_CHECKLIST:
                for (EnumHand enumhand : EnumHand.values())
                {
                    ItemStack stack = player.getHeldItem(enumhand);
                    if (stack != null && stack.getItem() == GCItems.prelaunchChecklist)
                    {
                        NBTTagCompound tagCompound = stack.getTagCompound();
                        if (tagCompound == null)
                        {
                            tagCompound = new NBTTagCompound();
                        }
                        NBTTagCompound tagCompoundRead = (NBTTagCompound) this.data.get(0);
                        tagCompound.setTag("checklistData", tagCompoundRead);
                        stack.setTagCompound(tagCompound);
                    }
                }
                break;
            case S_REQUEST_MACHINE_DATA:
                TileEntity tile3 = player.world.getTileEntity((BlockPos) this.data.get(0));
                if (tile3 instanceof ITileClientUpdates)
                {
                    ((ITileClientUpdates) tile3).sendUpdateToClient(playerBase);
                }
                break;
            case S_REQUEST_CONTAINER_SLOT_REFRESH:
                // It seems as though "Update slot" packets sent internally on
                // the minecraft network packets are sent and
                // received before our custom gui open packets are handled. This
                // causes slots to not update, because from
                // the client's perspective the gui isn't open yet. Sending this
                // to the server causes all slots to be updated
                // server -> client
                if (player.openContainer.windowId == (Integer) this.data.get(0))
                {
                    for (int i = 0; i < player.openContainer.inventoryItemStacks.size(); ++i)
                    {
                        player.openContainer.inventoryItemStacks.set(i, ItemStack.EMPTY);
                    }
                }
                break;
            default:
                break;
        }
    }

    /*
     * BEGIN "net.minecraft.network.Packet" IMPLEMENTATION This is for handling
     * server->client packets before the player has joined the world
     */

    @Override
    public void readPacketData(PacketBuffer var1)
    {
        this.decodeInto(var1);
    }

    @Override
    public void writePacketData(PacketBuffer var1)
    {
        this.encodeInto(var1);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void processPacket(INetHandler var1)
    {
        if (this.type != EnumSimplePacket.C_UPDATE_SPACESTATION_LIST && this.type != EnumSimplePacket.C_UPDATE_PLANETS_LIST && this.type != EnumSimplePacket.C_UPDATE_CONFIGS)
        {
            return;
        }

        if (GCCoreUtil.getEffectiveSide() == Side.CLIENT)
        {
            this.handleClientSide(FMLClientHandler.instance().getClientPlayerEntity());
        }
    }

    /*
     * END "net.minecraft.network.Packet" IMPLEMENTATION This is for handling
     * server->client packets before the player has joined the world
     */
}
