/*
 * Copyright (c) 2022 Team Galacticraft
 *
 * Licensed under the MIT license.
 * See LICENSE file in the project root for details.
 */

package micdoodle8.mods.galacticraft.planets.venus.world.gen;

import java.util.Random;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.MapGenBaseMeta;
import micdoodle8.mods.galacticraft.core.Constants;
import micdoodle8.mods.galacticraft.planets.venus.VenusBlocks;
import micdoodle8.mods.galacticraft.planets.venus.blocks.BlockBasicVenus;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public class MapGenLavaVenus extends MapGenBaseMeta
{

    protected void generateCaveNode(long par1, int par3, int par4, ChunkPrimer primer, double par6, double par8, double par10, float par12, float par13, float par14, int par15, int par16, double par17)
    {
        final double d4 = par3 * 16 + 8;
        final double d5 = par4 * 16 + 8;
        float f3 = 0.0F;
        float f4 = 0.0F;
        final Random random = new Random(par1);

        if (par16 <= 0)
        {
            final int j1 = this.range * 16 - 16;
            par16 = j1 - random.nextInt(j1 / 4);
        }

        boolean flag = false;

        if (par15 == -1)
        {
            par15 = par16 / 2;
            flag = true;
        }

        final int k1 = random.nextInt(par16 / 2) + par16 / 4;

        for (final boolean flag1 = random.nextInt(6) == 0; par15 < par16; ++par15)
        {
            final double d6 = 0.5D + MathHelper.sin(par15 * (float) Math.PI / par16) * par12 * 1.0F;
            final double d7 = d6 * par17;
            final float f5 = MathHelper.cos(par14);
            final float f6 = MathHelper.sin(par14);
            par6 += MathHelper.cos(par13) * f5;
            par8 += f6;
            par10 += MathHelper.sin(par13) * f5;

            if (flag1)
            {
                par14 *= 0.92F;
            } else
            {
                par14 *= 0.7F;
            }

            par14 += f4 * 0.1F;
            par13 += f3 * 0.1F;
            f4 *= 0.9F;
            f3 *= 0.75F;
            f4 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            f3 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

            if (!flag && par15 == k1 && par12 > 1.0F && par16 > 0)
            {
                this.generateCaveNode(random.nextLong(), par3, par4, primer, par6, par8, par10, random.nextFloat() * 0.5F + 0.5F, par13 - Constants.halfPI, par14 / 3.0F, par15, par16, 1.0D);
                this.generateCaveNode(random.nextLong(), par3, par4, primer, par6, par8, par10, random.nextFloat() * 0.5F + 0.5F, par13 + Constants.halfPI, par14 / 3.0F, par15, par16, 1.0D);
                return;
            }

            if (flag || random.nextInt(4) != 0)
            {
                final double d8 = par6 - d4;
                final double d9 = par10 - d5;
                final double d10 = par16 - par15;
                final double d11 = par12 + 2.0F + 16.0F;

                if (d8 * d8 + d9 * d9 - d10 * d10 > d11 * d11)
                {
                    return;
                }

                if (par6 >= d4 - 16.0D - d6 * 2.0D && par10 >= d5 - 16.0D - d6 * 2.0D && par6 <= d4 + 16.0D + d6 * 2.0D && par10 <= d5 + 16.0D + d6 * 2.0D)
                {
                    int l1 = MathHelper.floor(par6 - d6) - par3 * 16 - 1;
                    int i2 = MathHelper.floor(par6 + d6) - par3 * 16 + 1;
                    int j2 = MathHelper.floor(par8 - d7) - 1;
                    int k2 = MathHelper.floor(par8 + d7) + 1;
                    int l2 = MathHelper.floor(par10 - d6) - par4 * 16 - 1;
                    int i3 = MathHelper.floor(par10 + d6) - par4 * 16 + 1;

                    if (l1 < 0)
                    {
                        l1 = 0;
                    }

                    if (i2 > 16)
                    {
                        i2 = 16;
                    }

                    if (j2 < 1)
                    {
                        j2 = 1;
                    }

//                    if (k2 > 120)
//                    {
//                        k2 = 120;
//                    }

                    if (l2 < 0)
                    {
                        l2 = 0;
                    }

                    if (i3 > 16)
                    {
                        i3 = 16;
                    }

                    int j3;
                    for (j3 = l1; j3 < i2; ++j3)
                    {
                        for (int l3 = l2; l3 < i3; ++l3)
                        {
                            for (int i4 = k2 + 1; i4 >= j2 - 1; --i4)
                            {
                                if (i4 >= 0 && i4 < 128)
                                {
                                    if (i4 != j2 - 1 && j3 != l1 && j3 != i2 - 1 && l3 != l2 && l3 != i3 - 1)
                                    {
                                        i4 = j2;
                                    }
                                }
                            }
                        }
                    }

                    if (true)
                    {

                        for (int localY = j2; localY < k2; localY++)
                        {
                            final double yfactor = (localY + 0.5D - par8) / d7;
                            final double yfactorSq = yfactor * yfactor;

                            for (int localX = l1; localX < i2; localX++)
                            {
                                final double zfactor = (localX + par3 * 16 + 0.5D - par6) / d6;
                                final double zfactorSq = zfactor * zfactor;

                                for (int localZ = l2; localZ < i3; localZ++)
                                {
                                    final double xfactor = (localZ + par4 * 16 + 0.5D - par10) / d6;
                                    final double xfactorSq = xfactor * xfactor;

                                    if (xfactorSq + zfactorSq < 1.0D)
                                    {
                                        if (yfactor > -0.7D && xfactorSq + yfactorSq + zfactorSq < 1.0D)
                                        {
                                            IBlockState toReplace = primer.getBlockState(localX, localY, localZ);
                                            if (toReplace.getBlock() == VenusBlocks.venusBlock)
                                            {
                                                BlockBasicVenus.EnumBlockBasicVenus type = (BlockBasicVenus.EnumBlockBasicVenus) toReplace.getValue(BlockBasicVenus.BASIC_TYPE_VENUS);
                                                if (type == BlockBasicVenus.EnumBlockBasicVenus.ROCK_HARD || type == BlockBasicVenus.EnumBlockBasicVenus.ROCK_SOFT)
                                                {
                                                    IBlockState north = localZ == 0 ? Blocks.AIR.getDefaultState() : primer.getBlockState(localX, localY, localZ - 1);
                                                    IBlockState south = localZ == 15 ? Blocks.AIR.getDefaultState() : primer.getBlockState(localX, localY, localZ + 1);
                                                    IBlockState east = localX == 15 ? Blocks.AIR.getDefaultState() : primer.getBlockState(localX + 1, localY, localZ);
                                                    IBlockState west = localX == 0 ? Blocks.AIR.getDefaultState() : primer.getBlockState(localX - 1, localY, localZ);
                                                    IBlockState below = localY == 0 ? Blocks.AIR.getDefaultState() : primer.getBlockState(localX, localY - 1, localZ);
                                                    if (north.getBlock() == Blocks.AIR || south.getBlock() == Blocks.AIR || east.getBlock() == Blocks.AIR || west.getBlock() == Blocks.AIR
                                                        || below.getBlock() == Blocks.AIR)
                                                    {
                                                        primer.setBlockState(localX, localY, localZ,
                                                            VenusBlocks.venusBlock.getDefaultState().withProperty(BlockBasicVenus.BASIC_TYPE_VENUS, BlockBasicVenus.EnumBlockBasicVenus.ROCK_MAGMA));
                                                    } else
                                                    {
                                                        primer.setBlockState(localX, localY, localZ, Blocks.LAVA.getDefaultState());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (flag)
                        {
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void recursiveGenerate(World par1World, int par2, int par3, int par4, int par5, ChunkPrimer primer)
    {
        int var7 = this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(40) + 1) + 1);

        if (this.rand.nextInt(25) != 0)
        {
            var7 = 0;
        }

        for (int var8 = 0; var8 < var7; ++var8)
        {
            final double var9 = par2 * 16 + this.rand.nextInt(16);
            final double var11 = this.rand.nextInt(this.rand.nextInt(120) + 8);
            final double var13 = par3 * 16 + this.rand.nextInt(16);

            for (int var16 = 0; var16 < 1; ++var16)
            {
                final float var17 = this.rand.nextFloat() * Constants.twoPI;
                final float var18 = (this.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;

                this.generateCaveNode(this.rand.nextLong(), par4, par5, primer, var9, var11, var13, 1.0F, var17, var18, 0, 0, 1.0D);
            }
        }
    }
}
