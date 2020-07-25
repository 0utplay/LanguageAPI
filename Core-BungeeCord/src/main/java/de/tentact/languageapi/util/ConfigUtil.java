package de.tentact.languageapi.util;
/*  Created in the IntelliJ IDEA.
    Created by 0utplay | Aldin Sijamhodzic
    Datum: 25.04.2020
    Uhrzeit: 17:05
*/

import de.tentact.languageapi.AbstractLanguageAPI;
import de.tentact.languageapi.LanguageBungeecord;
import de.tentact.languageapi.api.LanguageAPI;
import de.tentact.languageapi.mysql.MySQL;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigUtil {


    public static boolean isBungeeCordMode;

    private static Configuration bungeecordmySQLConfiguration;

    private static MySQL mySQL;

    private static Logger logger;
    public static String defaultLanguage;

    public static void initBungeecord() {
        mySQL = new MySQL(bungeecordmySQLConfiguration.getString("mysql.hostname"),
                bungeecordmySQLConfiguration.getString("mysql.database"),
                bungeecordmySQLConfiguration.getString("mysql.username"),
                bungeecordmySQLConfiguration.getString("mysql.password"),
                bungeecordmySQLConfiguration.getInt("mysql.port"));
        AbstractLanguageAPI.setInstance(new LanguageAPI());

    }

    public static void createBungeeCordMySQLConfig(LanguageBungeecord languageBungeecord) {

        File bungeecordmySQLConfigFile = new File("plugins/LanguageAPI", "config.yml");
        if (!languageBungeecord.getDataFolder().exists()) {
            languageBungeecord.getDataFolder().mkdir();
        }

        try {
            bungeecordmySQLConfiguration = ConfigurationProvider.getProvider(net.md_5.bungee.config.YamlConfiguration.class).load(bungeecordmySQLConfigFile);

            if (!bungeecordmySQLConfigFile.exists()) {
                bungeecordmySQLConfigFile.createNewFile();
                bungeecordmySQLConfiguration.set("mysql.host", "hostname");
                bungeecordmySQLConfiguration.set("mysql.username", "de/tentact/languageapi");
                bungeecordmySQLConfiguration.set("mysql.database", "de/tentact/languageapi");
                bungeecordmySQLConfiguration.set("mysql.password", "password");
                bungeecordmySQLConfiguration.set("languageapi.defaultlang", "de_de");
            }
            ConfigurationProvider.getProvider(net.md_5.bungee.config.YamlConfiguration.class).save(bungeecordmySQLConfiguration, bungeecordmySQLConfigFile);

        } catch (IOException ignored) {

        }
    }
    @NotNull
    public static String getDefaultLanguage() {
        //TODO: REWORK THIS IN ONLY BUNGEECORD
        return defaultLanguage;
    }

    public static boolean getUpdateNotfication() {
        return bungeecordmySQLConfiguration.getBoolean("language.notify");
    }
    @NotNull
    public static MySQL getMySQL() {
        return mySQL;
    }
    public static void initLogger(Logger initLogger) {
        logger = initLogger;
    }
    public static void log(String message, Level logLevel) {
        logger.log(logLevel, message);
    }
}