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
package net.pistonmaster.serverwrecker.pathfinding.execution;

import net.kyori.event.EventSubscriber;
import net.kyori.event.EventSubscription;
import net.pistonmaster.serverwrecker.api.event.bot.BotPreTickEvent;
import net.pistonmaster.serverwrecker.protocol.BotConnection;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

public class PathExecutor implements EventSubscriber<BotPreTickEvent> {
    private final Queue<WorldAction> worldActions;
    private final BotConnection connection;
    private final Supplier<List<WorldAction>> findPath;
    private final ExecutorService executorService;
    private EventSubscription subscription;
    private int ticks = 0;

    public PathExecutor(BotConnection connection, List<WorldAction> worldActions, Supplier<List<WorldAction>> findPath,
                        ExecutorService executorService) {
        this.worldActions = new ArrayBlockingQueue<>(worldActions.size());
        this.worldActions.addAll(worldActions);
        this.connection = connection;
        this.findPath = findPath;
        this.executorService = executorService;
    }

    @Override
    public void on(@NonNull BotPreTickEvent event) {
        var connection = event.connection();
        if (connection != this.connection) {
            return;
        }

        if (worldActions.isEmpty()) {
            unregister();
            return;
        }

        var worldAction = worldActions.peek();
        if (worldAction == null) {
            unregister();
            return;
        }

        if (worldAction instanceof RecalculatePathAction) {
            connection.logger().info("Recalculating path...");
            recalculatePath();
            return;
        }

        if (ticks > 0 && ticks >= worldAction.getAllowedTicks()) {
            connection.logger().warn("Took too long to complete action: {}", worldAction);
            connection.logger().warn("Recalculating path...");
            recalculatePath();
            return;
        }

        if (worldAction.isCompleted(connection)) {
            worldActions.remove();
            connection.logger().info("Reached goal in {} ticks!", ticks);
            ticks = 0;

            // Directly use tick to execute next goal
            worldAction = worldActions.peek();

            // If there are no more goals, stop
            if (worldAction == null) {
                connection.logger().info("Finished all goals!");
                var movementManager = connection.sessionDataManager().getBotMovementManager();
                movementManager.getControlState().resetAll();
                unregister();
                return;
            }

            connection.logger().debug("Next goal: {}", worldAction);
        }

        ticks++;
        worldAction.tick(connection);
    }

    public void register() {
        subscription = connection.eventBus().subscribe(BotPreTickEvent.class, this);
    }

    public void unregister() {
        subscription.unsubscribe();
    }

    private void recalculatePath() {
        this.unregister();
        connection.sessionDataManager().getBotMovementManager().getControlState().resetAll();

        executorService.submit(() -> {
            try {
                var newActions = findPath.get();
                connection.logger().info("Found new path with {} actions!", newActions.size());
                var newExecutor = new PathExecutor(connection, newActions, findPath, executorService);
                newExecutor.register();
            } catch (Throwable t) {
                connection.logger().error("Failed to recalculate path!", t);
            }
        });
    }
}
