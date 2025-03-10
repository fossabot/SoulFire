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

import com.github.steveice10.mc.protocol.data.game.entity.RotationOrigin;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lenni0451.lambdaevents.EventHandler;
import net.pistonmaster.soulfire.server.api.PluginHelper;
import net.pistonmaster.soulfire.server.api.SoulFireAPI;
import net.pistonmaster.soulfire.server.api.event.bot.BotPreEntityTickEvent;
import net.pistonmaster.soulfire.server.api.event.lifecycle.SettingsRegistryInitEvent;
import net.pistonmaster.soulfire.server.protocol.bot.state.TickHookContext;
import net.pistonmaster.soulfire.server.settings.lib.SettingsObject;
import net.pistonmaster.soulfire.server.settings.lib.property.*;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class KillAura implements InternalExtension {
    @EventHandler
    public static void onSettingsManagerInit(SettingsRegistryInitEvent event) {
        event.settingsRegistry().addClass(KillAuraSettings.class, "Kill Aura");
    }

    @Override
    public void onLoad() {
        SoulFireAPI.registerListeners(KillAura.class);
        PluginHelper.registerBotEventConsumer(BotPreEntityTickEvent.class, event -> {
            var bot = event.connection();
            if (!bot.settingsHolder().get(KillAuraSettings.ENABLE)) return;

            var control = bot.botControl();

            var whitelistedUser = bot.settingsHolder().get(KillAuraSettings.WHITELISTED_USER);

            var lookRange = bot.settingsHolder().get(KillAuraSettings.LOOK_RANGE);
            var hitRange = bot.settingsHolder().get(KillAuraSettings.HIT_RANGE);
            var swingRange = bot.settingsHolder().get(KillAuraSettings.SWING_RANGE);

            var max = Math.max(lookRange, Math.max(hitRange, swingRange));

            var target = control.getClosestEntity(max, whitelistedUser, true, true, bot.settingsHolder().get(KillAuraSettings.CHECK_WALLS));

            if (target == null) return;

            double distance;
            var bestVisiblePoint = control.getEntityVisiblePoint(target);
            if (bestVisiblePoint != null) {
                distance = bestVisiblePoint.distance(
                        bot.sessionDataManager().clientEntity().getEyePosition()
                );
            } else {
                distance = target.getEyePosition().distance(
                        bot.sessionDataManager().clientEntity().getEyePosition()
                );
            }

            if (distance > lookRange) {
                return;
            }

            if (bestVisiblePoint != null) {
                bot.sessionDataManager().clientEntity().lookAt(RotationOrigin.EYES, bestVisiblePoint);
            } else {
                bot.sessionDataManager().clientEntity().lookAt(RotationOrigin.EYES, RotationOrigin.EYES, target);
            }

            TickHookContext.INSTANCE.get().addHook(TickHookContext.HookType.POST_ENTITY_TICK, () -> {
                if (control.attackCooldownTicks() > 0) return;

                var swing = distance <= swingRange;
                if (distance <= hitRange) {
                    control.attack(target, swing);
                } else if (swing) {
                    control.swingArm();
                }

                if (bot.meta().protocolVersion().olderThan(ProtocolVersion.v1_9)
                        || bot.settingsHolder().get(KillAuraSettings.IGNORE_COOLDOWN)) {
                    var randomTickDelay = ThreadLocalRandom.current().nextDouble(
                            bot.settingsHolder().get(KillAuraSettings.ATTACK_DELAY_TICKS.min()),
                            bot.settingsHolder().get(KillAuraSettings.ATTACK_DELAY_TICKS.max())
                    );
                    control.attackCooldownTicks((int) randomTickDelay);
                }
            });
        });
    }

    @NoArgsConstructor(access = AccessLevel.NONE)
    private static class KillAuraSettings implements SettingsObject {
        private static final Property.Builder BUILDER = Property.builder("kill-aura");
        public static final BooleanProperty ENABLE = BUILDER.ofBoolean(
                "enable",
                "Enable",
                new String[]{"--kill-aura"},
                "Enable KillAura",
                false
        );
        public static final StringProperty WHITELISTED_USER = BUILDER.ofString(
                "whitelisted-user",
                "Whitelisted User",
                new String[]{"--kill-aura-whitelisted-user", "--kill-aura-whitelisted-username", "--kwu"},
                "This user will be ignored by the kill aura",
                "Pansexuel"
        );
        public static final DoubleProperty HIT_RANGE = BUILDER.ofDouble(
                "hit-range",
                "Hit Range",
                new String[]{"--kill-aura-hit-range", "--kill-aura-hit-distance", "--khr"},
                "Distance for the kill aura where the bot will start hitting the entity",
                3.0d,
                0.5d,
                6.0d,
                0.1d
        );
        public static final DoubleProperty SWING_RANGE = BUILDER.ofDouble(
                "swing-range",
                "Swing Range",
                new String[]{"--kill-aura-swing-range", "--kill-aura-swing-distance", "--ksr"},
                "Distance for the kill aura where the bot will start swinging arm, set to 0 to disable",
                3.5d,
                0.0d,
                10.0d,
                0.0d
        );
        public static final DoubleProperty LOOK_RANGE = BUILDER.ofDouble(
                "look-range",
                "Look Range",
                new String[]{"--kill-aura-look-range", "--kill-aura-look-distance", "--klr"},
                "Distance for the kill aura where the bot will start looking at the entity, set to 0 to disable",
                4.8d,
                0.0d,
                25.0d,
                0.0d
        );
        public static final BooleanProperty CHECK_WALLS = BUILDER.ofBoolean(
                "check-walls",
                "Check Walls",
                new String[]{"--kill-aura-check-walls", "--kill-aura-cw"},
                "Check if the entity is behind a wall",
                true
        );
        public static final BooleanProperty IGNORE_COOLDOWN = BUILDER.ofBoolean(
                "ignore-cooldown",
                "Ignore Cooldown",
                new String[]{"--kill-aura-ignore-cooldown", "--kill-aura-ic"},
                "Ignore the 1.9+ attack cooldown to act like a 1.8 kill aura",
                false
        );
        public static final MinMaxPropertyLink ATTACK_DELAY_TICKS = new MinMaxPropertyLink(
                BUILDER.ofInt(
                        "attack-delay-ticks-min",
                        "Attack Delay Ticks Min",
                        new String[]{"--kill-aura-adt-min"},
                        "Minimum tick delay between attacks on pre-1.9 versions",
                        8,
                        1,
                        20,
                        1
                ),
                BUILDER.ofInt(
                        "attack-delay-ticks-max",
                        "Attack Delay Ticks Max",
                        new String[]{"--kill-aura-adt-max"},
                        "Maximum tick delay between attacks on pre-1.9 versions",
                        12,
                        1,
                        20,
                        1
                )
        );
    }
}
