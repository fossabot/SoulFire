/*
 * ServerWrecker
 *
 * Copyright (C) 2023 ServerWrecker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 */
package net.pistonmaster.serverwrecker.pathfinding.graph.actions.parkour;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import net.pistonmaster.serverwrecker.pathfinding.BotEntityState;
import net.pistonmaster.serverwrecker.pathfinding.Costs;
import net.pistonmaster.serverwrecker.pathfinding.execution.GapJumpAction;
import net.pistonmaster.serverwrecker.pathfinding.graph.actions.GraphAction;
import net.pistonmaster.serverwrecker.pathfinding.graph.actions.GraphInstructions;
import net.pistonmaster.serverwrecker.util.VectorHelper;
import org.cloudburstmc.math.vector.Vector3i;

import java.util.List;

public class ParkourMovement implements GraphAction {
    private static final Vector3i FEET_POSITION_RELATIVE_BLOCK = Vector3i.ZERO;
    private final ParkourDirection direction;
    private final Vector3i targetFeetBlock;
    @Setter
    @Getter
    private boolean isImpossible = false;

    public ParkourMovement(ParkourDirection direction) {
        this.direction = direction;
        this.targetFeetBlock = direction.offset(direction.offset(FEET_POSITION_RELATIVE_BLOCK));
    }

    private ParkourMovement(ParkourMovement other) {
        this.direction = other.direction;
        this.targetFeetBlock = other.targetFeetBlock;
        this.isImpossible = other.isImpossible;
    }

    public List<Vector3i> listRequiredFreeBlocks() {
        List<Vector3i> requiredFreeBlocks = new ObjectArrayList<>();

        // Make head block free (maybe head block is a slab)
        requiredFreeBlocks.add(FEET_POSITION_RELATIVE_BLOCK.add(0, 1, 0));

        // Make block above the head block free for jump
        requiredFreeBlocks.add(FEET_POSITION_RELATIVE_BLOCK.add(0, 2, 0));

        var oneFurther = direction.offset(FEET_POSITION_RELATIVE_BLOCK);

        // The gap to jump over
        requiredFreeBlocks.add(oneFurther.sub(0, 1, 0));

        // Room for jumping
        requiredFreeBlocks.add(oneFurther);
        requiredFreeBlocks.add(oneFurther.add(0, 1, 0));
        requiredFreeBlocks.add(oneFurther.add(0, 2, 0));

        var twoFurther = direction.offset(oneFurther);

        // Room for jumping
        requiredFreeBlocks.add(twoFurther);
        requiredFreeBlocks.add(twoFurther.add(0, 1, 0));
        requiredFreeBlocks.add(twoFurther.add(0, 2, 0));

        return requiredFreeBlocks;
    }

    public Vector3i requiredSolidBlock() {
        // Floor block
        return targetFeetBlock.sub(0, 1, 0);
    }

    @Override
    public boolean isImpossibleToComplete() {
        return isImpossible;
    }

    @Override
    public GraphInstructions getInstructions(BotEntityState previousEntityState) {
        var absoluteTargetFeetBlock = previousEntityState.positionBlock().add(targetFeetBlock);
        var targetFeetDoublePosition = VectorHelper.middleOfBlockNormalize(absoluteTargetFeetBlock.toDouble());

        return new GraphInstructions(new BotEntityState(
                targetFeetDoublePosition,
                absoluteTargetFeetBlock,
                previousEntityState.levelState(),
                previousEntityState.inventory()
        ), Costs.ONE_GAP_JUMP, List.of(new GapJumpAction(targetFeetDoublePosition)));
    }

    @Override
    public ParkourMovement copy(BotEntityState previousEntityState) {
        return new ParkourMovement(this);
    }
}
