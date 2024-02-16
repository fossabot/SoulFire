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
package net.pistonmaster.soulfire.server.command.commands.utility;

import com.mojang.brigadier.Command;
import lombok.extern.slf4j.Slf4j;
import net.pistonmaster.soulfire.server.command.ICommand;
import net.pistonmaster.soulfire.server.command.ServerCommandManager;
import org.apache.commons.io.FileUtils;

import java.util.List;

@Slf4j
public class StatsCommand implements ICommand {
    @Override
    public List<String> getAliases() {
        return List.of("stats", "network");
    }

    @Override
    public String getDescription() {
        return "Shows network stats.";
    }

    @Override
    public String getSyntax() {
        return null;
    }

    @Override
    public int onCommand(String cmd, String[] args, ServerCommandManager manager) {
        manager.forEveryAttackEnsureHasBots(attackManager -> {
            log.info("Total bots: {}", attackManager.botConnections().size());
            long readTraffic = 0;
            long writeTraffic = 0;
            for (var bot : attackManager.botConnections()) {
                var trafficShapingHandler = bot.getTrafficHandler();

                if (trafficShapingHandler == null) {
                    continue;
                }

                readTraffic += trafficShapingHandler.trafficCounter().cumulativeReadBytes();
                writeTraffic += trafficShapingHandler.trafficCounter().cumulativeWrittenBytes();
            }

            log.info("Total read traffic: {}", FileUtils.byteCountToDisplaySize(readTraffic));
            log.info("Total write traffic: {}", FileUtils.byteCountToDisplaySize(writeTraffic));

            long currentReadTraffic = 0;
            long currentWriteTraffic = 0;
            for (var bot : attackManager.botConnections()) {
                var trafficShapingHandler = bot.getTrafficHandler();

                if (trafficShapingHandler == null) {
                    continue;
                }

                currentReadTraffic += trafficShapingHandler.trafficCounter().lastReadThroughput();
                currentWriteTraffic += trafficShapingHandler.trafficCounter().lastWriteThroughput();
            }

            log.info("Current read traffic: {}/s", FileUtils.byteCountToDisplaySize(currentReadTraffic));
            log.info("Current write traffic: {}/s", FileUtils.byteCountToDisplaySize(currentWriteTraffic));

            return Command.SINGLE_SUCCESS;
        });
        return Command.SINGLE_SUCCESS;
    }
}
