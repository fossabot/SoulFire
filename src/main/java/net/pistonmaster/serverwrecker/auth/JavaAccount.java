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
package net.pistonmaster.serverwrecker.auth;

import java.util.UUID;

public record JavaAccount(AuthType authType, String username, UUID profileId, String authToken, long tokenExpireAt, boolean enabled) {
    public JavaAccount {
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null!");
        }
    }

    public JavaAccount(String username) {
        this(AuthType.OFFLINE, username, null, null, -1, true);
    }

    public boolean isPremium() {
        return profileId != null && authToken != null;
    }

    public boolean isTokenExpired() {
        return tokenExpireAt != -1 && System.currentTimeMillis() > tokenExpireAt;
    }

    @Override
    public String toString() {
        return String.format("JavaAccount(username=%s, profileId=%s, authToken=REDACTED)", username, profileId);
    }
}
