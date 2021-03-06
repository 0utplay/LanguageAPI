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

package de.tentact.languageapi.command;

import de.tentact.languageapi.LanguageAPI;
import de.tentact.languageapi.LanguageSpigot;
import de.tentact.languageapi.configuration.LanguageInventoryConfiguration;
import de.tentact.languageapi.configuration.SpigotConfiguration;
import de.tentact.languageapi.i18n.I18N;
import de.tentact.languageapi.player.LanguagePlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class LanguageCommand implements TabExecutor {

    public final LanguageAPI languageAPI = LanguageAPI.getInstance();
    private final List<String> tabComplete = Arrays.asList(
            "add", "remove", "update", "create", "delete",
            "param", "copy", "translations", "reload",
            "import", "export", "help", "info", "list");
    private final LanguageSpigot languageSpigot;
    private final LanguageInventoryConfiguration languageInventory;
    private final String version;
    private static MetadataValue EDIT_MESSAGE;

    public LanguageCommand(LanguageSpigot languageSpigot) {
        this.languageSpigot = languageSpigot;
        this.languageInventory = languageSpigot.getSpigotConfiguration().getLanguageInventory();
        this.version = languageSpigot.getVersion();
        EDIT_MESSAGE = new FixedMetadataValue(languageSpigot, true);
    }

    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            LanguagePlayer languagePlayer = this.languageAPI.getPlayerExecutor().getLanguagePlayer(player.getUniqueId());
            if (languagePlayer == null) {
                return false;
            }
            if (player.hasPermission("system.languageapi")) {
                if (args.length >= 1) {
                    switch (args[0].toLowerCase()) {
                        case "add":
                            if (this.checkDoesNotHavePermission(player, args)) {
                                return false;
                            }
                            if (!(args.length >= 4)) {
                                languagePlayer.sendMessage(I18N.LANGUAGEAPI_ADD_HELP.get());
                                return false;
                            }
                            String language = args[1].toLowerCase();
                            if (!this.languageAPI.isLanguage(language)) {
                                languagePlayer.sendMessage(I18N.LANGUAGEAPI_LANG_NOT_FOUND.get().replace("%LANG%", language));
                                return false;
                            }
                            String key = args[2].toLowerCase();
                            if (!this.languageAPI.isKey(key, language)) {
                                StringBuilder messageBuilder = new StringBuilder();
                                for (int i = 3; i < args.length; i++) {
                                    messageBuilder.append(args[i]).append(" ");
                                }
                                this.languageAPI.addMessage(key, messageBuilder.toString(), language);
                                languagePlayer.sendMessage(I18N.LANGUAGEAPI_ADD_SUCCESS.get()
                                        .replace("%KEY%", key)
                                        .replace("%LANG%", language)
                                        .replace("%MSG%", messageBuilder.toString()));
                                return true;
                            } else {
                                languagePlayer.sendMessage(I18N.LANGUAGEAPI_KEY_ALREADY_EXISTS.get()
                                        .replace("%KEY%", key)
                                        .replace("%LANG%", language));
                                return false;
                            }
                        case "update":
                            if (this.checkDoesNotHavePermission(player, args)) {
                                return false;
                            }
                            if (!(args.length >= 3)) {
                                languagePlayer.sendMessage(I18N.LANGUAGEAPI_UPDATE_HELP.get());
                                return false;
                            }
                            language = args[1];
                            key = args[2].toLowerCase();
                            this.languageAPI.isLanguageAsync(language).thenAccept(isLanguage -> {
                                if(!isLanguage) {
                                    languagePlayer.sendMessage(I18N.LANGUAGEAPI_LANG_NOT_FOUND.get()
                                            .replace("%LANG%", language));
                                    return;
                                }
                                this.languageAPI.isKeyAsync(key, language).thenAccept(isKey -> {
                                    if (isKey) {
                                        player.setMetadata("editMessage", EDIT_MESSAGE);
                                        player.setMetadata("editParameter", new FixedMetadataValue(this.languageSpigot, Arrays.asList(key, language)));
                                        languagePlayer.sendMessage(I18N.LANGUAGEAPI_UPDATE_INSTRUCTIONS.get());
                                    } else {
                                        languagePlayer.sendMessage(I18N.LANGUAGEAPI_KEY_NOT_FOUND.get().replace("%KEY%", key)
                                                .replace("%LANG%", language));
                                    }
                                });
                            });
                        case "create":
                            if (this.checkDoesNotHavePermission(player, args)) {
                                return false;
                            }
                            if (!(args.length >= 2)) {
                                languagePlayer.sendMessage(I18N.LANGUAGEAPI_CREATE_HELP.get());
                                return false;
                            }
                            this.languageAPI.isLanguageAsync(args[1]).thenAccept(isLanguage -> {
                                if (!isLanguage) {
                                    this.languageAPI.createLanguage(args[1]);
                                    languagePlayer.sendMessage(I18N.LANGUAGEAPI_CREATE_SUCCESS.get().replace("%LANG%", args[1]));
                                } else {
                                    languagePlayer.sendMessage(I18N.LANGUAGEAPI_LANG_ALREADY_EXISTS.get().replace("%LANG%", args[1]));
                                }
                            });
                            break;
                        case "list":
                            if (this.checkDoesNotHavePermission(player, args)) {
                                return false;
                            }
                            this.languageAPI.getAvailableLanguagesAsync().thenAccept(languageList ->
                                    languagePlayer.sendMessage(I18N.LANGUAGEAPI_LANGUAGES_LIST.get()
                                            .replace("%LANGUAGES%", String.join(", ", languageList))));
                        case "delete":
                            if (this.checkDoesNotHavePermission(player, args)) {
                                return false;
                            }
                            if (!(args.length >= 2)) {
                                return false;
                            }
                            language = args[1];
                            this.languageAPI.executeAsync(() -> {
                                List<String> availableLanguages = this.languageAPI.getAvailableLanguages();
                                if (this.containsIgnoreCase(availableLanguages, language) && !this.languageAPI.getDefaultLanguage().equalsIgnoreCase(language)) {
                                    this.languageAPI.deleteLanguage(language);
                                    languagePlayer.sendMessage(I18N.LANGUAGEAPI_DELETE_SUCCESS.get().replace("%LANG%", language));
                                } else if (language.equalsIgnoreCase("*")) {
                                    availableLanguages.forEach(this.languageAPI::deleteLanguage);

                                    languagePlayer.sendMessage(I18N.LANGUAGEAPI_DELETE_ALL_LANGS.get());
                                } else {
                                    languagePlayer.sendMessage(I18N.LANGUAGEAPI_LANG_NOT_FOUND.get()
                                            .replace("%LANG%", language));
                                }
                            });
                        case "copy":
                            if (this.checkDoesNotHavePermission(player, args)) {
                                return false;
                            }
                            if (args.length >= 3) {
                                String langfrom = args[1];
                                String langto = args[2];
                                this.languageAPI.isLanguageAsync(langfrom).thenAcceptAsync(isLangFrom ->
                                        this.languageAPI.isLanguageAsync(langto).thenAcceptAsync(isLangTo -> {
                                            String resultLanguage;
                                            if (isLangFrom && isLangTo) {
                                                this.languageAPI.copyLanguage(langfrom, langto);
                                                languagePlayer.sendMessage(I18N.LANGUAGEAPI_COPY_SUCCESS.get()
                                                        .replace("%OLDLANG%", langfrom)
                                                        .replace("%NEWLANG%", langto));
                                            } else {
                                                resultLanguage = langfrom;
                                                if (this.containsIgnoreCase(this.languageAPI.getAvailableLanguages(), langfrom)) {
                                                    resultLanguage = langto;
                                                }
                                                languagePlayer.sendMessage(I18N.LANGUAGEAPI_LANG_NOT_FOUND.get()
                                                        .replace("%LANG%", resultLanguage));
                                            }
                                        }));
                            } else {
                                languagePlayer.sendMessage(I18N.LANGUAGEAPI_COPY_HELP.get());
                                return false;
                            }
                        case "param": //language param key
                            if (this.checkDoesNotHavePermission(player, args)) {
                                return false;
                            }
                            if (!(args.length >= 2)) {
                                return false;
                            }
                            key = args[1].toLowerCase();
                            if (!this.languageAPI.hasParameter(key)) {
                                languagePlayer.sendMessage(I18N.LANGUAGEAPI_KEY_HAS_NO_PARAM.get().replace("%KEY%", key));
                                return false;
                            }
                            this.languageAPI.getParameterAsListAsync(key).thenAccept(parameterList ->
                                    languagePlayer.sendMessage(I18N.LANGUAGEAPI_SHOW_SUCCESS.get()
                                            .replace("%PARAM%", String.join(",", parameterList))
                                            .replace("%KEY%", key)));
                            return true;
                        case "translations":
                            if (this.checkDoesNotHavePermission(player, args)) {
                                return false;
                            }
                            language = args[1];
                            if (this.languageAPI.isLanguage(language)) {
                                Map<String, String> translationMap = this.languageAPI.getKeysAndTranslations(language);
                                for (Map.Entry<String, String> translationEntry : translationMap.entrySet()) {
                                    languagePlayer.sendMessage(I18N.LANGUAGEAPI_TRANSLATION_SUCCESS.get().replace("%KEY%", translationEntry.getKey())
                                            .replace("%MSG%", translationEntry.getValue()));
                                }
                                return true;
                            } else {
                                languagePlayer.sendMessage(I18N.LANGUAGEAPI_LANG_NOT_FOUND.get()
                                        .replace("%LANG%", language));
                                return false;
                            }
                        case "remove": //language remove language key
                            if (this.checkDoesNotHavePermission(player, args)) {
                                return false;
                            }
                            if (args.length >= 3) {
                                language = args[1];
                                key = args[2].toLowerCase();
                                this.languageAPI.executeAsync(() -> {
                                    if (this.languageAPI.getDefaultLanguage().contains(language)) {
                                        if (this.languageAPI.isKey(key, language)) {
                                            this.languageAPI.deleteMessage(key, language); //EINE SPRACHE EIN KEY
                                            languagePlayer.sendMessage(I18N.LANGUAGEAPI_REMOVE_KEY_IN_LANGUAGE.get()
                                                    .replace("%KEY%", key)
                                                    .replace("%LANG%", language));
                                        } else if (key.endsWith("*")) {
                                            for (String keys : this.languageAPI.getAllTranslationKeys(language)) {
                                                if (keys.startsWith(key.replace("*", ""))) {
                                                    this.languageAPI.deleteMessage(keys, language);
                                                }
                                            }
                                            languagePlayer.sendMessage(I18N.LANGUAGEAPI_REMOVE_EVERY_KEY_IN_LANGUAGE.get()
                                                    .replace("%LANG%", language)
                                                    .replace("%STARTSWITH%", key.replace("*", "")));
                                        } else {
                                            languagePlayer.sendMessage(I18N.LANGUAGEAPI_KEY_NOT_FOUND.get()
                                                    .replace("%LANG%", language).replace("%KEY%", key));
                                        }
                                    } else if (language.equalsIgnoreCase("*")) {
                                        if (key.endsWith("*")) { //JEDE SPRACHE JEDER KEY
                                            this.languageAPI.getAvailableLanguages().forEach(langs -> this.languageAPI.getAllTranslationKeys(langs).forEach(keys -> {
                                                if (keys.startsWith(key.replace("*", ""))) {
                                                    if (!keys.startsWith("languageapi-")) {
                                                        this.languageAPI.deleteMessage(keys, langs);
                                                        Bukkit.getScheduler().runTaskLater(this.languageSpigot, () -> this.languageAPI.deleteAllParameter(key), 45L);
                                                    }
                                                }
                                            }));
                                            languagePlayer.sendMessage(I18N.LANGUAGEAPI_REMOVE_EVERY_KEY_IN_EVERY_LANGUAGE.get()
                                                    .replace("%STARTSWITH%", key.
                                                            replace("*", "")));
                                        } else { //JEDE SPRACHE EIN KEY
                                            this.languageAPI.getAvailableLanguages().forEach(langs -> {
                                                if (this.languageAPI.isKey(key, langs)) {
                                                    this.languageAPI.deleteMessage(key, langs);
                                                    this.languageAPI.deleteAllParameter(key);
                                                }
                                            });
                                            languagePlayer.sendMessage(I18N.LANGUAGEAPI_REMOVE_KEY_IN_EVERY_LANGUAGE.get()
                                                    .replace("%KEY%", key));
                                        }
                                    }
                                });
                            }
                            break;
                        case "import": //lang import FILE BOOL
                            if (this.checkDoesNotHavePermission(player, args)) {
                                return false;
                            }
                            if (args.length < 3) {
                                languagePlayer.sendMessage(I18N.LANGUAGEAPI_IMPORT_HELP.get());
                                return false;
                            }
                            File file = new File("plugins/LanguageAPI/import", args[1]);
                            if (!file.exists()) {
                                languagePlayer.sendMessage(I18N.LANGUAGEAPI_IMPORT_FILE_NOT_FOUND.get()
                                        .replace("%FILE%", args[1]));
                                return false;
                            }
                            this.languageAPI.getFileHandler().loadFileAsync(file, this.parseBoolean(args[2])).thenAccept(passedLoad -> {
                                if (!passedLoad) {
                                    languagePlayer.sendMessage(I18N.LANGUAGEAPI_IMPORT_ERROR.get());
                                    return;
                                }
                                languagePlayer.sendMessage(I18N.LANGUAGEAPI_IMPORT_SUCCESS.get()
                                        .replace("%FILE%", args[1]));
                            });
                            break;
                        case "export":
                            if (this.checkDoesNotHavePermission(player, args)) {
                                return false;
                            }
                            if (args.length < 2) {
                                languagePlayer.sendMessage(I18N.LANGUAGEAPI_EXPORT_HELP.get());
                                return false;
                            }
                            if (!this.languageAPI.isLanguage(args[1]) && !args[1].equalsIgnoreCase("@a") && !args[1].equalsIgnoreCase("all")) {
                                languagePlayer.sendMessage(I18N.LANGUAGEAPI_LANG_NOT_FOUND.get().replace("%LANG%", args[1]));
                                return false;
                            }
                            if (args[1].equalsIgnoreCase("@a")
                                    || args[1].equalsIgnoreCase("all")
                                    || args[1].equalsIgnoreCase("*")) {
                                this.languageAPI.getFileHandler().exportAll().thenAccept(passedExport -> {
                                    if (!passedExport) {
                                        languagePlayer.sendMessage(I18N.LANGUAGEAPI_EXPORT_ERROR.get().replace("%LANGUAGE%", args[1]));
                                        return;
                                    }
                                    languagePlayer.sendMessage(I18N.LANGUAGEAPI_EXPORT_ALL_SUCCESS.get());
                                });
                            } else {
                                this.languageAPI.getFileHandler().exportLanguageToFile(args[1]).thenAccept(passedExport -> {
                                    if (!passedExport) {
                                        languagePlayer.sendMessage(I18N.LANGUAGEAPI_EXPORT_ERROR.get().replace("%LANGUAGE%", args[1]));
                                        return;
                                    }
                                    languagePlayer.sendMessage(I18N.LANGUAGEAPI_EXPORT_SUCCESS.get()
                                            .replace("%FILE%", args[1].toLowerCase() + ".yml"));
                                });
                            }
                            break;
                        case "reload":
                            if (this.checkDoesNotHavePermission(player, args)) {
                                return false;
                            }
                            this.languageSpigot.getLogger().log(Level.INFO, "Reloading config.json...");
                            this.languageSpigot.setSpigotConfiguration(new SpigotConfiguration(this.languageSpigot.getLogger()));
                            languagePlayer.sendMessage(I18N.LANGUAGEAPI_RELOAD_SUCCESS.get());
                            break;
                        case "info":
                            if (this.checkDoesNotHavePermission(player, args)) {
                                return false;
                            }
                            languagePlayer.sendMessage(I18N.LANGUAGEAPI_INFO.get().replace("%VERSION%", version));
                            break;
                        default:
                            if (this.checkDoesNotHavePermission(player, args)) {
                                return false;
                            }
                            languagePlayer.sendMultipleTranslation(I18N.LANGUAGEAPI_HELP.get());
                            break;
                    }
                } else {
                    Inventory inventory = this.languageInventory.getLanguageInventory();
                    if (inventory == null) {
                        return false;
                    }
                    player.openInventory(inventory);
                }
            } else {
                languagePlayer.sendMessage(I18N.LANGUAGEAPI_NOPERMS.get());
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] args) {
        if (args.length == 1) {
            return this.getTabCompletes(args[0], this.tabComplete);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create")) {
                return Collections.emptyList();
            }
            if (!args[0].equalsIgnoreCase("param")) {
                return this.getTabCompletes(args[1], this.languageAPI.getAvailableLanguages());
            }
            return this.getTabCompletes(args[1], this.languageAPI.getAllTranslationKeys(this.languageAPI.getDefaultLanguage()));
        } else if (args.length == 3) {
            List<String> keyComplete = Arrays.asList("add", "remove", "update");
            if (keyComplete.contains(args[0].toLowerCase())) {
                return this.getTabCompletes(args[2], this.languageAPI.getAllTranslationKeys(args[1].toLowerCase()));
            }
        }
        return Collections.emptyList();
    }

    private List<String> getTabCompletes(String playerInput, List<String> tabComplete) {
        List<String> possibleCompletes = new ArrayList<>();
        StringUtil.copyPartialMatches(playerInput, tabComplete, possibleCompletes);
        Collections.sort(possibleCompletes);
        return possibleCompletes;
    }

    private boolean parseBoolean(String input) {
        return input.equalsIgnoreCase("yes") || input.equalsIgnoreCase("true") || input.equalsIgnoreCase("ja");
    }

    private boolean checkDoesNotHavePermission(Player player, String[] args) {
        LanguagePlayer languagePlayer = this.languageAPI.getPlayerExecutor().getLanguagePlayer(player.getUniqueId());
        if (player.hasPermission("system.languageapi." + args[0])) {
            return false;
        }
        if (languagePlayer != null) {
            languagePlayer.sendMessage(I18N.LANGUAGEAPI_NOPERMS.get());
        }
        return true;
    }

    private boolean containsIgnoreCase(List<String> list, String comparingString) {
        for (String s : list) {
            if (s.equalsIgnoreCase(comparingString)) {
                return true;
            }
        }
        return false;
    }
}
