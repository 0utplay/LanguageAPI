package de.tentact.languageapi.api;
/*  Created in the IntelliJ IDEA.
    Created by 0utplay | Aldin Sijamhodzic
    Datum: 25.04.2020
    Uhrzeit: 16:52
*/

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.tentact.languageapi.AbstractLanguageAPI;
import de.tentact.languageapi.LanguageSpigot;
import de.tentact.languageapi.event.LanguageCopyEvent;
import de.tentact.languageapi.event.LanguageCreateEvent;
import de.tentact.languageapi.event.LanguageDeleteEvent;
import de.tentact.languageapi.event.LanguageUpdateTranslationEvent;
import de.tentact.languageapi.mysql.MySQL;
import de.tentact.languageapi.util.Source;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class LanguageAPI extends AbstractLanguageAPI {

    private final MySQL mySQL = Source.getMySQL();

    private final Cache<String, HashMap<String, String>> translationCache = CacheBuilder.newBuilder().expireAfterWrite(5L, TimeUnit.MINUTES).build();
    private final LanguageSpigot languageSpigot = LanguageSpigot.getPlugin(LanguageSpigot.class);
    private final PluginManager pluginManager = Bukkit.getPluginManager();

    @Override
    public void createLanguage(final String language) {
        if (this.getAvailableLanguages().isEmpty() || !this.isLanguage(language)) {
            this.mySQL.createTable(language.replace(" ", "").toLowerCase());
            this.mySQL.update("INSERT INTO languages(language) VALUES ('" + language.toLowerCase() + "')");
            logInfo("Creating new language:" +language);
            LanguageCreateEvent languageCreateEvent = new LanguageCreateEvent(language.toLowerCase());
            if(!languageCreateEvent.isCancelled()) {
                pluginManager.callEvent(languageCreateEvent);
            }
        }

    }

    @Override
    public void deleteLanguage(String language) {
        if (!this.getDefaultLanguage().equalsIgnoreCase(language) && this.isLanguage(language)) {
            this.mySQL.update("DROP TABLE " + language.toLowerCase());
            this.mySQL.update("DELETE FROM languages WHERE language='" + language.toLowerCase() + "'");
            logInfo("Deleting language:" +language);
            LanguageDeleteEvent languageDeleteEvent = new LanguageDeleteEvent(language.toLowerCase());
            if(!languageDeleteEvent.isCancelled()) {
                pluginManager.callEvent(languageDeleteEvent);
            }
        }

    }

    @Override
    public void setPlayerLanguage(UUID playerUUID, String newLanguage, boolean orElseDefault) {
        this.registerPlayer(playerUUID);
        if (!this.isLanguage(newLanguage)) {
            this.setPlayerLanguage(playerUUID, this.getDefaultLanguage());
            return;
        }
        this.mySQL.update("UPDATE choosenlang WHERE uuid='" + playerUUID.toString() + "' SET language='" + newLanguage.toLowerCase() + "';");
    }

    @Override
    public void setPlayerLanguage(UUID playerUUID, String newLanguage) {
        this.registerPlayer(playerUUID);
        if (!this.isLanguage(newLanguage)) {
            throw new IllegalArgumentException("Language " + newLanguage + " was not found!");
        }
        this.mySQL.update("UPDATE choosenlang WHERE uuid='" + playerUUID.toString() + "' SET language='" + newLanguage.toLowerCase() + "';");
    }

    @Override
    public void registerPlayer(UUID playerUUID) {
        this.registerPlayer(playerUUID, this.getDefaultLanguage());
    }

    @Override
    public void registerPlayer(UUID playerUUID, String language) {
        if (!this.isRegisteredPlayer(playerUUID)) {
            Bukkit.getScheduler().runTaskLater(languageSpigot, () -> {
                if (!this.isLanguage(language)) {
                    logInfo("Registering player with default language ("+this.getDefaultLanguage()+")");
                    new Thread(() -> this.mySQL.update("INSERT INTO choosenlang(uuid, language) VALUES ('" + playerUUID.toString() + "', '" + this.getDefaultLanguage() + "');")).start();
                    return;
                }
                new Thread(() -> this.mySQL.update("INSERT INTO choosenlang(uuid, language) VALUES ('" + playerUUID.toString() + "', '" + language.toLowerCase() + "');")).start();
                logInfo("Registering player with language: "+language);
            }, 50L);
        } else {
            if (!this.isLanguage(this.getPlayerLanguage(playerUUID))) {
                new Thread(() -> this.mySQL.update("UPDATE choosenlang SET language='" + this.getDefaultLanguage() + "' WHERE uuid='" + playerUUID.toString() + "';")).start();
                logInfo("Updating players selected language");
            }
        }
    }

    @Override
    public boolean isRegisteredPlayer(UUID playerUUID) {
        return this.mySQL.exists("SELECT * FROM choosenlang WHERE uuid='" + playerUUID.toString() + "';");
    }

    @Override
    public void addMessage(final String transkey, final String message, final String language, String param) {
        if (!this.isLanguage(language)) {
            throw new IllegalArgumentException("Language " + language + " was not found!");
        }
        this.addMessage(transkey, message, language);
        this.addParameter(transkey, param);
    }

    @Override
    public void addMessage(final String transkey, final String message, final String language) {
        if (this.isLanguage(language)) {
            if (!Source.isBungeeCordMode) {
                new Thread(() -> this.mySQL.update("INSERT INTO " + language.toLowerCase() + "(transkey, translation) VALUES ('" + transkey.toLowerCase() + "', '" + ChatColor.translateAlternateColorCodes('&', message) + "');")).start();
            } else {
                new Thread(() -> this.mySQL.update("INSERT INTO " + language.toLowerCase() + "(transkey, translation) VALUES ('" + transkey.toLowerCase() + "', '" + net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', message) + "');")).start();
            }

        }
    }

    @Override
    public void addParameter(final String transkey, final String param) {
        new Thread(() -> this.mySQL.update("INSERT INTO Parameter (transkey, param) VALUES ('" + transkey.toLowerCase() + "', '" + param + "');")).start();

    }

    @Override
    public void deleteParameter(final String transkey, final String param) {
        if (!this.hasParameter(transkey)) {
            return;
        }
        if (!this.getParameter(transkey).contains(param)) {
            return;
        }
        new Thread(() -> this.mySQL.update("UPDATE Parameter SET param='" + getParameter(transkey).replace(param, "") + "' WHERE transkey='" + transkey + "';")).start();

    }

    @Override
    public void deleteAllParameter(final String transkey) {
        if (!this.hasParameter(transkey)) {
            return;
        }
        new Thread(() -> this.mySQL.update("DELETE FROM Parameter WHERE transkey='" + transkey + "';")).start();

    }

    @Override
    public void addMessage(final String transkey, final String language) {
        if (!this.isLanguage(language)) {
            return;
        }
        if (this.isKey(transkey, language)) {
            return;
        }
        this.addMessage(transkey, transkey, language);

    }

    @Override
    public void addMessage(final String transkey) {
        if (this.isKey(transkey, this.getDefaultLanguage().toLowerCase())) {
            return;
        }
       this.addMessage(transkey, transkey, this.getDefaultLanguage());

    }

    @Override
    public void addMessageToDefault(final String transkey, final String translation) {
        if (this.isKey(transkey, this.getDefaultLanguage().toLowerCase())) {
            return;
        }
        this.addMessage(transkey, translation, this.getDefaultLanguage());

    }

    @Override
    public void addMessageToDefault(final String transkey, final String translation, final String param) {
        if (this.isKey(transkey, this.getDefaultLanguage().toLowerCase())) {
            return;
        }
        this.addMessageToDefault(transkey, translation);
        this.addParameter(transkey, param);

    }

    @Override
    public void addTranslationKeyToMultipleTranslation(final String multipleTranslation, final String transkey) {

        String[] translationKeys = new String[]{};
        try (Connection connection = this.mySQL.dataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT transkeys FROM MultipleTranslation WHERE multipleKey='" + multipleTranslation.toLowerCase() + "'");
            if (resultSet.next()) {
                translationKeys = resultSet.getString("transkeys").split(",");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        ArrayList<String> translationKeysAsArrayList = new ArrayList<>(Arrays.asList(translationKeys));
        translationKeysAsArrayList.add(transkey);
        this.setMultipleTranslation(multipleTranslation, translationKeysAsArrayList, true);
    }

    @Override
    public void copyLanguage(String langfrom, String langto) {
        if (!this.isLanguage(langfrom.toLowerCase()) || !this.isLanguage(langto.toLowerCase())) {
            throw new IllegalArgumentException("Language " + langfrom + " or " + langto + " was not found!");
        }
        this.mySQL.update("INSERT INTO " + langto.toLowerCase() + " SELECT * FROM " + langfrom.toLowerCase() + ";");
        LanguageCopyEvent languageCopyEvent = new LanguageCopyEvent(langfrom.toLowerCase(), langto.toLowerCase());
        if(!languageCopyEvent.isCancelled()) {
            pluginManager.callEvent(languageCopyEvent);
        }
    }

    @Override
    public boolean hasParameter(String translationKey) {
        return this.mySQL.exists("SELECT param FROM Parameter WHERE transkey='" + translationKey + "';");
    }

    @Override
    public String getParameter(String translationKey) {
        if (!this.hasParameter(translationKey)) {
            throw new IllegalArgumentException(translationKey + " has no parameter");
        }

        try (Connection connection = this.mySQL.dataSource.getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT param FROM Parameter WHERE transkey='" + translationKey.toLowerCase() + "';");
            if (rs.next()) {
                return rs.getString("param");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        throw new IllegalArgumentException(translationKey + " has no parameter");

    }

    @Override
    public boolean isParameter(String translationKey, String param) {
        return this.getParameter(translationKey).contains(param);
    }

    @Override
    public void deleteMessageInEveryLang(String transkey) {
        for (String langs : this.getAvailableLanguages()) {
            if (this.isKey(transkey, langs)) {
                this.deleteMessage(transkey, langs);
            }
        }
    }

    @Override
    public void updateMessage(String transkey, String language, String message) {
        if (!this.isLanguage(language)) {
            throw new IllegalArgumentException("Language " + language + " was not found!");
        }
        if (!this.isKey(transkey, language)) {
            throw new IllegalArgumentException("Translationkey " + transkey + " was not found!");
        }
        LanguageUpdateTranslationEvent languageUpdateTranslationEvent = new LanguageUpdateTranslationEvent(language, transkey, this.getMessage(transkey, language),message);
        if(!languageUpdateTranslationEvent.isCancelled()) {
            Bukkit.getPluginManager().callEvent(languageUpdateTranslationEvent);
        }
        new Thread(() -> this.mySQL.update("UPDATE " + language.toLowerCase() + " SET translation='" + ChatColor.translateAlternateColorCodes('&', message) + "' WHERE transkey='" + transkey.toLowerCase() + "';")).start();
        translationCache.invalidate(transkey.toLowerCase());

    }

    @Override
    public void setMultipleTranslation(String multipleTranslation, List<String> translationKeys, boolean overwrite) {
        if (isMultipleTranslation(multipleTranslation) && overwrite) {
            this.removeMultipleTranslation(multipleTranslation);
        }
        StringBuilder stringBuilder = new StringBuilder();

        for (String translationKey : translationKeys) {
            stringBuilder.append(translationKey.toLowerCase()).append(",");
        }
        new Thread(() -> this.mySQL.update("INSERT INTO MultipleTranslation(multipleKey, transkeys) VALUES ('" + multipleTranslation.toLowerCase() + "','" + stringBuilder.toString() + "');")).start();
    }

    @Override
    public void removeMultipleTranslation(final String multipleTranslation) {
        if (!isMultipleTranslation(multipleTranslation)) {
            throw new IllegalArgumentException(multipleTranslation + " was not found");
        }
        new Thread(() -> this.mySQL.update("DELETE FROM MultipleTranslation WHERE multipleKey='" + multipleTranslation + "';")).start();
    }

    @Override
    public void removeSingleTranslationFromMultipleTranslation(final String multipleTranslation, final String transkey) {
        if (!isMultipleTranslation(multipleTranslation)) {
            throw new IllegalArgumentException(multipleTranslation + " was not found");
        }

        ArrayList<String> translationKeysAsArrayList = null;
        try (Connection connection = this.mySQL.dataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT transkeys FROM MultipleTranslation WHERE multipleKey='" + multipleTranslation.toLowerCase() + "'");
            if (resultSet.next()) {
                translationKeysAsArrayList = new ArrayList<>(Arrays.asList(resultSet.getString("transkeys").split(",")));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        assert translationKeysAsArrayList != null;
        translationKeysAsArrayList.remove(transkey);
        this.setMultipleTranslation(multipleTranslation, translationKeysAsArrayList, true);
    }

    @Override
    public boolean isMultipleTranslation(final String multipleTranslation) {
        return this.mySQL.exists("SELECT * FROM MultipleTranslation WHERE multipleKey='" + multipleTranslation.toLowerCase() + "';");
    }

    @Override
    public void deleteMessage(String transkey, String language) {
        if (!this.isLanguage(language)) {
            throw new IllegalArgumentException("Language " + language + " was not found!");
        }
        if (!this.isKey(transkey, language)) {
            throw new IllegalArgumentException("Translationkey " + transkey + " was not found!");
        }
        new Thread(() -> this.mySQL.update("DELETE FROM " + language.toLowerCase() + " WHERE transkey='" + transkey.toLowerCase() + "';")).start();
    }

    @NotNull
    @Override
    public String getPlayerLanguage(UUID playerUUID) {
        if(!isRegisteredPlayer(playerUUID)) {
            this.registerPlayer(playerUUID);
        }

        try (Connection connection = this.mySQL.dataSource.getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT language FROM choosenlang WHERE uuid='" + playerUUID.toString() + "';");
            if (rs.next()) {

                return rs.getString("language").toLowerCase();
            }
        } catch (SQLException throwables) {
            return this.getDefaultLanguage();
        }
        return this.getDefaultLanguage();
    }

    @Override
    public boolean isKey(String transkey, String lang) {
        return this.mySQL.exists("SELECT * FROM " + lang.toLowerCase() + " WHERE transkey='" + transkey.toLowerCase() + "';");
    }

    @NotNull
    @Override
    public String getMessage(String transkey, UUID playerUUID, boolean usePrefix) {
        return this.getMessage(transkey, this.getPlayerLanguage(playerUUID), usePrefix);
    }

    @Override
    public String getMessage(String translationkey, String language, boolean usePrefix) {
        return usePrefix ? this.getPrefix(language) + this.getMessage(translationkey, language) : this.getMessage(translationkey, language);
    }


    @NotNull
    @Override
    public String getMessage(String transkey, UUID playerUUID) {
        return this.getMessage(transkey, this.getPlayerLanguage(playerUUID));
    }

    @NotNull
    @Override
    public ArrayList<String> getMultipleMessages(String transkey) {
        return this.getMultipleMessages(transkey, this.getDefaultLanguage());
    }

    @NotNull
    @Override
    public ArrayList<String> getMultipleMessages(String transkey, UUID playerUUID) {
        return this.getMultipleMessages(transkey, this.getPlayerLanguage(playerUUID));
    }

    @Override
    public ArrayList<String> getMultipleMessages(String transkey, String language) {
        return this.getMultipleMessages(transkey, language, false);
    }
    @Override
    public ArrayList<String> getMultipleMessages(String transkey, UUID playerUUID, boolean usePrefix) {
        return this.getMultipleMessages(transkey, this.getPlayerLanguage(playerUUID), usePrefix);
    }

    @NotNull
    @Override
    public ArrayList<String> getMultipleMessages(String transkey, String language, boolean usePrefix) {
        ArrayList<String> resolvedMessages = new ArrayList<>();
        String[] translationKeys = new String[]{};
        try (Connection connection = this.mySQL.dataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT transkeys FROM MultipleTranslation WHERE multipleKey='" + transkey.toLowerCase() + "'");
            if (resultSet.next()) {
                String mysqlString = resultSet.getString("transkeys");
                Bukkit.broadcastMessage(resultSet.getString("transkeys"));
                translationKeys = mysqlString.split(",");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        for (String translationKey : translationKeys) {
            resolvedMessages.add(this.getMessage(translationKey, language, usePrefix));
        }

        return resolvedMessages;
    }

    @NotNull
    @Override
    public String getMessage(String transkey, String lang) {
        if (!this.isLanguage(lang)) {
            throw new IllegalArgumentException(lang + " was not found");
        }
        if (!this.isKey(transkey, lang)) {
            throw new IllegalArgumentException(transkey + " not found for language " + lang);
        }
        if (this.translationCache.getIfPresent(transkey) != null && Objects.requireNonNull(this.translationCache.getIfPresent(transkey)).containsKey(lang)) {
            return Objects.requireNonNull(this.translationCache.getIfPresent(transkey)).get(lang);
        }
        
        try (Connection connection = this.mySQL.dataSource.getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT translation FROM " + lang.toLowerCase() + " WHERE transkey='" + transkey.toLowerCase() + "';");
            if (rs.next()) {
                String translation;
                if (!Source.isBungeeCordMode) {
                    translation = ChatColor.translateAlternateColorCodes('&', rs.getString("translation"));
                } else {
                    translation = net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', rs.getString("translation"));
                }

                HashMap<String, String> cacheMap = new HashMap<>();
                cacheMap.put(lang, translation);
                translationCache.put(transkey, cacheMap);
                return translation;
            }
        } catch (SQLException throwables) {
            return transkey;
        }
        return transkey;
    }

    @Override
    public boolean isLanguage(String language) {
        if (this.getAvailableLanguages().isEmpty()) {
            throw new UnsupportedOperationException("There are no languages available");
        }
        return this.getAvailableLanguages().contains(language.toLowerCase());
    }

    @NotNull
    @Override
    public ArrayList<String> getAvailableLanguages() {
        ArrayList<String> languages = new ArrayList<>();

        try (Connection connection = this.mySQL.dataSource.getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT language FROM languages");
            while (rs.next()) {
                languages.add(rs.getString("language").toLowerCase());
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return languages;
    }

    @Override
    public ArrayList<String> getAllTranslationKeys(String language) {
        ArrayList<String> keys = new ArrayList<>();
        if (this.isLanguage(language)) {
            try (Connection connection = this.mySQL.dataSource.getConnection()) {
                ResultSet rs = connection.createStatement().executeQuery("SELECT transkey FROM " + language);
                while (rs.next()) {
                    keys.add(rs.getString("transkey"));
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return keys;
        }
        throw new IllegalArgumentException(language + " was not found");
    }

    @Override
    public ArrayList<String> getAllTranslations(String language) {
        ArrayList<String> messages = new ArrayList<>();
        if (this.isLanguage(language)) {
            try (Connection connection = this.mySQL.dataSource.getConnection()) {
                ResultSet rs = connection.createStatement().executeQuery("SELECT translation FROM " + language);
                while (rs.next()) {
                    messages.add(rs.getString("translation"));
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();

            }
            return messages;
        }
        throw new IllegalArgumentException(language + " was not found");
    }

    @Override
    public String getDefaultLanguage() {
        return Source.getDefaultLanguage().toLowerCase();
    }

    @Override
    public String getPrefix() {
        return this.getMessage("languageapi-prefix", this.getDefaultLanguage());
    }

    @Override
    public String getPrefix(String language) {
        return this.getMessage("languageapi-prefix", language);
    }

    private void logInfo(String message) {
        Source.log(message, Level.INFO);
    }

}