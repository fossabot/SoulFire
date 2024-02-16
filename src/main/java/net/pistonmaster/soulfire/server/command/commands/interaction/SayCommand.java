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
package net.pistonmaster.soulfire.server.command.commands.interaction;

import com.mojang.brigadier.Command;
import net.pistonmaster.soulfire.server.command.ServerCommandManager;
import net.pistonmaster.soulfire.server.command.ICommand;
import net.pistonmaster.soulfire.server.command.SoulfireCommandSyntaxException;

import java.util.List;

public class SayCommand implements ICommand {
    @Override
    public List<String> getAliases() {
        return List.of("say");
    }

    @Override
    public String getDescription() {
        return "Makes all connected bots send a message in chat or execute a command.";
    }

    @Override
    public String getSyntax() {
        return "<message>";
    }

    @Override
    public int onCommand(String cmd, String[] args, ServerCommandManager manager) throws SoulfireCommandSyntaxException {
        if (args.length == 0) {
            throw new SoulfireCommandSyntaxException("You need to specify a message!");
        }

        String message = String.join(" ", args);

        manager.forEveryBot((bot) -> {
            bot.botControl().sendMessage(message);
            return Command.SINGLE_SUCCESS;
        });

        return Command.SINGLE_SUCCESS;
    }
}
