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
package net.pistonmaster.soulfire.server.api;

public enum AttackState {
    RUNNING,
    PAUSED,
    STOPPED;

    public boolean isRunning() {
        return this == RUNNING;
    }

    public boolean isPaused() {
        return this == PAUSED;
    }

    public boolean isStopped() {
        return this == STOPPED;
    }

    public boolean isInactive() {
        return isPaused() || isStopped();
    }
}
