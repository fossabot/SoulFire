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
package net.pistonmaster.soulfire.server.viaversion.platform;

import com.viaversion.viarewind.api.ViaRewindPlatform;
import lombok.RequiredArgsConstructor;
import net.pistonmaster.soulfire.server.viaversion.JLoggerToSLF4J;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class SFViaRewind implements ViaRewindPlatform {
    private final JLoggerToSLF4J logger = new JLoggerToSLF4J(LoggerFactory.getLogger("ViaRewind"));
    private final Path dataFolder;

    public void init() {
        init(dataFolder.resolve("config.yml").toFile());
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
