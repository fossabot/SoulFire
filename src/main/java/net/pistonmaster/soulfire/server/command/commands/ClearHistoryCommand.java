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
import net.pistonmaster.soulfire.server.command.ServerCommandManager;
import net.pistonmaster.soulfire.server.command.ICommand;

import java.util.List;

public class ClearHistoryCommand implements ICommand {
    @Override
    public List<String> getAliases() {
        return List.of("cls", "clear", "clear-history");
    }

    @Override
    public String getDescription() {
        return "Wipes the command history data.";
    }

    @Override
    public String getSyntax() {
        return null;
    }

    @Override
    public int onCommand(String cmd, String[] args, ServerCommandManager manager) {
        manager.clearCommandHistory();
        return Command.SINGLE_SUCCESS;
    }
}
