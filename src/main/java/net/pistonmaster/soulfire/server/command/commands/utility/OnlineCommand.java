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

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class OnlineCommand implements ICommand {
    @Override
    public List<String> getAliases() {
        return List.of("online");
    }

    @Override
    public String getDescription() {
        return "Shows connected bots from all attacks.";
    }

    @Override
    public String getSyntax() {
        return null;
    }

    @Override
    public int onCommand(String cmd, String[] args, ServerCommandManager manager) {
        manager.forEveryAttackEnsureHasBots(attackManager -> {
            var online = new ArrayList<String>();
            for (var bot : attackManager.botConnections()) {
                if (!bot.isOnline()) {
                    continue;
                }

                online.add(bot.meta().minecraftAccount().username());
            }

            log.info(online.size() + " bots online: " + String.join(", ", online));
            return Command.SINGLE_SUCCESS;
        });
        return Command.SINGLE_SUCCESS;
    }
}
