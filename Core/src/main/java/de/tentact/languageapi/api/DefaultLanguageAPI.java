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

package de.tentact.languageapi.api;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zaxxer.hikari.HikariDataSource;
import de.tentact.languageapi.LanguageAPI;
import de.tentact.languageapi.concurrent.LanguageFuture;
import de.tentact.languageapi.configuration.LanguageConfig;
import de.tentact.languageapi.configuration.MySQL;
import de.tentact.languageapi.file.FileHandler;
import de.tentact.languageapi.i18n.Translation;
import de.tentact.languageapi.player.DefaultSpecificPlayerExecutor;
import de.tentact.languageapi.player.PlayerExecutor;
import de.tentact.languageapi.player.SpecificPlayerExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class DefaultLanguageAPI extends LanguageAPI {

    private final MySQL mySQL;
    private final LanguageConfig languageConfig;

    private final Cache<String, Map<String, String>> translationCache;
    private final Map<String, Translation> translationMap;
    private final PlayerExecutor playerExecutor;
    private final FileHandler fileHandler;
    private final ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("LanguageAPI-Thread-%d").build());

    public DefaultLanguageAPI(LanguageConfig languageConfig) {
        this.languageConfig = languageConfig;
        this.playerExecutor = this.getPlayerExecutor();
        this.mySQL = languageConfig.getMySQL();
        this.translationCache = CacheBuilder.newBuilder().expireAfterWrite(languageConfig.getLanguageSetting().getCachedTime(), TimeUnit.MINUTES).build();
        this.translationMap = new HashMap<>();
        this.fileHandler = new DefaultFileHandler();
    }

    @Override
    public void createLanguage(final String language) {
        if (this.getAvailableLanguages().isEmpty() || !this.isLanguage(language)) {
            this.executorService.execute(() -> {
                this.mySQL.createTable(language.replace(" ", "").toLowerCase());
                try (Connection connection = this.getDataSource().getConnection();
                     PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO languages(language) VALUES (?)")) {
                    preparedStatement.setString(1, language.toLowerCase());
                    preparedStatement.execute();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                this.addMessage("languageapi-prefix", "&eLanguageAPI x &7", language);
                this.debug("Creating new language: " + language);
            });
        }
    }

    @Override
    public void deleteLanguage(String language) {
        if (!this.getDefaultLanguage().equalsIgnoreCase(language) && this.isLanguage(language)) {
            this.executorService.execute(() -> {
                try (Connection connection = this.getDataSource().getConnection()) {
                    try (PreparedStatement preparedStatement = connection.prepareStatement("DROP TABLE " + language.toLowerCase() + "; DELETE FROM languages WHERE language=?;")) {
                        preparedStatement.setString(1, language.toLowerCase());
                        preparedStatement.execute();
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                this.debug("Deleting language:" + language);
            });
        }
    }


    @Override
    public boolean addMessage(final String translationKey, final String message, final String language, String param) {
        if (!this.isLanguage(language)) {
            throw new IllegalArgumentException("Language " + language + " was not found!");
        }
        this.addParameter(translationKey, param);
        return this.addMessage(translationKey, message, language);
    }

    @Override
    public boolean addMessage(final String translationKey, final String message, final String language) {
        if (!this.isLanguage(language)) {
            return false;
        }
        if (this.isKey(translationKey, language)) {
            return false;
        }
        this.executorService.execute(() -> {
            try (Connection connection = this.getDataSource().getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + language.toLowerCase() + " (translationKey, translation) VALUES (?,?);")) {
                preparedStatement.setString(1, translationKey.toLowerCase());
                preparedStatement.setString(2, this.translateColorCode(message));
                preparedStatement.execute();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        return true;
    }

    @Override
    public void addParameter(final String translationKey, final String param) {
        if (param == null || param.isEmpty()) {
            return;
        }
        if (this.isParameter(translationKey, param)) {
            return;
        }
        String currentParameter = this.getParameter(translationKey);
        if (currentParameter == null) {
            this.setParameter(translationKey, param);
        } else {
            String joinedParameter = currentParameter.replace(" ", "");
            if (joinedParameter.endsWith(",")) {
                joinedParameter += param;
            } else {
                joinedParameter += "," + param;
            }
            this.setParameter(translationKey, joinedParameter);
        }
    }

    @Override
    public void setParameter(String translationKey, String param) {
        if (param == null || param.isEmpty()) {
            return;
        }
        this.executorService.execute(() -> {
            try (Connection connection = this.getDataSource().getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("REPLACE INTO Parameter(translationKey, param) VALUES (?,?);")) {
                preparedStatement.setString(1, translationKey.toLowerCase());
                preparedStatement.setString(2, param.replace(" ", ""));
                preparedStatement.execute();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    @Override
    public void deleteParameter(final String translationKey, final String param) {
        if (!this.hasParameter(translationKey)) {
            return;
        }
        if (!this.getParameter(translationKey).contains(param)) {
            return;
        }
        this.executorService.execute(() -> {
            try (Connection connection = this.getDataSource().getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Parameter SET param=? WHERE translationKey=?;")) {
                preparedStatement.setString(1, this.getParameter(translationKey).replace(param, ""));
                preparedStatement.setString(2, translationKey);
                preparedStatement.execute();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });

    }

    @Override
    public void deleteAllParameter(final String translationKey) {
        if (!this.hasParameter(translationKey)) {
            return;
        }
        this.executorService.execute(() -> {
            try (Connection connection = this.getDataSource().getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Parameter WHERE translationKey=?;")) {
                preparedStatement.setString(1, translationKey);
                preparedStatement.execute();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    @Override
    public boolean addMessage(final String translationKey, final String language) {
        return this.addMessage(translationKey, translationKey, language);
    }

    @Override
    public boolean addMessage(final String translationKey) {
        return this.addMessage(translationKey, translationKey, this.getDefaultLanguage());
    }

    @Override
    public boolean addMessageToDefault(final String translationKey, final String translation) {
        return this.addMessage(translationKey, translation, this.getDefaultLanguage());
    }

    @Override
    public boolean addMessageToDefault(final String translationKey, final String translation, final String param) {
        this.addParameter(translationKey, param);
        return this.addMessageToDefault(translationKey, translation);
    }

    @Override
    public void addTranslationKeyToMultipleTranslation(final String multipleTranslation, final String translationKey) {
        String[] translationKeys = new String[]{};
        try (Connection connection = this.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT translationKeys FROM MultipleTranslation WHERE multipleKey=?;")) {
            preparedStatement.setString(1, multipleTranslation.toLowerCase());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                translationKeys = resultSet.getString("translationKeys").split(",");
            }
            resultSet.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        List<String> translationKeysAsArrayList = new ArrayList<>(Arrays.asList(translationKeys));
        translationKeysAsArrayList.add(translationKey);
        this.setMultipleTranslation(multipleTranslation, translationKeysAsArrayList, true);
    }

    @Override
    public void copyLanguage(String langfrom, String langto) {
        if (!this.isLanguage(langfrom.toLowerCase()) || !this.isLanguage(langto.toLowerCase())) {
            throw new IllegalArgumentException("Language " + langfrom + " or " + langto + " was not found!");
        }
        try (Connection connection = this.getDataSource().getConnection()) {
            connection.createStatement().execute("INSERT IGNORE " + langto + " SELECT * FROM " + langfrom + ";");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public boolean hasParameter(String translationKey) {
        return this.hasParameterAsync(translationKey).getAfter(5, false);
    }

    @Override
    public LanguageFuture<Boolean> hasParameterAsync(String translationKey) {
        return LanguageFuture.supplyAsync(() -> {
            if(translationKey == null) {
                return false;
            }
            try (Connection connection = this.getDataSource().getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Parameter WHERE translationKey=?;")) {
                preparedStatement.setString(1, translationKey);
                ResultSet resultSet = preparedStatement.executeQuery();
                return resultSet.next();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return false;
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return false;
        });
    }

    @Override
    public @Nullable String getParameter(String translationKey) {
        return this.getParameterAsync(translationKey).getAfter(5, null);
    }

    @Override
    public @NotNull LanguageFuture<String> getParameterAsync(String translationKey) {
        return LanguageFuture.supplyAsync(() -> {
            if (!this.hasParameter(translationKey)) {
                return null;
            }
            try (Connection connection = this.getDataSource().getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT param FROM Parameter WHERE translationKey=?;")) {
                preparedStatement.setString(1, translationKey.toLowerCase());
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getString("param");
                }
                resultSet.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return null;
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public boolean isParameter(String translationKey, String parameter) {
        return this.isParameterAsync(translationKey, parameter).getAfter(5, false);
    }

    @Override
    public LanguageFuture<Boolean> isParameterAsync(String translationKey, String parameter) {
        return LanguageFuture.supplyAsync(() -> {
            if (!this.hasParameter(translationKey)) {
                return false;
            }
            return this.getParameter(translationKey).contains(parameter);
        });
    }

    @Override
    public void deleteMessageInEveryLang(String translationKey) {
        this.executorService.execute(() -> {
            for (String languages : this.getAvailableLanguages()) {
                if (this.isKey(translationKey, languages)) {
                    this.deleteMessage(translationKey, languages);
                }
            }
        });

    }

    @Override
    public void updateMessage(String translationKey, String message, String language) {
        if (!this.isLanguage(language)) {
            throw new IllegalArgumentException("Language " + language + " was not found!");
        }
        if (!this.isKey(translationKey, language)) {
            throw new IllegalArgumentException("Translationkey " + translationKey + " was not found!");
        }
        this.executorService.execute(() -> {
            try (Connection connection = this.getDataSource().getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + language + " SET translation=? WHERE translationKey=?;")) {
                preparedStatement.setString(1, this.translateColorCode(message));
                preparedStatement.setString(2, translationKey.toLowerCase());
                preparedStatement.execute();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        this.translationCache.invalidate(translationKey.toLowerCase());
    }

    @Override
    public void setMultipleTranslation(String multipleTranslation, List<String> translationKeys, boolean overwrite) {
        if (this.isMultipleTranslation(multipleTranslation)) {
            if (!overwrite) {
                return;
            }
            this.removeMultipleTranslation(multipleTranslation);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String translationKey : translationKeys) {
            stringBuilder.append(translationKey.toLowerCase()).append(",");
        }
        this.executorService.execute(() -> {
            try (Connection connection = this.getDataSource().getConnection();
                 PreparedStatement preparedStatement =
                         connection.prepareStatement("INSERT INTO MultipleTranslation(multipleKey, translationKeys) VALUES (?,?)")) {
                preparedStatement.setString(1, multipleTranslation);
                preparedStatement.setString(2, stringBuilder.toString());
                preparedStatement.execute();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });

    }


    @Override
    public void removeMultipleTranslation(final String multipleTranslation) {
        if (!this.isMultipleTranslation(multipleTranslation)) {
            throw new IllegalArgumentException("Multiple Translation " + multipleTranslation + " was not found");
        }
        this.executorService.execute(() -> {
            try (Connection connection = this.getDataSource().getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM MultipleTranslation WHERE multipleKey=?;")) {
                preparedStatement.setString(1, multipleTranslation);
                preparedStatement.execute();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });

    }

    @Override
    public void removeSingleTranslationFromMultipleTranslation(final String multipleTranslation, final String translationKey) {
        if (!this.isMultipleTranslation(multipleTranslation)) {
            throw new IllegalArgumentException(multipleTranslation + " was not found");
        }
        List<String> translationKeysAsArrayList = null;
        try (Connection connection = this.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT translationKeys FROM MultipleTranslation WHERE multipleKey=?")) {
            preparedStatement.setString(1, multipleTranslation.toLowerCase());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                translationKeysAsArrayList = new ArrayList<>(Arrays.asList(resultSet.getString("translationKeys").split(",")));
            }
            resultSet.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        if (translationKeysAsArrayList == null) {
            return;
        }
        translationKeysAsArrayList.remove(translationKey);
        this.setMultipleTranslation(multipleTranslation, translationKeysAsArrayList, true);
    }

    @Override
    public boolean isMultipleTranslation(final String multipleTranslation) {
        try (Connection connection = this.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM MultipleTranslation WHERE multipleKey=?;")) {
            preparedStatement.setString(1, multipleTranslation.toLowerCase());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    @Override
    public void deleteMessage(String translationKey, String language) {
        if (!this.isLanguage(language)) {
            throw new IllegalArgumentException("Language " + language + " was not found!");
        }
        if (!this.isKey(translationKey, language)) {
            throw new IllegalArgumentException("Translationkey " + translationKey + " was not found!");
        }
        try (Connection connection = this.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM ? WHERE translationKey=?;")) {
            preparedStatement.setString(1, language.toLowerCase());
            preparedStatement.setString(2, translationKey.toLowerCase());
            preparedStatement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public boolean isKey(String translationKey, String language) {
        return this.isKeyAsync(translationKey, language).getAfter(5, false);
    }

    public LanguageFuture<Boolean> isKeyAsync(String translationKey, String language) {
        return LanguageFuture.supplyAsync(() -> {
            try (Connection connection = this.getDataSource().getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + language.toLowerCase() + " WHERE translationKey=?;")) {
                preparedStatement.setString(1, translationKey.toLowerCase());
                ResultSet resultSet = preparedStatement.executeQuery();
                return resultSet.next();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return false;
        });
    }

    @NotNull
    @Override
    public String getMessage(String translationKey, UUID playerUUID) {
        return this.getMessageAsync(translationKey, playerUUID).getAfter(5, "");
    }

    @Override
    public @NotNull LanguageFuture<String> getMessageAsync(String translationkey, UUID playerUUID) {
        return LanguageFuture.supplyAsync(() ->
                this.getMessage(translationkey, this.playerExecutor.getPlayerLanguage(playerUUID)));
    }

    @NotNull
    @Override
    public List<String> getMultipleMessages(String translationKey) {
        return this.getMultipleMessagesAsync(translationKey).getAfter(5, Collections.emptyList());
    }

    @Override
    public @NotNull LanguageFuture<List<String>> getMultipleMessagesAsync(String translationKey) {
        return LanguageFuture.supplyAsync(() ->
                this.getMultipleMessages(translationKey, this.getDefaultLanguage())
        );
    }

    @NotNull
    @Override
    public List<String> getMultipleMessages(String translationKey, UUID playerUUID) {
        return this.getMultipleMessagesAsync(translationKey, playerUUID).getAfter(5, Collections.emptyList());
    }

    @Override
    public @NotNull LanguageFuture<List<String>> getMultipleMessagesAsync(String translationKey, UUID playerUUID) {
        return LanguageFuture.supplyAsync(() -> this.getMultipleMessages(translationKey, this.playerExecutor.getPlayerLanguage(playerUUID)));
    }

    @NotNull
    @Override
    public List<String> getMultipleMessages(String translationKey, String language) {
        return this.getMultipleMessagesAsync(translationKey, language).getAfter(5, Collections.emptyList());
    }

    @Override
    public @NotNull LanguageFuture<List<String>> getMultipleMessagesAsync(String translationKey, String language) {
        return LanguageFuture.supplyAsync(() -> this.getMultipleMessages(translationKey, language, ""));
    }

    @Override
    public @NotNull List<String> getMultipleMessages(String translationKey, String language, String prefixKey) {
        return this.getMultipleMessagesAsync(translationKey, language, prefixKey).getAfter(5, Collections.emptyList());
    }

    @Override
    public @NotNull LanguageFuture<List<String>> getMultipleMessagesAsync(String multipleKey, String language, String prefixKey) {
        return LanguageFuture.supplyAsync(() -> {
            List<String> resolvedMessages = new ArrayList<>();
            String[] translationKeys = new String[0];
            String prefix = "";
            if (prefixKey != null && !prefixKey.isEmpty()) {
                prefix = this.getMessage(prefixKey, language);
            }
            try (Connection connection = this.getDataSource().getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT translationKeys FROM MultipleTranslation WHERE multipleKey=?;")) {
                preparedStatement.setString(1, multipleKey.toLowerCase());
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    String column = resultSet.getString("translationKeys");
                    translationKeys = column.split(",");
                }
                resultSet.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            for (String translationKey : translationKeys) {
                resolvedMessages.add(prefix + this.getMessage(translationKey, language));
            }
            return resolvedMessages;
        });
    }

    @NotNull
    @Override
    public String getMessage(String translationKey, String language) {
        return this.getMessageAsync(translationKey, language).getAfter(5, translationKey);
    }

    @Override
    public @NotNull LanguageFuture<String> getMessageAsync(String translationKey, String language) {
        return LanguageFuture.supplyAsync(() -> {
            if (!this.isLanguage(language)) {
                throw new IllegalArgumentException(language + " was not found");
            }
            if (!this.isKey(translationKey, language)) {
                this.languageConfig.debug("Translationkey '" + translationKey + "' not found in language '" + language + "'");
                this.languageConfig.debug("As result you will get the translationkey as translation");
                return translationKey;
            }
            Map<String, String> cacheMap = this.translationCache.getIfPresent(translationKey.toLowerCase());
            if (cacheMap != null && cacheMap.containsKey(language)) {
                return cacheMap.get(language);
            }
            try (Connection connection = this.getDataSource().getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT translation FROM " + language.toLowerCase() + " WHERE translationKey=?;")) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String translation = this.translateColorCode(resultSet.getString("translation"));
                        Map<String, String> map = new HashMap<>(1);
                        map.put(language, translation);
                        this.translationCache.put(translationKey, map);
                        return translation;
                    }
                }
            } catch (SQLException throwables) {
                return translationKey;
            }
            return translationKey;
        });
    }

    @Override
    public boolean isLanguage(String language) {
        return this.isLanguageAsync(language).getAfter(5, false);
    }

    @Override
    public LanguageFuture<Boolean> isLanguageAsync(@Nullable String language) {
        return LanguageFuture.supplyAsync(() -> {
            if (language == null) {
                return false;
            }
            return this.getAvailableLanguages().contains(language.toLowerCase());
        });
    }

    @NotNull
    @Override
    public List<String> getAvailableLanguages() {
        return this.getAvailableLanguagesAsync().getAfter(5, Collections.emptyList());
    }

    @Override
    public @NotNull LanguageFuture<List<String>> getAvailableLanguagesAsync() {
        return LanguageFuture.supplyAsync(() -> {
            List<String> languages = new ArrayList<>();
            try (Connection connection = this.getDataSource().getConnection();
                 ResultSet resultSet = connection.createStatement().executeQuery("SELECT language FROM languages")) {
                while (resultSet.next()) {
                    languages.add(resultSet.getString("language").toLowerCase());
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return languages;
        });
    }

    @Override
    public @NotNull List<String> getAllTranslationKeys(String language) {
        return this.getAllTranslationsAsync(language).getAfter(5, Collections.emptyList());
    }

    @Override
    public @NotNull LanguageFuture<List<String>> getAllTranslationKeysAsync(String language) {
        return LanguageFuture.supplyAsync(() -> {
            List<String> keys = new ArrayList<>();
            if (this.isLanguage(language)) {
                try (Connection connection = this.getDataSource().getConnection();
                     ResultSet resultSet = connection.createStatement().executeQuery("SELECT translationKey FROM " + language)) {
                    while (resultSet.next()) {
                        keys.add(resultSet.getString("translationKey"));
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                return keys;
            }
            throw new IllegalArgumentException("Language " + language + " was not found");
        });
    }

    @Override
    public @NotNull List<String> getAllTranslations(String language) {
        return this.getAllTranslationsAsync(language).getAfter(5, Collections.emptyList());
    }

    @Override
    public @NotNull LanguageFuture<List<String>> getAllTranslationsAsync(String language) {
        return LanguageFuture.supplyAsync(() -> {
            List<String> messages = new ArrayList<>();
            if (this.isLanguage(language)) {
                try (Connection connection = this.getDataSource().getConnection();
                     ResultSet resultSet = connection.createStatement().executeQuery("SELECT translation FROM " + language)) {
                    while (resultSet.next()) {
                        messages.add(resultSet.getString("translation"));
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                return messages;
            }
            throw new IllegalArgumentException("Language " + language + " was not found");
        });
    }

    @Override
    public @NotNull Map<String, String> getKeysAndTranslations(String language) {
        return this.getKeysAndTranslationsAsync(language).getAfter(5, new HashMap<>());
    }

    @Override
    public @NotNull LanguageFuture<Map<String, String>> getKeysAndTranslationsAsync(String language) {
        return LanguageFuture.supplyAsync(() -> {
            if (!this.isLanguage(language)) {
                throw new IllegalArgumentException("Language " + language + " was not found");
            }
            Map<String, String> keysAndTranslations = new HashMap<>();
            this.getAllTranslationKeys(language).forEach(key -> keysAndTranslations.put(key, this.getMessage(key, language)));
            return keysAndTranslations;
        });
    }

    @Override
    public @NotNull String getDefaultLanguage() {
        return this.languageConfig.getLanguageSetting().getDefaultLanguage().toLowerCase();
    }

    @Override
    public @NotNull String getLanguageAPIPrefix() {
        return this.getLanguageAPIPrefix(this.getDefaultLanguage());
    }

    @Override
    public @NotNull String getLanguageAPIPrefix(String language) {
        return this.getMessage("languageapi-prefix", language);
    }

    @Override
    public @NotNull Translation getTranslation(@NotNull String translationKey) {
        if (this.translationMap.containsKey(translationKey)) {
            return this.translationMap.get(translationKey);
        }
        Translation translation = new DefaultTranslation(translationKey);
        this.updateTranslation(translation);
        return translation;
    }

    @Override
    public @NotNull Translation getTranslationWithPrefix(Translation prefixTranslation, String translationKey) {
        return this.getTranslation(translationKey).setPrefixTranslation(prefixTranslation);
    }

    @Override
    public abstract @NotNull PlayerExecutor getPlayerExecutor();

    @Override
    public @NotNull SpecificPlayerExecutor getSpecificPlayerExecutor(@NotNull UUID playerId) {
        return new DefaultSpecificPlayerExecutor(playerId);
    }

    @Override
    public void updateTranslation(Translation translation) {
        this.translationMap.put(translation.getTranslationKey(), translation);
    }

    @Override
    public FileHandler getFileHandler() {
        return this.fileHandler;
    }

    @Override
    public void executeAsync(Runnable command) {
        this.executorService.execute(command);
    }

    private HikariDataSource getDataSource() {
        return this.mySQL.getDataSource();
    }

    private void debug(String message) {
        this.languageConfig.debug(message);
    }

    //From Bungeecord
    private String translateColorCode(String textToTranslate) {
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = '§';
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }
}
