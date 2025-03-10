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

import com.github.steveice10.mc.protocol.packet.common.serverbound.ServerboundCustomPayloadPacket;
import com.github.steveice10.mc.protocol.packet.login.serverbound.ServerboundLoginAcknowledgedPacket;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.lenni0451.lambdaevents.EventHandler;
import net.pistonmaster.soulfire.server.api.PluginHelper;
import net.pistonmaster.soulfire.server.api.SoulFireAPI;
import net.pistonmaster.soulfire.server.api.event.bot.SFPacketSentEvent;
import net.pistonmaster.soulfire.server.api.event.lifecycle.SettingsRegistryInitEvent;
import net.pistonmaster.soulfire.server.settings.lib.SettingsObject;
import net.pistonmaster.soulfire.server.settings.lib.property.BooleanProperty;
import net.pistonmaster.soulfire.server.settings.lib.property.Property;
import net.pistonmaster.soulfire.server.settings.lib.property.StringProperty;

public class ClientBrand implements InternalExtension {
    public static void onPacket(SFPacketSentEvent event) {
        if (event.packet() instanceof ServerboundLoginAcknowledgedPacket) {
            var connection = event.connection();
            var settingsHolder = connection.settingsHolder();

            if (!settingsHolder.get(ClientBrandSettings.ENABLED)) {
                return;
            }

            var buf = Unpooled.buffer();
            connection.session().getCodecHelper()
                    .writeString(buf, settingsHolder.get(ClientBrandSettings.CLIENT_BRAND));

            connection.session().send(new ServerboundCustomPayloadPacket(
                    "minecraft:brand",
                    ByteBufUtil.getBytes(buf)
            ));
        }
    }

    @EventHandler
    public static void onSettingsManagerInit(SettingsRegistryInitEvent event) {
        event.settingsRegistry().addClass(ClientBrandSettings.class, "Client Brand");
    }

    @Override
    public void onLoad() {
        SoulFireAPI.registerListeners(ClientBrand.class);
        PluginHelper.registerBotEventConsumer(SFPacketSentEvent.class, ClientBrand::onPacket);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static class ClientBrandSettings implements SettingsObject {
        private static final Property.Builder BUILDER = Property.builder("client-brand");
        public static final BooleanProperty ENABLED = BUILDER.ofBoolean(
                "enabled",
                "Send client brand",
                new String[]{"--send-client-brand"},
                "Send client brand to the server",
                true
        );
        public static final StringProperty CLIENT_BRAND = BUILDER.ofString(
                "client-brand",
                "Client brand",
                new String[]{"--client-brand"},
                "The client brand to send to the server",
                "vanilla"
        );
    }
}
