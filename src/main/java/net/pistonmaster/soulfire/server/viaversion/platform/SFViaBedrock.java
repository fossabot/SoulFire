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

import lombok.RequiredArgsConstructor;
import net.pistonmaster.soulfire.server.viaversion.JLoggerToSLF4J;
import net.raphimc.viabedrock.ViaBedrockConfig;
import net.raphimc.viabedrock.platform.ViaBedrockPlatform;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class SFViaBedrock implements ViaBedrockPlatform {
    private final JLoggerToSLF4J logger = new JLoggerToSLF4J(LoggerFactory.getLogger("ViaBedrock"));
    private final Path dataFolder;

    public void init() {
        var configFile = dataFolder.resolve("config.yml").toFile();
        var config = new ViaBedrockConfig(configFile);
        config.reload();
        config.set("translate-resource-packs", false);
        config.save();
        init(configFile);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public File getDataFolder() {
        return dataFolder.toFile();
    }
}
