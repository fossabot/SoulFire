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
package net.pistonmaster.soulfire.server.command.commands.movement;

import com.mojang.brigadier.Command;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import net.pistonmaster.soulfire.server.command.ICommand;
import net.pistonmaster.soulfire.server.command.ServerCommandManager;
import net.pistonmaster.soulfire.server.command.SoulfireCommandSyntaxException;
import net.pistonmaster.soulfire.server.pathfinding.BotEntityState;
import net.pistonmaster.soulfire.server.pathfinding.RouteFinder;
import net.pistonmaster.soulfire.server.pathfinding.execution.PathExecutor;
import net.pistonmaster.soulfire.server.pathfinding.execution.WorldAction;
import net.pistonmaster.soulfire.server.pathfinding.goals.GoalScorer;
import net.pistonmaster.soulfire.server.pathfinding.goals.PosGoal;
import net.pistonmaster.soulfire.server.pathfinding.goals.XZGoal;
import net.pistonmaster.soulfire.server.pathfinding.goals.YGoal;
import net.pistonmaster.soulfire.server.pathfinding.graph.MinecraftGraph;
import net.pistonmaster.soulfire.server.pathfinding.graph.ProjectedInventory;
import net.pistonmaster.soulfire.server.pathfinding.graph.ProjectedLevelState;

import java.util.List;
import java.util.Objects;

public class WalkCommand implements ICommand {
    @Override
    public List<String> getAliases() {
        return List.of("walk", "path", "goto");
    }

    @Override
    public String getDescription() {
        return "Walks bots to a specific location.";
    }

    @Override
    public String getSyntax() {
        return "y or <x> (y) <z>";
    }

    @Override
    public int onCommand(String cmd, String[] args, ServerCommandManager manager) throws SoulfireCommandSyntaxException {
        if (args.length < 1) {
            throw new SoulfireCommandSyntaxException("You need to specify at least one coordinate");
        }

        if (args.length == 1) { // y path
            try {
                int y = Integer.parseInt(args[0]);

                return executePathfinding(new YGoal(y), manager);
            } catch (NumberFormatException e) {
                throw new SoulfireCommandSyntaxException("The y coordinate needs to be a valid number");
            }
        }

        if (args.length == 2) { // x z path
            try {
                int x = Integer.parseInt(args[0]);
                int z = Integer.parseInt(args[1]);

                return executePathfinding(new XZGoal(x, z), manager);
            } catch (NumberFormatException e) {
                throw new SoulfireCommandSyntaxException("The x and z coordinates need to be valid numbers");
            }
        }

        try { // x y z path
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            int z = Integer.parseInt(args[2]);

            return executePathfinding(new PosGoal(x, y, z), manager);
        } catch (NumberFormatException e) {
            throw new SoulfireCommandSyntaxException("The x, y and z coordinates need to be valid numbers");
        }
    }

    private int executePathfinding(GoalScorer goalScorer, ServerCommandManager manager) {
        return manager.forEveryBot(bot -> {
            var logger = bot.logger();
            var executorService = bot.executorManager().newExecutorService(bot, "PathfindingManager");
            executorService.execute(() -> {
                var sessionDataManager = bot.sessionDataManager();
                var clientEntity = sessionDataManager.clientEntity();
                var routeFinder = new RouteFinder(
                        new MinecraftGraph(sessionDataManager.tagsState()),
                        goalScorer
                );

                Boolean2ObjectFunction<List<WorldAction>> findPath = requiresRepositioning -> {
                    var start = BotEntityState.initialState(
                            clientEntity,
                            new ProjectedLevelState(
                                    Objects.requireNonNull(sessionDataManager.getCurrentLevel(), "Level is null!").chunks().immutableCopy()
                            ),
                            new ProjectedInventory(
                                    sessionDataManager.inventoryManager().playerInventory()
                            )
                    );
                    logger.info("Starting calculations at: {}", start);
                    var actions = routeFinder.findRoute(start, requiresRepositioning);
                    logger.info("Calculated path with {} actions: {}", actions.size(), actions);
                    return actions;
                };

                var pathExecutor = new PathExecutor(bot, findPath.get(true), findPath, executorService);
                pathExecutor.register();
            });

            return Command.SINGLE_SUCCESS;
        });
    }
}
