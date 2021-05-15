/*
 * MIT License
 *
 * Copyright (c) 2020 0utplay (Aldin Sijamhodzic)
 * Copyright (c) 2020 contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.tentact.languageapi.player;

import de.tentact.languageapi.LanguageAPI;
import de.tentact.languageapi.configuration.LanguageConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpigotPlayerExecutor extends DefaultPlayerExecutor {

    public SpigotPlayerExecutor(LanguageAPI languageAPI, LanguageConfig languageConfig) {
        super(languageAPI, languageConfig);
    }

    @Override
    public @Nullable LanguagePlayer getLanguagePlayer(UUID playerId) {
        LanguagePlayer languagePlayer = super.playerCache.getIfPresent(playerId);
        if (languagePlayer != null) {
            return languagePlayer;
        }

        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return null;
        }

        languagePlayer = new SpigotLanguagePlayer(player);
        super.playerCache.put(playerId, languagePlayer);
        return languagePlayer;
    }

    @Override
    public @NotNull Collection<LanguagePlayer> getOnlineLanguagePlayers() {
        return Bukkit.getOnlinePlayers().stream().map(player -> this.getLanguagePlayer(player.getUniqueId())).collect(Collectors.toList());
    }
}
