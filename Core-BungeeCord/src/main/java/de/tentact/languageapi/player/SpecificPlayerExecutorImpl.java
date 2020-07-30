package de.tentact.languageapi.player;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.tentact.languageapi.LanguageAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SpecificPlayerExecutorImpl extends PlayerManagerImpl implements SpecificPlayerExecutor {

    private final UUID playerId;
    private final PlayerExecutor playerExecutor = new PlayerExecutorImpl();
    private final LanguageAPI languageAPI = LanguageAPI.getInstance();


    public SpecificPlayerExecutorImpl(UUID playerId) {
        this.playerId = playerId;
    }

    @Override
    public @NotNull String getPlayerLanguage() {

        return this.playerExecutor.getPlayerLanguage(this.playerId);
    }

    @Override
    public boolean isPlayersLanguage(String language) {
        if (!this.languageAPI.isLanguage(language)) {
            return false;
        }
        return this.playerExecutor.getPlayerLanguage(this.playerId).equalsIgnoreCase(language);
    }

    @Override
    public void setPlayerLanguage(String newLanguage, boolean orElseDefault) {
        this.playerExecutor.setPlayerLanguage(this.playerId, newLanguage, orElseDefault);

    }

    @Override
    public void setPlayerLanguage(String newLanguage) {
        this.playerExecutor.setPlayerLanguage(this.playerId, newLanguage);
    }

    @Override
    public void registerPlayer() {
        this.playerExecutor.registerPlayer(this.playerId);
    }

    @Override
    public void registerPlayer(String language) {
        this.playerExecutor.registerPlayer(this.playerId, language);
    }

    @Override
    public boolean isRegisteredPlayer() {
        return this.playerExecutor.isRegisteredPlayer(this.playerId);
    }

    @Override
    public @Nullable LanguagePlayer getLanguagePlayer() {
        return this.playerExecutor.getLanguagePlayer(this.playerId);
    }

    @Override
    public @NotNull LanguageOfflinePlayer getLanguageOfflinePlayer() {
        return this.playerExecutor.getLanguageOfflinePlayer(this.playerId);
    }
}
