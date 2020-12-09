package de.tentact.languageapi.player;
/*  Created in the IntelliJ IDEA.
    Copyright(c) 2020
    Created by 0utplay | Aldin Sijamhodzic
    Datum: 16.07.2020
    Uhrzeit: 23:12
*/

import de.tentact.languageapi.LanguageAPI;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DefaultLanguageOfflinePlayer implements LanguageOfflinePlayer {

    private final PlayerExecutor playerExecutor;

    private final UUID playerID;

    private final String language;

    public DefaultLanguageOfflinePlayer(@NotNull UUID playerID) {
        this.playerID = playerID;
        this.playerExecutor = LanguageAPI.getInstance().getPlayerExecutor();
        this.language = this.playerExecutor.getPlayerLanguage(playerID);
    }

    @Override
    public void setLanguage(@NotNull String language) {
        this.setLanguage(language, false);
    }

    @Override
    public void setLanguage(@NotNull String language, boolean orElseDefault) {
        if (!LanguageAPI.getInstance().isLanguage(language) && !orElseDefault) {
            throw new IllegalArgumentException(language + " was not found");
        }
        this.playerExecutor.setPlayerLanguage(this.playerID, language, orElseDefault);
    }

    @Override
    public @NotNull String getLanguage() {
        return this.language;
    }

    @Override
    public UUID getUniqueId() {
        return this.playerID;
    }

    @Override
    public boolean isOnline() {
        return this.playerExecutor.getLanguagePlayer(this.playerID) != null;
    }

}
