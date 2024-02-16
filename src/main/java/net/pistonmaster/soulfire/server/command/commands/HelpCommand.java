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
package net.pistonmaster.soulfire.server.command.commands;

import com.mojang.brigadier.Command;
import lombok.extern.slf4j.Slf4j;
import net.pistonmaster.soulfire.server.command.ServerCommandManager;
import net.pistonmaster.soulfire.server.command.ICommand;

import java.util.List;

@Slf4j
public class HelpCommand implements ICommand {
    @Override
    public List<String> getAliases() {
        return List.of("help", "?");
    }

    @Override
    public String getDescription() {
        return "Prints a list of all available commands.";
    }

    @Override
    public String getSyntax() {
        return null;
    }

    @Override
    public int onCommand(String cmd, String[] args, ServerCommandManager manager) {
        manager.commands().forEach((command) -> {
            StringBuilder commandInfo = new StringBuilder();
            commandInfo.append("{");
            command.getAliases().forEach((alias) -> commandInfo.append(alias).append("/"));
            commandInfo.deleteCharAt(commandInfo.length() - 1);
            commandInfo.append("} ");
            if (command.getSyntax() != null) {
                commandInfo.append(command.getSyntax());
            } else {
                commandInfo.deleteCharAt(commandInfo.length() - 1);
            }
            commandInfo.append(": ").append(command.getDescription());
            log.info(commandInfo.toString());
        });
        return Command.SINGLE_SUCCESS;
    }
}
