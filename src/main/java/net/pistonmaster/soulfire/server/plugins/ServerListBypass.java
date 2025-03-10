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
package net.pistonmaster.soulfire.server.plugins;

import com.github.steveice10.mc.protocol.data.ProtocolState;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.lenni0451.lambdaevents.EventHandler;
import net.pistonmaster.soulfire.server.api.PluginHelper;
import net.pistonmaster.soulfire.server.api.SoulFireAPI;
import net.pistonmaster.soulfire.server.api.event.attack.PreBotConnectEvent;
import net.pistonmaster.soulfire.server.api.event.lifecycle.SettingsRegistryInitEvent;
import net.pistonmaster.soulfire.server.settings.lib.SettingsObject;
import net.pistonmaster.soulfire.server.settings.lib.property.BooleanProperty;
import net.pistonmaster.soulfire.server.settings.lib.property.MinMaxPropertyLink;
import net.pistonmaster.soulfire.server.settings.lib.property.Property;
import net.pistonmaster.soulfire.server.util.RandomUtil;
import net.pistonmaster.soulfire.server.util.TimeUtil;

import java.util.concurrent.TimeUnit;

public class ServerListBypass implements InternalExtension {
    public static void onPreConnect(PreBotConnectEvent event) {
        var connection = event.connection();
        if (connection.meta().targetState() == ProtocolState.STATUS) {
            return;
        }

        var factory = connection.factory();
        var settingsHolder = connection.settingsHolder();
        if (!settingsHolder.get(ServerListBypassSettings.ENABLED)) {
            return;
        }

        factory.prepareConnectionInternal(ProtocolState.STATUS).connect().join();
        TimeUtil.waitTime(RandomUtil.getRandomInt(settingsHolder.get(ServerListBypassSettings.DELAY.min()),
                settingsHolder.get(ServerListBypassSettings.DELAY.max())), TimeUnit.SECONDS);
    }

    @EventHandler
    public static void onSettingsManagerInit(SettingsRegistryInitEvent event) {
        event.settingsRegistry().addClass(ServerListBypassSettings.class, "Server List Bypass");
    }

    @Override
    public void onLoad() {
        SoulFireAPI.registerListeners(ServerListBypass.class);
        PluginHelper.registerAttackEventConsumer(PreBotConnectEvent.class, ServerListBypass::onPreConnect);
    }

    @NoArgsConstructor(access = AccessLevel.NONE)
    private static class ServerListBypassSettings implements SettingsObject {
        private static final Property.Builder BUILDER = Property.builder("server-list-bypass");
        public static final BooleanProperty ENABLED = BUILDER.ofBoolean("enabled",
                "Enable Server List Bypass",
                new String[]{"--server-list-bypass"},
                "Whether to ping the server list before connecting. (Bypasses anti-bots like EpicGuard)",
                false
        );
        public static final MinMaxPropertyLink DELAY = new MinMaxPropertyLink(
                BUILDER.ofInt(
                        "min-delay",
                        "Min delay (seconds)",
                        new String[]{"--server-list-bypass-min-delay"},
                        "Minimum delay between joining the server",
                        1,
                        0,
                        Integer.MAX_VALUE,
                        1
                ),
                BUILDER.ofInt(
                        "max-delay",
                        "Max delay (seconds)",
                        new String[]{"--server-list-bypass-max-delay"},
                        "Maximum delay between joining the server",
                        3,
                        0,
                        Integer.MAX_VALUE,
                        1
                )
        );
    }
}
