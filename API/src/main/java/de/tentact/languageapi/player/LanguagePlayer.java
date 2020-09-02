package de.tentact.languageapi.player;

import de.tentact.languageapi.i18n.Translation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * You can get this interface by {@link PlayerManager#getLanguagePlayer(UUID)} or {@link PlayerExecutor#getLanguagePlayer(UUID)}
 */
public interface LanguagePlayer extends LanguageOfflinePlayer {
    /**
     * Sends a message to the player by a {@link Translation}
     * @param translation the {@link Translation} to get the translated message from
     */
    void sendMessage(@NotNull Translation translation);

    /**
     * Sends a message to the player by a translationkey
     * @param transkey the translationkey to get the translation from
     */
    void sendMessageByKey(@NotNull String transkey);
    /**
     * Sends a message to the player by a translationkey
     * @param transkey the translationkey to get the translation from
     * @param usePrefix whether to use the default languageapi prefix
     */
    void sendMessageByKey(@NotNull String transkey, boolean usePrefix);

    /**
     * Sends multiple messages to the player by a single multipleTranslationKey
     * @param multipleTranslationKey the multipleTranslationKey to get the Collection of translationkeys
     */
    void sendMultipleTranslation(@NotNull String multipleTranslationKey);

    /**
     * Sends multiple messages to the player by a single {@link Translation}
     * @param multipleTranslation the multipleTranslation to get the Collection of translationkeys
     */
    void sendMultipleTranslation(@NotNull Translation multipleTranslation);

    /**
     * Sends multiple messages to the player by a single multipleTranslationKey
     * @param multipleTranslationKey the multipleTranslationKey to get the Collection of translationkeys
     * @param language the language to get the translation in
     */
    void sendMultipleTranslation(@NotNull String multipleTranslationKey, @NotNull String language);

    /**
     * Kick a player with a {@link Translation} as reason
     * @param translation the {@link Translation} to get the translated message from
     */
    void kickPlayer(Translation translation);


}
