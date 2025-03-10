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
package net.pistonmaster.soulfire.server.pathfinding.goals;

import net.pistonmaster.soulfire.server.pathfinding.BotEntityState;
import net.pistonmaster.soulfire.server.pathfinding.graph.MinecraftGraph;

/**
 * A goal represents something that the user wants the bot to achieve.
 */
public interface GoalScorer {
    /**
     * Calculates the estimated score for a given block position to the goal.
     * Usually this means the distance from achieving the goal.
     *
     * @param graph       the graph to calculate the score for
     * @param entityState the entity state to calculate the score for
     * @return the score for the given world state
     */
    double computeScore(MinecraftGraph graph, BotEntityState entityState);

    /**
     * Checks if the given world state indicates that the goal is reached.
     *
     * @param entityState the current entity state
     * @return true if the goal is reached, false otherwise
     */
    boolean isFinished(BotEntityState entityState);
}
