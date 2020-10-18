package de.tentact.languageapi.api;
/*  Created in the IntelliJ IDEA.
    Copyright(c) 2020
    Created by 0utplay | Aldin Sijamhodzic
    Datum: 16.10.2020
    Uhrzeit: 12:12
*/

import de.tentact.languageapi.LanguageAPI;
import de.tentact.languageapi.i18n.Translation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

public class DefaultTranslation implements Translation {

    private final String translationkey;
    private Translation prefixTranslation = null;
    private final LanguageAPI languageAPI = LanguageAPI.getInstance();
    private final HashMap<String, String> params = new HashMap<>();
    private String message;

    public DefaultTranslation(@NotNull String translationkey) {
        this.translationkey = translationkey;
    }

    @NotNull
    @Override
    public String getMessage() {
        return this.getMessage(this.languageAPI.getDefaultLanguage());
    }

    @NotNull
    @Override
    public String getMessage(@NotNull UUID playerUUID) {
        return this.getMessage(this.languageAPI.getPlayerExecutor().getPlayerLanguage(playerUUID));
    }

    @NotNull
    @Override
    public String getMessage(@NotNull String language, boolean orElseDefault) {
        String prefix = "";
        if (this.hasPrefixTranslation()) {
            prefix = this.prefixTranslation.getMessage(language, orElseDefault);
        }
        message = this.languageAPI.getMessage(this.translationkey, language);
        params.forEach((key, value) -> message = message.replace(key, value));
        params.clear();
        return prefix + message;
    }

    @Override
    public String getParameter() {
        return this.languageAPI.getParameter(this.translationkey);
    }

    @Override
    public Translation setPrefixTranslation(Translation prefixTranslation) {
        this.prefixTranslation = prefixTranslation;
        this.updateTranslation();
        return this;
    }

    @Override
    public @Nullable Translation getPrefixTranslation() {
        return this.prefixTranslation;
    }

    @Override
    public Translation replace(String old, String replacement) {
        params.put(old, replacement);
        return this;
    }

    @Override
    public String getTranslationKey() {
        return this.translationkey;
    }

    @Override
    public Translation createDefaults(String message) {
        this.languageAPI.addMessageToDefault(this.translationkey, message);
        return this;
    }

    @Override
    public Translation createDefaults(String message, String param) {
        this.languageAPI.addMessageToDefault(this.translationkey, message, param);
        return this;
    }

    @Override
    public Translation addTranslation(String language, String message) {
        return this.addTranslation(language, message, null);
    }

    @Override
    public Translation addTranslation(String language, String message, String param) {
        this.languageAPI.addMessage(this.translationkey, message, language, param);
        return this;
    }

    private void updateTranslation() {
        this.languageAPI.updateTranslation(this);
    }

    private boolean hasPrefixTranslation() {
        return this.prefixTranslation != null;
    }
}