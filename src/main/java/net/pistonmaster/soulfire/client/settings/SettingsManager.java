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
package net.pistonmaster.soulfire.client.settings;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.pistonmaster.soulfire.server.settings.lib.ProfileDataStructure;
import net.pistonmaster.soulfire.server.settings.lib.SettingsHolder;
import net.pistonmaster.soulfire.server.settings.lib.property.PropertyKey;

import javax.inject.Provider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public class SettingsManager {
    private final Multimap<PropertyKey, Consumer<JsonElement>> listeners = Multimaps.newListMultimap(new LinkedHashMap<>(), ArrayList::new);
    private final Map<PropertyKey, Provider<JsonElement>> providers = new LinkedHashMap<>();
    @Getter
    private final AccountRegistry accountRegistry = new AccountRegistry();
    @Getter
    private final ProxyRegistry proxyRegistry = new ProxyRegistry();

    public void registerProvider(PropertyKey property, Provider<JsonElement> provider) {
        providers.put(property, provider);
    }

    public void registerListener(PropertyKey property, Consumer<JsonElement> listener) {
        listeners.put(property, listener);
    }

    public void loadProfile(Path path) throws IOException {
        SettingsHolder.createSettingsHolder(ProfileDataStructure.deserialize(Files.readString(path)),
                listeners, accounts -> {
                    accountRegistry.setAccounts(accounts);
                    accountRegistry.callLoadHooks();
                }, proxies -> {
                    proxyRegistry.setProxies(proxies);
                    proxyRegistry.callLoadHooks();
                });
    }

    public void saveProfile(Path path) throws IOException {
        Files.createDirectories(path.getParent());

        Files.writeString(path, exportSettings());
    }

    public String exportSettings() {
        var settingsData = new LinkedHashMap<String, Map<String, JsonElement>>();
        for (var providerEntry : providers.entrySet()) {
            var property = providerEntry.getKey();
            var provider = providerEntry.getValue();

            var namespace = property.namespace();
            var settingId = property.key();
            var value = provider.get();

            settingsData.computeIfAbsent(namespace, k -> new LinkedHashMap<>())
                    .put(settingId, value);
        }

        return new ProfileDataStructure(
                settingsData,
                accountRegistry.getAccounts(),
                proxyRegistry.getProxies()
        ).serialize();
    }
}
