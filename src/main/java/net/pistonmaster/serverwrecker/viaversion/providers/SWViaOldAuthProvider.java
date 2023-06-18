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
package net.pistonmaster.serverwrecker.viaversion.providers;

import com.viaversion.viaversion.api.connection.UserConnection;
import net.pistonmaster.serverwrecker.protocol.netty.ViaClientSession;
import net.pistonmaster.serverwrecker.viaversion.StorableSession;
import net.raphimc.vialegacy.protocols.release.protocol1_3_1_2to1_2_4_5.providers.OldAuthProvider;

import java.util.Objects;

public class SWViaOldAuthProvider extends OldAuthProvider {
    @Override
    public void sendAuthRequest(UserConnection user, String serverId) {
        ViaClientSession session = Objects.requireNonNull(user.get(StorableSession.class)).session();
        session.getMeta().joinServerId(serverId, session);
    }
}
