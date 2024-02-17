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
package net.pistonmaster.soulfire.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.Suggestion;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pistonmaster.soulfire.brigadier.ConsoleSubject;
import net.pistonmaster.soulfire.server.AttackManager;
import net.pistonmaster.soulfire.server.SoulFireServer;
import net.pistonmaster.soulfire.server.api.SoulFireAPI;
import net.pistonmaster.soulfire.server.api.event.lifecycle.DispatcherInitEvent;
import net.pistonmaster.soulfire.server.command.commands.*;
import net.pistonmaster.soulfire.server.command.commands.interaction.*;
import net.pistonmaster.soulfire.server.command.commands.misc.*;
import net.pistonmaster.soulfire.server.command.commands.movement.*;
import net.pistonmaster.soulfire.server.command.commands.utility.*;
import net.pistonmaster.soulfire.server.protocol.BotConnection;
import net.pistonmaster.soulfire.util.SFPathConstants;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.*;
import java.util.function.ToIntFunction;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ServerCommandManager {
    @Getter
    private final CommandDispatcher<ConsoleSubject> dispatcher = new CommandDispatcher<>();
    @Getter
    private final SoulFireServer soulFireServer;
    private final List<Map.Entry<Instant, String>> commandHistory = Collections.synchronizedList(new ArrayList<>());
    private final Path targetFile = SFPathConstants.DATA_FOLDER.resolve(".command_history");
    @Getter
    private final List<ICommand> commands = new ArrayList<>();

    @PostConstruct
    public void postConstruct() {
        loadCommandHistory();

        commands.add(new ClearHistoryCommand());
        commands.add(new ReloadHistoryCommand());
        commands.add(new StopAttacksCommand());

        // Help
        commands.add(new HelpCommand());

        // Movement
        commands.add(new WalkCommand());
        commands.add(new ForwardCommand());
        commands.add(new BackwardCommand());
        commands.add(new LeftCommand());
        commands.add(new RightCommand());
        commands.add(new JumpCommand());
        commands.add(new SneakCommand());
        commands.add(new LookAtCommand());
        commands.add(new ResetCommand());
        commands.add(new StopPathCommand());

        // Interaction
        commands.add(new SayCommand());

        // Utility
        commands.add(new OnlineCommand());
        commands.add(new StatsCommand());

        // Misc
        commands.add(new CrashCommand());

        SoulFireAPI.postEvent(new DispatcherInitEvent(dispatcher));
    }

    public int forEveryAttack(ToIntFunction<AttackManager> consumer) {
        if (soulFireServer.attacks().isEmpty()) {
            log.warn("No attacks found!");
            return 2;
        }

        var resultCode = Command.SINGLE_SUCCESS;
        for (var attackManager : soulFireServer.attacks().values()) {
            log.info("--- Running command for attack {} ---", attackManager.id());
            var result = consumer.applyAsInt(attackManager);
            if (result != Command.SINGLE_SUCCESS) {
                resultCode = result;
            }
        }

        return resultCode;
    }

    public int forEveryAttackEnsureHasBots(ToIntFunction<AttackManager> consumer) {
        return forEveryAttack(attackManager -> {
            if (attackManager.botConnections().isEmpty()) {
                log.warn("No bots connected!");
                return 3;
            }

            return consumer.applyAsInt(attackManager);
        });
    }

    public int forEveryBot(ToIntFunction<BotConnection> consumer) {
        return forEveryAttackEnsureHasBots(attackManager -> {
            var resultCode = Command.SINGLE_SUCCESS;
            for (var bot : attackManager.botConnections()) {
                log.info("--- Running command for bot {} ---", bot.meta().minecraftAccount().username());
                var result = consumer.applyAsInt(bot);
                if (result != Command.SINGLE_SUCCESS) {
                    resultCode = result;
                }
            }

            return resultCode;
        });
    }

    public List<Map.Entry<Instant, String>> getCommandHistory() {
        synchronized (commandHistory) {
            return List.copyOf(commandHistory);
        }
    }

    public int execute(String command) {
        command = command.strip();

        try {
            commandHistory.add(Map.entry(Instant.now(), command));

            String commandName = command.split(" ")[0];
            String[] args = command.split(" ");
            args = Arrays.copyOfRange(args, 1, args.length);

            String[] finalArgs = args;
            ICommand cmd = commands.stream().filter(c -> c.getAliases().contains(commandName)).findFirst().orElse(null);

            if (cmd == null) {
                log.warn("Command not found: {}", commandName);
                return Command.SINGLE_SUCCESS;
            }

            try {
                int status = cmd.onCommand(commandName, finalArgs, this);
                if (status == Command.SINGLE_SUCCESS) {
                    newCommandHistoryEntry(command);
                }
                return status;
            } catch (SoulfireCommandSyntaxException e) {
                log.warn(e.getMessage());
                log.warn("Usage: {}", cmd.getSyntax());
                return Command.SINGLE_SUCCESS;
            } catch (Exception e) {
                log.error("Failed to execute command!", e);
                log.error("Usage: {}", cmd.getSyntax());
                return 1;
            }
        } catch (Exception e) {
            log.error("Failed to execute command!", e);
            return 1;
        }
    }

    public void loadCommandHistory() {
        synchronized (commandHistory) {
            commandHistory.clear();
            try {
                if (!Files.exists(targetFile)) {
                    return;
                }

                var lines = Files.readAllLines(targetFile);
                for (var line : lines) {
                    var firstColon = line.indexOf(':');
                    if (firstColon == -1) {
                        continue;
                    }

                    var seconds = Long.parseLong(line.substring(0, firstColon));
                    var command = line.substring(firstColon + 1);

                    commandHistory.add(Map.entry(Instant.ofEpochSecond(seconds), command));
                }
            } catch (IOException e) {
                log.error("Failed to create command history file!", e);
            }
        }
    }

    private void newCommandHistoryEntry(String command) {
        synchronized (commandHistory) {
            try {
                Files.createDirectories(targetFile.getParent());
                var newLine = Instant.now().getEpochSecond() + ":" + command + System.lineSeparator();
                Files.writeString(targetFile, newLine, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                log.error("Failed to create command history file!", e);
            }
        }
    }

    public void clearCommandHistory() {
        synchronized (commandHistory) {
            try {
                Files.deleteIfExists(targetFile);
                commandHistory.clear();
            } catch (IOException e) {
                log.error("Failed to delete command history file!", e);
            }
        }
    }

    public List<String> getCompletionSuggestions(String command) {
        return dispatcher.getCompletionSuggestions(dispatcher.parse(command, ConsoleSubject.INSTANCE))
                .join()
                .getList()
                .stream()
                .map(Suggestion::getText)
                .toList();
    }
}
