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

package de.tentact.languageapi.i18n;

import de.tentact.languageapi.LanguageAPI;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This interface can only be accessed via {@link LanguageAPI#getTranslation(String)}
 * Used to store translationkey and send them directly to a player instead of fetching
 */
public interface Translation {

    /**
     * Sends this {@link Translation} to the console
     * @since 1.9
     */
    void sendToConsole();

    /**
     * @return returns a translation of the key in the default language
     */
    @NotNull
    String getMessage();

    /**
     * @return returns a translation of the key in the default language
     */
    @NotNull
    CompletableFuture<String> getMessageAsync();

    /**
     * @param playerUUID the player's uniqueid to fetch the language from
     * @return returns a translation of the key in the language fetched by @link UUID
     */
    @NotNull
    String getMessage(@NotNull UUID playerUUID);

    /**
     * @param playerUUID the player's uniqueid to fetch the language from
     * @return returns a translation of the key in the language fetched by @link UUID
     */
    @NotNull
    CompletableFuture<String> getMessageAsync(@NotNull UUID playerUUID);

    /**
     * @param language the language to get the translation in
     * @return returns a translation of the key in the given language
     */
    @NotNull
    default String getMessage(@NotNull String language) {
        return this.getMessage(language, false);
    }

    /**
     * @param language the language to get the translation in
     * @return returns a translation of the key in the given language
     */
    @NotNull
    default CompletableFuture<String> getMessageAsync(@NotNull String language) {
        return this.getMessageAsync(language, false);
    }

    /**
     * @param language      the language to get the translation in
     * @param orElseDefault whether to use the default language if the given one was not found
     * @return returns a translation of the key in the given language if found, else uses default language if orElseDefault is {@code true}
     */
    @NotNull
    String getMessage(@NotNull String language, boolean orElseDefault);

    /**
     * @param language      the language to get the translation in
     * @param orElseDefault whether to use the default language if the given one was not found
     * @return returns a translation of the key in the given language if found, else uses default language if orElseDefault is {@code true}
     */
    @NotNull
    CompletableFuture<String> getMessageAsync(@NotNull String language, boolean orElseDefault);

    /**
     * @return returns all parameters for the key in the {@link Translation#getTranslationKey()}
     * @deprecated use {@link Translation#getParameterAsList()}
     */
    @Nullable
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0")
    String getParameter();

    /**
     * @return returns all parameters for the key in the {@link Translation#getTranslationKey()} as list
     */
    List<String> getParameterAsList();

    /**
     * @return returns all parameters for the key in the {@link Translation#getTranslationKey()}
     * @deprecated {@link Translation#getParameterAsListAsync()}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0")
    CompletableFuture<String> getParameterAsync();

    /**
     * @return returns all parameters for the key in the {@link Translation#getTranslationKey()}
     */
    CompletableFuture<List<String>> getParameterAsListAsync();

    /**
     * @param prefixTranslation the prefix translation to get the prefix from
     * @return returns a {@link Translation} after setting the prefixTranslation
     */
    @NotNull
    Translation setPrefixTranslation(Translation prefixTranslation);

    /**
     * Get the prefixTranslation which was set with {@link Translation#setPrefixTranslation(Translation)}
     * @return returns the prefixTranslation, null if the translation was never set
     */
    @Nullable
    Translation getPrefixTranslation();

    /**
     * @return if the translation has a {@link Translation} as prefix set
     * @since 1.9
     */
    boolean hasPrefix();

    /**
     * a method to replace parameter in the specific translation for a player - this is resetted after {@link Translation#getMessage()}
     * @param old         the old String to replace
     * @param replacement the replacement for the paramater
     * @return returns {@link Translation} after replacing the parameter
     */
    @NotNull
    Translation replace(String old, String replacement);

    /**
     * @return returns the translationkey which was given
     */
    @NotNull
    String getTranslationKey();

    /**
     * Create the default translation for the {@link Translation}
     * @param message the default translation
     * @return the {@link Translation} after setting the default translation
     */
    @NotNull
    Translation createDefaults(String message);

    /**
     * Create the default translation for the {@link Translation}
     *
     * @param message the default translation
     * @param param   the parameter of the translation
     * @return the {@link Translation} after setting the default translation
     * @deprecated use {@link Translation#createDefaults(String, List)}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0")
    @NotNull
    Translation createDefaults(String message, String param);

    /**
     * Create the default translation for the {@link Translation}
     *
     * @param message the default translation
     * @param parameter   the parameters of the translation
     * @return the {@link Translation} after setting the default translation
     * @since 1.9
     */
    @NotNull
    Translation createDefaults(String message, List<String> parameter);

    /**
     * Adds a translation to the {@link Translation#getTranslationKey()} in the given language
     * @param language the language of the translation
     * @param message the translated message
     * @return the current {@link Translation}
     */
    @NotNull
    Translation addTranslation(String language, String message);

    /**
     * Adds a translation to the {@link Translation#getTranslationKey()} in the given language with parameters
     * @param language the language of the translation
     * @param message the translated message
     * @param param the parameters to the token
     * @return the current {@link Translation}
     * @deprecated use {@link Translation#addTranslation(String, String, List)}
     */
    @NotNull
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0")
    Translation addTranslation(String language, String message, String param);

    /**
     * Adds a translation to the {@link Translation#getTranslationKey()} in the given language with parameters
     * @param language the language of the translation
     * @param message the translated message
     * @param parameter the parameters to the token
     * @return the current {@link Translation}
     * @since 1.9
     */
    @NotNull
    Translation addTranslation(String language, String message, List<String> parameter);

}
