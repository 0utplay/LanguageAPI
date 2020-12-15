/*
 * MIT License
 *
 * Copyright (c) 2020 0utplay
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
import de.tentact.languageapi.i18n.Translation;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DefaultLanguagePlayer extends DefaultLanguageOfflinePlayer implements LanguagePlayer {

    private final UUID playerID;
    private ProxiedPlayer proxiedPlayer;
    private final LanguageAPI languageAPI = LanguageAPI.getInstance();

    public DefaultLanguagePlayer(UUID playerID) {
        super(playerID);
        this.playerID = playerID;
    }

    @Override
    public void sendMessage(@NotNull Translation translation) {
        ProxiedPlayer proxiedPlayer = this.getProxiedPlayer();
        if (proxiedPlayer == null) {
            return;
        }
        proxiedPlayer.sendMessage(translation.getMessage(this.getLanguage()));
    }


    @Override
    public void sendMessageByKey(@NotNull String translationKey) {
        this.sendMessage(this.languageAPI.getTranslation(translationKey));
    }

    @Override
    public void sendMultipleTranslation(@NotNull String multipleTranslationKey, @NotNull String language, String prefixKey) {
        if (!this.languageAPI.isMultipleTranslation(multipleTranslationKey)) {
            throw new IllegalArgumentException(multipleTranslationKey + " was not found");
        }
        ProxiedPlayer proxiedPlayer = this.getProxiedPlayer();
        if (proxiedPlayer == null) {
            return;
        }
        this.languageAPI.getMultipleMessages(multipleTranslationKey, language, prefixKey).forEach(proxiedPlayer::sendMessage);
    }


    @Override
    public void kickPlayer(Translation translation) {
        ProxiedPlayer proxiedPlayer = this.getProxiedPlayer();
        if (proxiedPlayer == null) {
            return;
        }
        proxiedPlayer.disconnect(translation.getMessage(this.getLanguage()));
    }

    private ProxiedPlayer getProxiedPlayer() {
        if (this.proxiedPlayer != null) {
            return this.proxiedPlayer;
        }
        this.proxiedPlayer = ProxyServer.getInstance().getPlayer(this.playerID);
        return this.proxiedPlayer;
    }

}
