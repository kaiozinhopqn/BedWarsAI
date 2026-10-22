package com.bedwarsai.util;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public final class WorldUtil {

    private WorldUtil() {}

    public static boolean isSolid(World w, BlockPos p) {
        return w.getBlockState(p).getBlock().getMaterial().isSolid();
    }

    /**
     * Ha chao seguro a partir de (x,y,z) ate 'depth' blocos abaixo?
     * Retorna false se o primeiro bloco nao-ar encontrado for liquido (lava/agua).
     */
    public static boolean groundWithin(World w, double x, double y, double z, int depth) {
        int bx = MathHelper.floor_double(x);
        int bz = MathHelper.floor_double(z);
        int by = MathHelper.floor_double(y);
        for (int i = 0; i <= depth; i++) {
            BlockPos p = new BlockPos(bx, by - i, bz);
            IBlockState st = w.getBlockState(p);
            Material m = st.getBlock().getMaterial();
            if (m.isSolid()) return true;
            if (m.isLiquid()) return false;
        }
        return false;
    }

    /** Distancia do olho de 'from' ate a caixa de colisao de 'to'. */
    public static double eyeDistanceToBox(Entity from, Entity to) {
        AxisAlignedBB bb = to.getEntityBoundingBox();
        double ex = from.posX;
        double ey = from.posY + from.getEyeHeight();
        double ez = from.posZ;
        double cx = MathHelper.clamp_double(ex, bb.minX, bb.maxX);
        double cy = MathHelper.clamp_double(ey, bb.minY, bb.maxY);
        double cz = MathHelper.clamp_double(ez, bb.minZ, bb.maxZ);
        double dx = ex - cx, dy = ey - cy, dz = ez - cz;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /** Yaw (graus) que o jogador deve ter para olhar na direcao da face. */
    public static float yawForFacing(EnumFacing facing) {
        switch (facing) {
            case SOUTH: return 0f;
            case WEST:  return 90f;
            case NORTH: return 180f;
            case EAST:  return 270f;
            default:    return 0f;
        }
    }

    public static EnumFacing dominantDirection(double dx, double dz) {
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? EnumFacing.EAST : EnumFacing.WEST;
        }
        return dz > 0 ? EnumFacing.SOUTH : EnumFacing.NORTH;
    }
}
