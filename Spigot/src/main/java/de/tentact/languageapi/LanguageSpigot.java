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

package de.tentact.languageapi;

import de.tentact.languageapi.api.SpigotLanguageAPI;
import de.tentact.languageapi.command.LanguageCommand;
import de.tentact.languageapi.configuration.LanguageConfig;
import de.tentact.languageapi.configuration.MySQL;
import de.tentact.languageapi.configuration.SpigotConfiguration;
import de.tentact.languageapi.listener.ChatListener;
import de.tentact.languageapi.listener.InventoryClickListener;
import de.tentact.languageapi.listener.JoinListener;
import de.tentact.languageapi.util.Updater;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

public class LanguageSpigot extends JavaPlugin {

    private Updater updater;
    private MySQL mySQL;
    public SpigotConfiguration spigotConfiguration;

    @Override
    public void onEnable() {

        this.getLogger().log(Level.INFO, "Starting LanguageAPI Version: " + this.getVersion());

        this.spigotConfiguration = new SpigotConfiguration(this.getLogger());
        LanguageConfig languageConfig = this.spigotConfiguration.getLanguageConfig();

        this.mySQL = languageConfig.getMySQL();
        this.mySQL.connect();
        LanguageAPI.setInstance(new SpigotLanguageAPI(languageConfig));
        this.mySQL.createDefaultTable();

        LanguageAPI.getInstance().createLanguage(languageConfig.getLanguageSetting().getDefaultLanguage());
        this.updater = new Updater(this);

        LanguageCommand languageCommand = new LanguageCommand(this);
        Objects.requireNonNull(this.getCommand("languageapi")).setExecutor(languageCommand);
        Objects.requireNonNull(this.getCommand("languageapi")).setTabCompleter(new LanguageCommand(this));

        new JoinListener(this);
        new ChatListener(this, languageCommand);
        new InventoryClickListener(this, this.spigotConfiguration.getLanguageInventory());

    }

    @Override
    public void onDisable() {
        mySQL.closeConnection();
    }

    public String getVersion() {
        return "1.9-SNAPSHOT-1024-0512";
    }

    public Updater getUpdater() {
        return this.updater;
    }
}