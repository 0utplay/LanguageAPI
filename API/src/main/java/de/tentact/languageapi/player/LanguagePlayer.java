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

import de.tentact.languageapi.i18n.Translation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Use {@link PlayerExecutor#getLanguagePlayer(UUID)} to get an instance
 *
 * @since 1.8
 */
public interface LanguagePlayer extends LanguageOfflinePlayer {

    /**
     * Sends a message to the player by a {@link Translation}
     *
     * @param translation the {@link Translation} to get the translated message from
     */
    void sendMessage(@NotNull Translation translation);

    /**
     * Sends a message to the player by a translationkey
     *
     * @param translationKey the translationkey to get the translation from
     * @since 1.9
     */
    void sendMessage(String translationKey);

    /**
     * Sends a message to the player by a translationkey
     *
     * @param translationKey the translationkey to get the translation from
     * @deprecated use {@link LanguagePlayer#sendMessage(String)} instead
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0")
    default void sendMessageByKey(@NotNull String translationKey) {
        this.sendMessage(translationKey);
    }

    /**
     * Sends multiple messages to the player by a single multipleTranslationKey
     *
     * @param multipleTranslationKey the multipleTranslationKey to get the Collection of translationkeys
     */
    default void sendMultipleTranslation(@NotNull String multipleTranslationKey) {
        this.sendMultipleTranslation(multipleTranslationKey, null);
    }

    /**
     * Sends multiple messages to the player by a single multipleTranslationKey
     *
     * @param multipleTranslationKey the multipleTranslationKey to get the Collection of translationkeys
     */
    void sendMultipleTranslation(@NotNull String multipleTranslationKey, String prefixKey);

    /**
     * Sends multiple messages to the player by a single {@link Translation}
     *
     * @param multipleTranslation the multipleTranslation to get the Collection of translationkeys
     */
    default void sendMultipleTranslation(@NotNull Translation multipleTranslation) {
        if (multipleTranslation.getPrefixTranslation() != null) {
            this.sendMultipleTranslation(multipleTranslation.getTranslationKey(), multipleTranslation.getPrefixTranslation().getTranslationKey());
        } else {
            this.sendMultipleTranslation(multipleTranslation.getTranslationKey());
        }
    }

    /**
     * Kick a player with a {@link Translation} as reason
     *
     * @param translation the {@link Translation} to get the translated message from
     */
    void kickPlayer(Translation translation);
}
