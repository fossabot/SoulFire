/*
 * ServerWrecker
 *
 * Copyright (C) 2022 ServerWrecker
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
package net.pistonmaster.serverwrecker;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

import java.util.ArrayList;
import java.util.List;

public class SWConstants {
    public static final String VERSION = "1.0.0";
    public static final ProtocolVersion CURRENT_PROTOCOL_VERSION = ProtocolVersion.v1_19_4;
    public static final ProtocolVersion LATEST_SHOWN_VERSION = ProtocolVersion.v1_19_4;

    public static List<ProtocolVersion> getVersionsSorted() {
        List<ProtocolVersion> versions = new ArrayList<>();

        for (ProtocolVersion version : ProtocolVersion.getProtocols()) {
            versions.add(version);

            // We don't need to add any versions after the latest version (too experimental)
            if (version == LATEST_SHOWN_VERSION) {
                break;
            }
        }

        return versions;
    }
}
