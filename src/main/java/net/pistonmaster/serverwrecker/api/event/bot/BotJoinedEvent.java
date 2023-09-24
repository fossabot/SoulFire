/*
 * ServerWrecker
 *
 * Copyright (C) 2023 ServerWrecker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 */
package net.pistonmaster.serverwrecker.api.event.bot;

import net.pistonmaster.serverwrecker.api.event.ServerWreckerBotEvent;
import net.pistonmaster.serverwrecker.protocol.BotConnection;

/**
 * Called when the bot finished joining the server.
 * This event is called after the server confirms the spawn location and the "World Loading" screen is gone.
 *
 * @param connection The bot connection instance.
 */
public record BotJoinedEvent(BotConnection connection) implements ServerWreckerBotEvent {
}
