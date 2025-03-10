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
package net.pistonmaster.soulfire.server.api.event.bot;

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundPlayerChatPacket;
import net.kyori.adventure.text.Component;
import net.pistonmaster.soulfire.server.SoulFireServer;
import net.pistonmaster.soulfire.server.api.event.SoulFireBotEvent;
import net.pistonmaster.soulfire.server.protocol.BotConnection;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * This event is called when a chat message is received from the server.
 *
 * @param connection The bot connection instance.
 * @param timestamp  The timestamp when the message was received.
 * @param message    The message that was received.
 * @param sender     The sender of the message or null if it's from the server.
 */
public record ChatMessageReceiveEvent(
        BotConnection connection,
        long timestamp,
        Component message,
        @Nullable ChatMessageSender sender
) implements SoulFireBotEvent {
    public String parseToText() {
        return SoulFireServer.PLAIN_MESSAGE_SERIALIZER.serialize(message);
    }

    public boolean isFromPlayer() {
        return sender != null;
    }

    public record ChatMessageSender(UUID senderUUID, String senderName) {
        public static ChatMessageSender fromClientboundPlayerChatPacket(ClientboundPlayerChatPacket packet) {
            var senderUUID = packet.getSender();
            var senderName = SoulFireServer.PLAIN_MESSAGE_SERIALIZER.serialize(packet.getName());
            return new ChatMessageSender(senderUUID, senderName);
        }
    }
}
