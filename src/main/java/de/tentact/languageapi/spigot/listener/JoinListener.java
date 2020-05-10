package de.tentact.languageapi.spigot.listener;
/*  Created in the IntelliJ IDEA.
    Created by 0utplay | Aldin Sijamhodzic
    Datum: 25.04.2020
    Uhrzeit: 19:03
*/

import de.tentact.languageapi.LanguageSpigot;
import de.tentact.languageapi.api.LanguageAPI;
import de.tentact.languageapi.util.Source;
import de.tentact.languageapi.util.Updater;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private Updater updater = LanguageSpigot.updater;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        LanguageAPI.getInstance().createPlayer(player.getUniqueId());
        if(Source.getUpdateNotfication()) {
            if(updater.hasUpdate()) {
                if(player.hasPermission("languageapi.notify")) {
                    player.sendMessage(LanguageAPI.getInstance().getPrefix()+"Es ist ein neues Update verfügbar. Aktuelle Version: §6"+updater.getLocalVersion()+"§7. Neuste Version: §c"+updater.getOnlineVersion()+"");
                }
            }

        }

    }
}
