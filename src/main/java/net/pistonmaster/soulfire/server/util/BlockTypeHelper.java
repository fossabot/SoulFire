/*
 * SoulFire
 * Copyright (C) 2024  AlexProgrammerDE
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.pistonmaster.soulfire.server.util;

import net.pistonmaster.soulfire.server.data.BlockShapeGroup;
import net.pistonmaster.soulfire.server.data.BlockState;
import net.pistonmaster.soulfire.server.data.BlockType;

public class BlockTypeHelper {
    private static final double SAFE_BLOCK_MIN_HEIGHT = 0.9;

    private BlockTypeHelper() {
    }

    public static boolean isFullBlock(BlockState meta) {
        return meta.blockShapeGroup().isFullBlock();
    }

    public static boolean isHurtOnTouchSide(BlockType type) {
        return type == BlockType.CACTUS
                || type == BlockType.SWEET_BERRY_BUSH
                || type == BlockType.WITHER_ROSE
                || type == BlockType.FIRE
                || type == BlockType.SOUL_FIRE
                || type == BlockType.LAVA;
    }

    public static boolean isHurtWhenStoodOn(BlockType type) {
        return type == BlockType.MAGMA_BLOCK;
    }

    public static boolean isSafeBlockToStandOn(BlockState meta) {
        return isRoughlyFullBlock(meta.blockShapeGroup()) && !isHurtWhenStoodOn(meta.blockType());
    }

    public static boolean isRoughlyFullBlock(BlockShapeGroup type) {
        if (type.blockShapes().size() != 1) {
            return false;
        }

        var shape = type.blockShapes().getFirst();
        return shape.isBlockXZCollision()
                && shape.minY() == 0
                && shape.maxY() >= SAFE_BLOCK_MIN_HEIGHT;
    }

    public static boolean isDiggable(BlockType type) {
        return type.destroyTime() != -1;
    }
}
