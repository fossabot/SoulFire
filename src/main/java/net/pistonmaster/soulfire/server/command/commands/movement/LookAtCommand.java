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

import com.github.steveice10.mc.protocol.data.game.entity.RotationOrigin;
import com.mojang.brigadier.Command;
import net.pistonmaster.soulfire.server.command.ICommand;
import net.pistonmaster.soulfire.server.command.ServerCommandManager;
import net.pistonmaster.soulfire.server.command.SoulfireCommandSyntaxException;
import org.cloudburstmc.math.vector.Vector3d;

import java.util.List;

public class LookAtCommand implements ICommand {
    @Override
    public List<String> getAliases() {
        return List.of("look-at", "look");
    }

    @Override
    public String getDescription() {
        return "Makes the bot walk to the specified coordinates";
    }

    @Override
    public String getSyntax() {
        return "<x> (y) <z>";
    }

    @Override
    public int onCommand(String cmd, String[] args, ServerCommandManager manager) throws SoulfireCommandSyntaxException {
        if (args.length < 2) {
            throw new SoulfireCommandSyntaxException("You need to specify at least one coordinate");
        }

        if (args.length == 2) { // x z pos
            try {
                double x = Double.parseDouble(args[0]);
                double z = Double.parseDouble(args[1]);

                manager.forEveryBot(bot -> {
                    Vector3d pos = Vector3d.from(x, bot.sessionDataManager().clientEntity().y(), z);
                    bot.sessionDataManager().clientEntity().lookAt(RotationOrigin.EYES, pos);

                    return Command.SINGLE_SUCCESS;
                });
            } catch (NumberFormatException e) {
                throw new SoulfireCommandSyntaxException("The x and z coordinates need to be valid numbers");
            }
        }

        try { // x y z pos
            double x = Double.parseDouble(args[0]);
            double y = Double.parseDouble(args[1]);
            double z = Double.parseDouble(args[2]);

            manager.forEveryBot(bot -> {
                Vector3d pos = Vector3d.from(x, y, z);
                bot.sessionDataManager().clientEntity().lookAt(RotationOrigin.EYES, pos);

                return Command.SINGLE_SUCCESS;
            });
            return Command.SINGLE_SUCCESS;
        } catch (NumberFormatException e) {
            throw new SoulfireCommandSyntaxException("The x, y and z coordinates need to be valid numbers");
        }
    }
}
